# cc_app_rest_calls.py. These calls will be used by command center flask web app
import json
from datetime import datetime

from flask import render_template, request, session
from sqlalchemy import create_engine, and_
from sqlalchemy.orm import sessionmaker

from webapp.db_model.MissionDBModel import User, Mission, InprogressMission, MissionRouteMap
from webapp.utils import alogd_sc_calls

# this file has rest endpoints that will be used by android client

engine = create_engine("sqlite:///db_model/mission_data.db", echo=True)

Session = sessionmaker(bind=engine)


def get_mission_details():
    dbsession = Session()
    ret = {"ret_code": 401}
    try:
        request_data = request.get_json()
        # print(request_data)
        email = request_data.get('email')
        access_code = request_data.get('access_code')

        res = dbsession.query(User.mnemonic, Mission.algo_appid).join(Mission).filter(
            and_(
                User.email.like(email),
                User.access_code.like(access_code),
                Mission.tgt_addr == User.pub_addr
            )
        ).all()
        if len(res) == 1:
            first_tuple = res[0]
            ret.pop("ret_code")
            ret["ret_code"] = 200
            ret["mnemonic"] = first_tuple[0]
            ret["app_id"] = first_tuple[1]
    except Exception as e:
        print(e)
    finally:
        dbsession.close()
        return json.dumps(ret)


def update_mission_status():
    dbsession = Session()
    ret = {"ret_code": 401}  # init as failed or 401 (unauthorized)
    try:
        request_data = request.get_json()
        # print(request_data)
        app_id = request_data.get('appid')
        status = request_data.get('status')

        # the logic here is if the status is inprogress, update the inprogress_mission table
        # if the status is failed/succeded. update the mission table

        ## this may be just a report back on progress for tracking. this info we wont write
        ## to bc. only finite states(created/failed/ are stored on bc to reduce cost of chain trans
        if status == "inprogress":
            in_prog_missn = InprogressMission(app_id)
            dbsession.add(in_prog_missn)
            dbsession.commit()
            ret = {"ret_code": 200}
        elif status == "failed" or status == "success":
            dbsession.begin()
            try:
                # Retrieve the record you want to update
                missn_rec = dbsession.query(Mission).filter_by(algo_appid=app_id).first()
                # Check if the record exists
                if missn_rec:
                    # Modify the attributes or columns
                    missn_rec.status = status
                    missn_rec.status_upd_date = datetime.datetime.now()
                    # now remove the inprogress record of same
                    inprog_rec = dbsession.query(InprogressMission).filter_by(algo_appid=app_id).first()
                    if inprog_rec:
                        dbsession.delete(inprog_rec)
                        # Commit the changes to the database
                        dbsession.commit()  # if both succeeds commit
                        # set return as success
                        ret = {"ret_code": 200}
                    else:
                        dbsession.rollback()  # if one fails rollback
            except Exception as e:
                print(e)
                dbsession.rollback()  # all or nothing
    except Exception as e:
        print(e)
        dbsession.rollback()  # all or nothing
    finally:
        dbsession.close()
        return json.dumps(ret)


def update_mission_location():
    dbsession = Session()
    ret = {"ret_code": 401}  # init as failed or 401 (unauthorized)
    try:
        request_data = request.get_json()
        # print(request_data)
        app_id = request_data.get('appid')
        latlon = request_data.get('latlon')

        missn_curr_loc_rec = MissionRouteMap(app_id, latlon, datetime.datetime.now())
        dbsession.add(missn_curr_loc_rec)
        ret = {"ret_code": 200}
        dbsession.commit()
    except Exception as e:
        print(e)
        dbsession.rollback()  # all or nothing
    finally:
        dbsession.close()
        return json.dumps(ret)


def set_latlon():
    dbsession = Session()
    lat = request.form['lat']
    lon = request.form['lon']
    address = request.form['address']
    status = request.form['status']
    appid = request.form['appid']
    ret = alogd_sc_calls.set_latlon_in_chain(status, address, lat, lon, appid, session["mn"])
    res = "Failed to set info"
    if ret is not None:
        # update the cc db mission table as well
        try:
            request_data = request.get_json()
            # print(request_data)
            app_id = request_data.get('appid')
            latlon = request_data.get('latlon')

            miss_rec = Mission(address, appid, datetime.datetime.now())
            dbsession.add(miss_rec)
            dbsession.commit()
            res = "Successfully set info for : " + address
        except Exception as e:
            print(e)
            dbsession.rollback()  # all or nothing
            res = "Failed : " + str(e)
        finally:
            dbsession.close()
            return render_template('result.html', ret=res)
