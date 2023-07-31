import 'package:flutter/material.dart';
import 'package:pdd_flutter_dapp05/utils/constants.dart';

import 'login.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: APP_NAME,

      // to hide debug banner
      debugShowCheckedModeBanner: false,
      home: LoginScreen(),
    );
  }
}