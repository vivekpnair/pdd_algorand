import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:pdd_flutter_dapp05/home_page.dart';
import 'package:pdd_flutter_dapp05/utils/constants.dart';

class LoginScreen extends StatefulWidget {
  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  String username = '';
  String password = '';
  String missionDetails = '';

  Future<void> login() async {
    // Make the REST API call
    final response = await http.post(
      Uri.parse(CC_REST_API_URL + '/api/get_miss_det'),
      body: jsonEncode({'email': username, 'access_code': password}),
      headers: {'Content-Type': 'application/json'},
    );

// Handle the response
    if (response.statusCode == 200) {
      myLog(response.body.toString());
      Map latlonDetMap = jsonDecode(response.body);
      if(latlonDetMap.isNotEmpty){
        if (latlonDetMap['ret_code'] == 200){
          setState(() {
            missionDetails = response.body.toString();
          });
          // Navigate to another widget passing the result as a parameter
          Navigator.of(context).push(MaterialPageRoute(builder: (context) => HomePage(missionDetails: missionDetails)));
        } else{
          setState(() {
            missionDetails = 'Error: ${latlonDetMap['ret_code']}';
          });
        }
      }
    } else {
      setState(() {
        missionDetails = 'Error: ${response.statusCode}';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text(APP_NAME)),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(
              onChanged: (value) => setState(() => username = value),
              decoration: const InputDecoration(labelText: 'E-mail'),
            ),
            TextField(
              onChanged: (value) => setState(() => password = value),
              decoration: const InputDecoration(labelText: 'Access Code'),
              obscureText: true,
            ),
            const SizedBox(height: 16.0),
            ElevatedButton(
              onPressed: login,
              child: Text('Login'),
            ),
            SizedBox(height: 16.0),
            Text(missionDetails),
          ],
        ),
      ),
    );
  }
}