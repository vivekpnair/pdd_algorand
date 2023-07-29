import json

from sqlalchemy import ForeignKey, Column, String, Integer, DateTime
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

####################################################################################
class User(Base):
    __tablename__ = "users"
    pub_addr = Column("pub_addr", String, primary_key=True)
    mnemonic = Column("mnemonic", String)
    access_code = Column("access_code",String)
    fullname = Column("fullname", String)
    email = Column("email", String, unique=True)
    mobile = Column("mobile", String, unique=True)

    def __init__(self, pub_addr, mnemonic, access_code, fullname, email, mobile):
        self.pub_addr = pub_addr
        self.mnemonic = mnemonic
        self.access_code = access_code
        self.fullname = fullname
        self.email = email
        self.mobile = mobile

    def __repr__(self):
        table = User.__table__
        rec_dict = {column.name: getattr(self, column.name) for column in table.columns}
        return json.dumps(rec_dict)
####################################################################################

####################################################################################
class AdminUser(Base):
    __tablename__ = "admin_users"
    pub_addr = Column("pub_addr", String, primary_key=True)
    mnemonic = Column("mnemonic", String)
    access_code = Column("access_code",String)
    fullname = Column("fullname", String)
    email = Column("email", String, unique=True)
    mobile = Column("mobile", String, unique=True)

    def __init__(self, pub_addr, mnemonic, access_code, fullname, email, mobile):
        self.pub_addr = pub_addr
        self.mnemonic = mnemonic
        self.access_code = access_code
        self.fullname = fullname
        self.email = email
        self.mobile = mobile

    def __repr__(self):
        table = AdminUser.__table__
        rec_dict = {column.name: getattr(self, column.name) for column in table.columns}
        return json.dumps(rec_dict)
####################################################################################

####################################################################################
class Mission(Base):
    __tablename__ = "missions"
    id = Column("id", Integer, primary_key=True, autoincrement=True)
    tgt_addr = Column("pub_addr", String)
    algo_appid = Column("algo_appid", Integer, unique=True)
    status = Column("status", String)
    creation_date = Column("creation_date", DateTime)
    status_upd_date = Column("status_upd_date", DateTime)

    def __init__(self, tgt_addr, algo_appid, status, creation_date):
        self.tgt_addr = tgt_addr
        self.algo_appid = algo_appid
        self.status = status
        self.creation_date = creation_date

    def __repr__(self):
        # table = Mission.__table__
        # rec_dict = {column.name: getattr(self, column.name) for column in table.columns}
        # return json.dumps(rec_dict)
        # return f"Mission({Mission.algo_appid}={self.algo_appid}, {Mission.creation_date}='{self.creation_date}',{Mission.status_upd_date}='{self.status_upd_date}')"
        attrs = ', '.join(f"{attr}={getattr(self, attr)!r}" for attr in vars(self) if attr != '_sa_instance_state')
        return f"{self.__class__.__name__}({attrs})"


####################################################################################

####################################################################################
class MissionRouteMap(Base):
    __tablename__ = "mission_route_maps"
    id = Column("id", Integer, primary_key=True, autoincrement=True)
    algo_appid = Column("appid", String, ForeignKey("missions.algo_appid"))
    latlon = Column("latlon", String)
    loc_timestamp = Column("loc_timestamp", DateTime)
    mission_status = Column("mission_status", String)

    def __init__(self, algo_appid, latlon, loc_timestamp, mission_status):
        self.algo_appid = algo_appid
        self.latlon = latlon
        self.loc_timestamp = loc_timestamp
        self.mission_status = mission_status

    def __repr__(self):
        table = MissionRouteMap.__table__
        rec_dict = {column.name: getattr(self, column.name) for column in table.columns}
        return json.dumps(rec_dict)
####################################################################################