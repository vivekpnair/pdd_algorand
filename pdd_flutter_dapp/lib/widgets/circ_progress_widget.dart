import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:pdd_flutter_dapp05/utils/constants.dart';

class CircularProgressWidget extends StatelessWidget {
  const CircularProgressWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(APP_NAME),
      ),
      body: const Center(
        child: CircularProgressIndicator(), // Use CircularProgressIndicator
      ),
    );
  }
}