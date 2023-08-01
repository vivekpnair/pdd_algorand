import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_polyline_points/flutter_polyline_points.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:http/http.dart' as http;
import 'package:location/location.dart';
import 'package:maps_toolkit/maps_toolkit.dart'
    as maps_tk; // had to do this because there is a local LalLon inside this package which conflicts with other package
import 'package:pdd_flutter_dapp05/utils/constants.dart';
import 'package:pdd_flutter_dapp05/utils/helper.dart';
import 'package:pdd_flutter_dapp05/widgets/circ_progress_widget.dart';

import 'utils/globals.dart' as globals;

class RoutePage extends StatefulWidget {
  final String missionDetails;

  const RoutePage(this.missionDetails, {super.key});

  @override
  State<RoutePage> createState() {
    return _RoutePageState();
  }
}

const kDebugMode = true;

class _RoutePageState extends State<RoutePage> {
  final Completer<GoogleMapController> _googleMapController = Completer();

  // we keep an mission going off path counter
  // if the mission goes off path for 3 times we abort
  // when ever the mission goes off path we increment the counter
  // when mission comes in path we decrement the counter.
  // once it become 4 we abort the mission declaring it as failed.
  int offPathCounter = 0;

  bool _missionOver = false;
  bool _missionSuccess = false;
  Map<MarkerId, Marker> markers = {};
  Map<PolylineId, Polyline> polylines = {};
  List<LatLng> polylineCoordinates = [];
  PolylinePoints polylinePoints = PolylinePoints();

  List<maps_tk.LatLng> _polyPointsAsMapTk = [];

  final Location location = Location();
  late LocationData _currLoc;
  late LocationData _startLoc;
  late GoogleMapController mapController;

  // change this from algo chain later...
  late final LatLng _destLoc;
  late final int _appID;
  late final String _mnemonic;

  late Future _future = getDataForWidget();

  final googleAPIKey = "AIzaSyBZzbXVBmxZuSC2ceaU-GTa7cAY0pRFsZM";

  _addMarker(LatLng position, String id, BitmapDescriptor descriptor) {
    MarkerId markerId = MarkerId(id);
    Marker marker =
        Marker(markerId: markerId, icon: descriptor, position: position);
    markers[markerId] = marker;
  }

  _addPolyLine() {
    PolylineId id = const PolylineId("poly");
    Polyline polyline = Polyline(
        polylineId: id, color: Colors.red, points: polylineCoordinates);
    polylines[id] = polyline;
    //setState(() {});
  }

  _getPolyline() async {
    // following code works only with google account which has credit card linked
    PolylineResult result = await polylinePoints.getRouteBetweenCoordinates(
      googleAPIKey,
      PointLatLng(_startLoc.latitude!, _startLoc.longitude!),
      PointLatLng(_destLoc.latitude, _destLoc.longitude),
      travelMode: TravelMode.driving,
    );
    if (result.points.isNotEmpty) {
      for (var point in result.points) {
        polylineCoordinates.add(LatLng(point.latitude, point.longitude));
      }
    } else {
      // just fall back to start and dest points if goog dint bless !
      polylineCoordinates
          .add(LatLng(_startLoc.latitude ?? 0, _startLoc.longitude ?? 0));
      polylineCoordinates.add(_destLoc);
    }
    // store the polylineCoordinates to _polyPointsAsMapTk format for isPointInPath func
    for (var point in polylineCoordinates) {
      _polyPointsAsMapTk.add(maps_tk.LatLng(point.latitude, point.longitude));
    }
    _addPolyLine();
  }

  Widget _getMarker() {
    return Container(
      width: 40,
      height: 40,
      padding: const EdgeInsets.all(2),
      decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(100),
          boxShadow: const [
            BoxShadow(
                color: Colors.grey,
                offset: Offset(0, 3),
                spreadRadius: 4,
                blurRadius: 6)
          ]),
      child: Column(
        children: [
          Visibility(
            visible: !_missionOver,
            child: ClipOval(
              child: Image.asset("assets/mission_inprog.jpeg"),
            ),
          ),
          Visibility(
            visible: (_missionOver &&
                _missionSuccess), // Set visibility based on your condition
            child: ClipOval(
              child: Image.asset("assets/mission_success.jpeg"),
            ),
          ),
          Visibility(
            visible: (_missionOver &&
                !_missionSuccess), // Set visibility based on your condition
            child: ClipOval(
              child: Image.asset("assets/mission_fail.jpeg"),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> getDataForWidget() async {
    if (!_missionOver) {
      _currLoc = await location.getLocation();
      reportMissionStartAsync("started");
      // save this as the start loc to draw the polyline
      _startLoc = _currLoc;
      _setupLocationTracker();
      moveToPosition(LatLng(_startLoc.latitude ?? 0, _startLoc.longitude ?? 0));

      /// origin marker
      _addMarker(LatLng(_startLoc.latitude ?? 0, _startLoc.longitude ?? 0),
          "origin", BitmapDescriptor.defaultMarker);

      /// destination marker
      _addMarker(
          _destLoc, "destination", BitmapDescriptor.defaultMarkerWithHue(90));
      // draw route as well
      await _getPolyline();
    }
  }

  @override
  void initState() {
    super.initState();
    myLog("initState : " + widget.missionDetails);
    Map missionDetailsMap = jsonDecode(widget.missionDetails);
    double lat = double.parse(missionDetailsMap['lat']);
    double lon = double.parse(missionDetailsMap['lon']);
    _destLoc = LatLng(lat, lon);
    _appID = int.parse(missionDetailsMap['appid']);
    _mnemonic = missionDetailsMap['mnemonic'].toString();
    myLog("initState _appID: " + _appID.toString());
    _future = getDataForWidget();
  }

  void _setupLocationTracker() {
    location.onLocationChanged.listen((newLocation) {
      myLog(
          "onLocationChanged _missionOver : $_missionOver offPathCounter :$offPathCounter");
      if (!_missionOver) {
        myLog("onLocationChanged");
        // check whether we have moved a lot from prev loc...if so then send a loc update to cc
        double distMoved = calculateDistance(_currLoc.latitude,
            _currLoc.longitude, newLocation.latitude, newLocation.longitude);
        myLog("dist moved: $distMoved");
        if (distMoved >= LOC_REPORT_DIST_M) {
          // we have moved enuf...so report this new loc to CC
          _currLoc = newLocation;
          myLog("loc diff is $distMoved. Reporting new loc to CC");
          reportLocationUpdate("inprogress");
        }
        // first move to the loc and then do some checks on path etc
        moveToPosition(LatLng(_currLoc.latitude ?? 0, _currLoc.longitude ?? 0));
        // check whether this new location falls comfortably within the polyline drawn
        bool reachedDest = reachedDestination();
        myLog("reachedDest = $reachedDest");
        if (reachedDest) {
          // if reached dest report it back update and all
          _missionOver = true;
          _missionSuccess = true;
          setState(() {});
          reportFinalMissionData("success");
        } else {
          bool isPathOK = isMissionInPath();
          myLog("isPathOK = $isPathOK");
          if (isPathOK) {
            offPathCounter = 0;
          } else {
            offPathCounter++;
            if (offPathCounter > 3) {
              _missionOver = true;
              _missionSuccess = false;
              setState(() {});
              reportFinalMissionData("failed");
            }
          }
        }
      }
    });
  }

  bool reachedDestination() {
    // here we create a polyline latlon list with dest and a point near to dest with offsetNearDest
    // then use that polyline with currloc to check how near the currloc to dest polyline
    List<maps_tk.LatLng> polyLineDest = [];
    double offsetNearDest = 0.00001;
    polyLineDest.add(maps_tk.LatLng(_destLoc.latitude, _destLoc.longitude));
    polyLineDest.add(maps_tk.LatLng(_destLoc.latitude + offsetNearDest,
        _destLoc.longitude + offsetNearDest));

    return maps_tk.PolygonUtil.isLocationOnPath(
        maps_tk.LatLng(_currLoc.latitude!, _currLoc.longitude!),
        polyLineDest,
        false,
        tolerance: 1);
  }

  bool isMissionInPath() {
    return maps_tk.PolygonUtil.isLocationOnPath(
        maps_tk.LatLng(_currLoc.latitude!, _currLoc.longitude!),
        _polyPointsAsMapTk,
        false,
        tolerance: 10);
  }

  void reportLocationUpdate(String status) {
    if (_currLoc != null) {
      String latlonStr = "${_currLoc.latitude!},${_currLoc.longitude!}";
      myLog("reporting curr loc to CC appID: $_appID latlonStr: $latlonStr");
      final response = http.post(
        Uri.parse(globals.ccBaseUrl + '/api/update_loc'),
        body: jsonEncode(
            {'appid': _appID, 'latlon': latlonStr, 'mission_status': status}),
        headers: {'Content-Type': 'application/json'},
      );
    }
  }

  void reportStatusUpdate(String status) {
    final response = http.post(
      Uri.parse(globals.ccBaseUrl + '/api/update_status'),
      body: jsonEncode({'appid': _appID, 'status': status}),
      headers: {'Content-Type': 'application/json'},
    );
  }

  Future<void> reportMissionStartAsync(String status) async {
    myLog("reportMissionStartAsync to CC appID: ");

    if (_currLoc != null) {
      String latlonStr = "${_currLoc.latitude!},${_currLoc.longitude!}";
      myLog("reportMissionStartAsync to CC appID: $_appID latlonStr: $latlonStr");
      final response = await http.post(
        Uri.parse(globals.ccBaseUrl + '/api/update_loc'),
        body: jsonEncode(
            {'appid': _appID, 'latlon': latlonStr, 'mission_status': status}),
        headers: {'Content-Type': 'application/json'},
      );
    }
  }

  void reportFinalMissionData(String status) {
    // update the CC db
    // first report final loc
    reportLocationUpdate(status);
    // then status update
    reportStatusUpdate(status);
    // update BC
    updateBlockchain(status, _mnemonic, _currLoc.latitude.toString(),
        _currLoc.longitude.toString());
  }

  void updateBlockchain(
      String status, String mnemonic, String lat, String lon) {
    // call a set latlon with all the info
    myLog("updateBlockchain $status : $_mnemonic : $lat : $lon");
    setMissionStatusInChain(_appID, mnemonic, status, lat, lon);
  }

  moveToPosition(LatLng latLng) async {
    GoogleMapController mapController = await _googleMapController.future;
    myLog("moveToPosition ${latLng.latitude} : ${latLng.longitude}");
    mapController.animateCamera(CameraUpdate.newCameraPosition(
        CameraPosition(target: latLng, zoom: 19)));
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<void>(
        future: _future,
        builder: (BuildContext context, AsyncSnapshot<void> snapshot) {
          if (snapshot.connectionState == ConnectionState.done) {
            return MaterialApp(home: _buildBody());
          } else {
            return const MaterialApp(
              home: CircularProgressWidget(), // Use the custom widget
            );
          }
        });
  }

  Widget _buildBody() {
    return _getMap();
  }

  Widget _getMap() {
    return Stack(
      children: [
        GoogleMap(
          initialCameraPosition: CameraPosition(
            target: LatLng(_currLoc.latitude!, _currLoc.longitude!),
            zoom: 19.0,
          ),
          mapType: MapType.normal,
          onMapCreated: (GoogleMapController controller) {
            // now we need a variable to get the controller of google map
            _googleMapController.complete(controller);
          },
          markers: Set<Marker>.of(markers.values),
          polylines: Set<Polyline>.of(polylines.values),
        ),
        Positioned.fill(
            child: Align(alignment: Alignment.center, child: _getMarker()))
      ],
    );
  }
}
