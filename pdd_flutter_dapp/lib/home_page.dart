import 'dart:convert';

import 'package:algorand_dart/algorand_dart.dart';
import 'package:flutter/material.dart';
import 'package:pdd_flutter_dapp05/route_page.dart';
import 'package:pdd_flutter_dapp05/utils/constants.dart';
import 'package:pdd_flutter_dapp05/utils/helper.dart';

class HomePage extends StatelessWidget {
  final String missionDetails;
  const HomePage({super.key, required this.missionDetails});

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: AppBar(
          title: const Text(APP_NAME),
        ),
        body: Center(
          child: ElevatedButton(
            onPressed: () => Navigator.of(context).push(MaterialPageRoute(
                builder: (context) =>
                    GetMissionData(missionDetails: missionDetails))),
            child: const Text(RETRIEVE_MISSION_DATA_BTN),
          ),
        ),
      ),
    );
  }
}

class GetMissionData extends StatelessWidget {
  final String missionDetails;
  const GetMissionData({super.key, required this.missionDetails});

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: AppBar(
          title: const Text(APP_NAME),
        ),
        body: FutureBuilder(
          builder: (ctx, snapshot) {
            // Checking if future is resolved or not
            if (snapshot.connectionState == ConnectionState.done) {
              // If we got an error
              if (snapshot.hasError) {
                return Center(
                  child: Text(
                    '${snapshot.error} occurred',
                    style: TextStyle(fontSize: 18),
                  ),
                );

                // if we got our data
              } else if (snapshot.hasData) {
                // Extracting data from snapshot object
                final data = snapshot.data as String;
                return Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        '$data',
                        style: const TextStyle(fontSize: 18),
                      ),
                      const SizedBox(height: 16), // Adding some spacing
                      ElevatedButton(
                        onPressed: () {
                          // Button action logic
                          Navigator.of(context).push(MaterialPageRoute(
                              builder: (context) =>
                                  RoutePage(snapshot.data.toString())));
                        },
                        child: const Text(START_MISSION_BTN),
                      ),
                    ],
                  ),
                );
              }
            }

            // Displaying LoadingSpinner to indicate waiting state
            return const Center(
              child: CircularProgressIndicator(),
            );
          },
          // Future that needs to be resolved
          // inorder to display something on the Canvas
          future: getMissionDataFromChain(missionDetails),
        ),
      ),
    );
  }
}
