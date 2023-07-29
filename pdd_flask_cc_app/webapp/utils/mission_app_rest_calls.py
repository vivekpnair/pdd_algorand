# mission_app_rest_calls.py. These calls will be used by Mission tracker flutter  app
import json
from datetime import datetime

from flask import render_template, request
from sqlalchemy import create_engine, and_
from sqlalchemy.orm import sessionmaker

from webapp.db_model.MissionDBModel import User, Mission, MissionRouteMap

# this file has rest endpoints that will be used by android client

engine = create_engine("sqlite:///db_model/mission_data.db", echo=True)

Session = sessionmaker(bind=engine)


def get_mission_details():
    session = Session()
    ret = {"ret_code": 401}
    try:
        request_data = request.get_json()
        # print(request_data)
        email = request_data.get('email')
        access_code = request_data.get('access_code')

        user = session.query(User).filter(User.email == email, User.access_code == access_code).first()
        if user:
            related_missions = session.query(Mission).filter(Mission.tgt_addr == user.pub_addr).all()
            if related_missions:
                ret.pop("ret_code")
                ret["ret_code"] = 200
                ret["mnemonic"] = user.mnemonic
                ret["app_id"] = related_missions[0].algo_appid
            else:
                print("Error:> Mission not found for user in missions table!. Pls add mission to missions table using "
                      "CC set_lat_lon!")
        else:
            print("Error:> User not found or email/pwd wrong !")
    except Exception as e:
        print(e)
    finally:
        session.close()
        return json.dumps(ret)


def update_mission_status():
    session = Session()
    ret = {"ret_code": 401}  # init as failed or 401 (unauthorized)
    try:
        request_data = request.get_json()
        # print(request_data)
        app_id = request_data.get('appid')
        status = request_data.get('status')
        session.begin()
        try:
            # Retrieve the record you want to update
            missn_rec = session.query(Mission).filter_by(algo_appid=app_id).first()
            # Check if the record exists
            if missn_rec:
                # Modify the attributes or columns
                missn_rec.status = status
                missn_rec.status_upd_date = datetime.now()
                session.commit()
                ret = {"ret_code": 200}
        except Exception as e:
            print(e)
            session.rollback()  # all or nothing
    except Exception as e:
        print(e)
        session.rollback()  # all or nothing
    finally:
        session.close()
        return json.dumps(ret)


def update_mission_location():
    session = Session()
    ret = {"ret_code": 401}  # init as failed or 401 (unauthorized)
    try:
        request_data = request.get_json()
        # print(request_data)
        app_id = request_data.get('appid')
        latlon = request_data.get('latlon')
        mission_status = request_data.get('mission_status')

        missn_curr_loc_rec = MissionRouteMap(app_id, latlon, datetime.now(), mission_status)
        session.add(missn_curr_loc_rec)
        ret = {"ret_code": 200}
        session.commit()
    except Exception as e:
        print(e)
        session.rollback()  # all or nothing
    finally:
        session.close()
        return json.dumps(ret)
