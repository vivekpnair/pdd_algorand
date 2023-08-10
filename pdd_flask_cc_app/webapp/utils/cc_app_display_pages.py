# cc_app_display_pages.py. These calls will be used by command center flask web app and mission tracker android app
import datetime
import json

from algosdk import account
from algosdk import mnemonic
from flask import render_template, request, session, redirect
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from webapp.db_model.MissionDBModel import User, Mission, MissionRouteMap, AdminUser

# this file has rest endpoints that will be used by android client

engine = create_engine("sqlite:///db_model/mission_data.db", echo=True)

Session = sessionmaker(bind=engine)


def disp_set_latlon_page():
    if "username" in session:
        dbsession = Session()
        statuses = ['created', 'inprogress', 'succeeded', 'failed']
        try:
            results = dbsession.query(User.pub_addr, User.fullname).all()
            pub_addr_array = []
            fullname_array = []

            for pub_addr, fullname in results:
                pub_addr_array.append(pub_addr)
                fullname_array.append(fullname)
        except Exception as e:
            print(e)
        finally:
            dbsession.close()
            return render_template('set_latlon.html', pub_addr_array=pub_addr_array, statuses=statuses,
                                   fullname_array=fullname_array)
        # Define a list of options for the dropdown
    else:
        return redirect("/login")


def login():
    if request.method == "POST":
        dbsession = Session()
        email = request.form["email"]
        access_code = request.form["access_code"]
        res = "Invalid credentials. Please try again. "
        try:
            admin_usr_rec = dbsession.query(AdminUser).filter_by(email=email, access_code=access_code).first()

            if admin_usr_rec:
                session["username"] = admin_usr_rec.email
                session["mnemonic"] = admin_usr_rec.mnemonic
                return redirect("/")
            else:
                return render_template("login.html", error=res)
        except Exception as e:
            res = res + str(e)
            return render_template("login.html", error=res)
        finally:
            dbsession.close()
    return render_template("login.html")


def disp_appids_page():
    if "username" in session:
        dbsession = Session()
        try:
            mission_tuples = dbsession.query(Mission).all()
            mission_list_of_dicts = []
            for mission in mission_tuples:
                mission_dict = mission.__dict__.copy()
                if mission_dict['creation_date'] is not None:
                    mission_dict['creation_date'] = mission_dict['creation_date'].isoformat()
                if mission_dict['status_upd_date'] is not None:
                    mission_dict['status_upd_date'] = mission_dict['status_upd_date'].isoformat()
                if '_sa_instance_state' in mission_dict:
                    del mission_dict['_sa_instance_state']
                mission_list_of_dicts.append(mission_dict)

            mission_data = json.dumps(mission_list_of_dicts)
            print(mission_data)
            if mission_tuples:
                return render_template('list_appids.html', mission_data=json.loads(mission_data), msg="")
            else:
                return render_template('list_appids.html', mission_data=[], msg="No apps setup")
        except Exception as e:
            print(e)
            return render_template('list_appids.html', mission_data=[], msg="Error" + str(e))
        finally:
            dbsession.close()
    else:
        return render_template("login.html")


def disp_appdetails_page():
    if "username" in session:
        dbsession = Session()
        try:
            appid = request.args.get('appid')
            mission_routemap_tuples = dbsession.query(MissionRouteMap).filter(MissionRouteMap.algo_appid == appid).all()
            mission_routemap_dict = []
            for mission in mission_routemap_tuples:
                mission_dict = mission.__dict__.copy()
                if mission_dict['loc_timestamp'] is not None:
                    mission_dict['loc_timestamp'] = mission_dict['loc_timestamp'].isoformat()
                if '_sa_instance_state' in mission_dict:
                    del mission_dict['_sa_instance_state']
                mission_routemap_dict.append(mission_dict)

            mission_data = json.dumps(mission_routemap_dict)
            print(mission_data)
            if mission_routemap_tuples:
                return render_template('mission_map.html', mission_data=json.loads(mission_data), msg="")
            else:
                return render_template('mission_map.html', mission_data=[], msg="No apps setup")
        except Exception as e:
            print(e)
            return render_template('mission_map.html', mission_data=[], msg="Error" + str(e))
        finally:
            dbsession.close()
    else:
        return render_template("login.html")


# this function will handle both adduser form display and submit
def add_user():
    if "username" in session:
        try:
            if request.method == "GET":
                private_key, pub_key = account.generate_account()
                mnemonic_var = mnemonic.from_private_key(private_key)
                return render_template('add_user.html', pub_key=pub_key, mnemonic=mnemonic_var)
            elif request.method == "POST":
                dbsession = Session()
                pub_key = request.form["pub_addr"]
                mnemonic_var = request.form["mnemonic"]
                access_code = request.form["access_code"]
                full_name = request.form["fullname"]
                email = request.form["email"]
                mobile = request.form["mobile"]
                user = User(pub_key, mnemonic_var, access_code, full_name, email, mobile)
                dbsession.add(user)
                dbsession.commit()
                res = "Successfully added user!. Dispense your account (using pub key) by clicking the link below "
        except Exception as e:
            print(e)
            if request.method == "POST":
                dbsession.rollback()  # all or nothing
            res = "Failed : " + str(e)
        finally:
            if request.method == "POST":
                dbsession.close()
                return render_template('result.html', ret=res)


def home():
    if "username" in session:
        return render_template('index.html')
    return redirect("/login")
