# cc_app_rest_calls.py. These calls will be used by command center flask web app
import json
from datetime import datetime

from flask import render_template, request, session
from sqlalchemy import create_engine, and_
from sqlalchemy.orm import sessionmaker

from webapp.db_model.MissionDBModel import User, Mission, MissionRouteMap
from webapp.utils import alogd_sc_calls

# this file has rest endpoints that will be used by android client

engine = create_engine("sqlite:///db_model/mission_data.db", echo=True)

Session = sessionmaker(bind=engine)


def set_latlon():
    dbsession = Session()
    lat = request.form['lat']
    lon = request.form['lon']
    address = request.form['address']
    status = request.form['status']
    appid = request.form['appid']
    mnemonic = session[
        'mnemonic']  # get the mnemonic from session (not the greatest security...need to get it from cc admins wallet... :p
    # def set_latlon_in_chain(status, address, lat, lon, app_id,dispatcher_mn):
    ret = "Failed to set lalon"
    # if ret is not None...update the CC db Mission table as well
    if ret is not None:
        try:
            mission_rec = Mission(address, appid, status, datetime.now())
            dbsession.add(mission_rec)
            res = alogd_sc_calls.set_latlon_in_chain(status, address, lat, lon, appid, mnemonic)
            if res is not None:
                dbsession.commit()
                ret = "Successfully set/updated info of : " + address
            else:
                dbsession.rollback()
        except Exception as e:
            print(e)
            ret = "Failed to set lalon : "+str(e)
            dbsession.rollback()  # all or nothing
        finally:
            dbsession.close()
    return render_template('result.html', ret=ret)
