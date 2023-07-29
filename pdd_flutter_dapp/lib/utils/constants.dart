

const String APP_NAME = "PDD Mission Tracker ";
const String RETRIEVE_MISSION_DATA_BTN = "Retrieve Mission Data";
const String START_MISSION_BTN = "Start Mission !";
const double LOC_REPORT_DIST_M = 10; // if dist b/n curr loc and new loc is more than LOC_REPORT_DIST_M we will report the new loc to CC
const String CC_REST_API_URL = "http://10.0.2.2:5000";

bool kDebugMode = true;
void myLog(String str) {
  if (kDebugMode) {
    // ignore: avoid_print
    print("pdd debug:$str");
  }
}


/*
void main() {
  int appID = 259780580;
  //", "app_id": 257383442
  String mnemonic = "symptom exhibit click fatigue salmon rhythm poverty fame vehicle recall cherry month basic extend flush own dune pepper winter flame inside flock sadness absorb suggest";
  String status = "success";
  double lat = 110.0;
  double lon = 121.0;
  setMissionStatusInChain(appID, mnemonic, status, lat.toString(), lon.toString());
}
*/
