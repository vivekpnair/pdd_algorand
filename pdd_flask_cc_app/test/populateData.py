import datetime
import json

from sqlalchemy import create_engine, ForeignKey, Column, String, Integer, DateTime
from sqlalchemy.orm import sessionmaker, declarative_base

from webapp.db_model.MissionDBModel import Base, AdminUser, User

#####
#
# This is a utility program to create an AdminUser and a User in the db table. On a fresh installation
# run this file once
#
#####
if __name__ == '__main__':
    engine = create_engine("sqlite:///../webapp/db_model/mission_data.db", echo=True)  # path to db file
    Base.metadata.create_all(bind=engine)

    Session = sessionmaker(bind=engine)
    session = Session()
    # # # #
    mneumonic = "muscle shadow weird feel liquid manage whip leisure brave ensure length gorilla attract slush bamboo stadium hunt tag garage source raw cloth autumn ability sister";
    admin_user = AdminUser("WBAWDBC5HCNE33JGY4UWGX2XSTOLOXKF5X7PLVZPGTRV56HN44UOVU2FC", mneumonic, "1234", "vivek nair",
                           "vivekpremnair@gmail.com", "99018774283")
    session.add(admin_user)

    # # # #
    mneumonic = "symptom exhibit click fatigue salmon rhythm poverty fame vehicle recall cherry month basic extend flush own dune pepper winter flame inside flock sadness absorb suggest"
    user = User("NRVHLO4K5RCOXUHAQQFVKO6HUBDHTNMXGEYRTTW7O6FHCKDK4RUJL63JVU", mneumonic, "12345", "vivek prem nair",
                "vivekpremnair@gmail.com", "9901877428")
    session.add(user)
    # # # # # #
    # # now = datetime.datetime.now()
    # # # #
    # # mission = Mission("NRVHLO4K5RCOXUHAQQFVKO6HUBDHTNMXGEYRTTW7O6FHCKDK4RUJL63JVU","259780580","created", now)
    # # session.add(mission)
    # # # #
    # # # # inprogmiss = InprogressMission("257383442", "12.3445,56.13333")
    # # # # session.add(inprogmiss)
    # # # # #
    # # # #missroutemap = MissionRouteMap("257383442", "12.3445,56.13333",datetime.datetime.now())
    # # # #session.add(missroutemap)
    # # # # # #
    session.commit()
    session.close()

    # res = session.query(User).filter(User.fullname.like("premnair"))
    # print(res[0])
