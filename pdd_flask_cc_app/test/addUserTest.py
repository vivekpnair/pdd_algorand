import datetime

from algosdk import account, mnemonic
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from webapp.db_model.MissionDBModel import Base, User

#####
#
# This is a test  program to create a User in the db table. It will also generate a algorand account using algosdk
#
#####
if __name__ == '__main__':
    engine = create_engine("sqlite:///../webapp/db_model/mission_data.db", echo=True)  # path to db file
    Base.metadata.create_all(bind=engine)

    Session = sessionmaker(bind=engine)
    session = Session()
    # # # #

    # use algo sdk and create an account

    private_key, pub_key = account.generate_account()

    mnemonic_var = mnemonic.from_private_key(private_key)

    # now add a user to db using the ORM class User.

    access_code = "1234"
    full_name = "jon doe"
    email = "jdde@doej.com"
    mobile = "99999999"
    user = User(pub_key, mnemonic_var, access_code, full_name,email,mobile)
    now = datetime.datetime.now()
    session.add(user)

    session.commit()
    session.close()

