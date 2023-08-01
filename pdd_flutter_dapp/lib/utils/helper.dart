
import 'dart:convert';
import 'dart:math';

import 'package:algorand_dart/algorand_dart.dart';

import 'constants.dart';

Future<Account> getAccount(mnemonic) async {
  return Account.fromSeedPhrase(mnemonic.split(' '));
}

AbiMethod? getLatLonMethodObj() {
  // this does not need contract.json
  Argument arg = Argument(name: "sender", type: "string", description: "");
  List<Argument> args = [arg];
  Map<String, dynamic> retMap = {'type': 'string'};
  Returns ret = Returns.fromJson(retMap);
  return AbiMethod(
      name: "get_latlon", description: "", arguments: args, returns: ret);
}

void setMissionStatusInChain(int appID, String mnemonic, String status, String lat, String lon) async {
  final options = AlgorandOptions(
    algodClient: AlgodClient(
      apiUrl: "https://testnet-api.algonode.cloud/",
    ),
    indexerClient: IndexerClient(
      apiUrl: "https://testnet-idx.algonode.cloud",
    ),
  );

  final algorand = Algorand(options: options);
  final params = await algorand.getSuggestedTransactionParams();
  final account = await getAccount(mnemonic);
  final atc = AtomicTransactionComposer();
  await atc.addMethodCall(MethodCallParams(
    applicationId: appID,
    sender: account.address,
    method: setLatLonMethodObj(),
    params: params,
    signer: account,
    methodArgs: [status,account.publicAddress,lat,lon],
  ));

  // Run the transaction and wait for the results
  final result = await atc.execute(algorand, waitRounds: 4);

  // Print out the results
  final resStr = result.methodResults[0].value.toString();
  myLog("setMissionStatusInChain : $resStr");
}

Future<String> getMissionDataFromChain(String missionDetails) async {
  final options = AlgorandOptions(
    algodClient: AlgodClient(
      apiUrl: "https://testnet-api.algonode.cloud/",
    ),
    indexerClient: IndexerClient(
      apiUrl: "https://testnet-idx.algonode.cloud",
    ),
  );

  myLog(missionDetails);
  Map<String, dynamic> missionDetailsJson = jsonDecode(missionDetails);

  final algorand = Algorand(options: options);
  final params = await algorand.getSuggestedTransactionParams();
  final account = await getAccount(missionDetailsJson['mnemonic'].toString());
  final int appId = int.parse(missionDetailsJson['app_id'].toString());
  final atc = AtomicTransactionComposer();
  await atc.addMethodCall(MethodCallParams(
    applicationId: appId,
    sender: account.address,
    method: getLatLonMethodObj(),
    params: params,
    signer: account,
    methodArgs: [account.publicAddress],
  ));

  // Run the transaction and wait for the results
  final result = await atc.execute(algorand, waitRounds: 4);

  // Print out the results
  final resStr = result.methodResults[0].value.toString();
  Map<String, dynamic> resStrJson = jsonDecode(resStr);
  // add app id as well to use in route map page
  resStrJson["appid"] = appId.toString();
  resStrJson["mnemonic"] = missionDetailsJson['mnemonic'].toString();

  return jsonEncode(resStrJson);
}

AbiMethod? setLatLonMethodObj() {
  // this does not need contract.json
  Argument arg1 = Argument(name: "status", type: "string", description: "");
  Argument arg2 = Argument(name: "tgt_addr", type: "string", description: "");
  Argument arg3 = Argument(name: "lat", type: "string", description: "");
  Argument arg4 = Argument(name: "lon", type: "string", description: "");

  List<Argument> args = [arg1,arg2,arg3,arg4];
  Map<String, dynamic> retMap = {'type': 'string'};
  Returns ret = Returns.fromJson(retMap);
  return AbiMethod(
      name: "set_latlon", description: "", arguments: args, returns: ret);
}

double calculateDistance(lat1, lon1, lat2, lon2){
  var ret = 0.0;
  var p = 0.017453292519943295;
  var a = 0.5 - cos((lat2 - lat1) * p)/2 +
      cos(lat1 * p) * cos(lat2 * p) *
          (1 - cos((lon2 - lon1) * p))/2;
  ret = 1000 * 12742 * asin(sqrt(a)); // in meters
  return ret;
}