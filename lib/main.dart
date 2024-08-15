
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SMS Forwarder',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('SMS Forwarder'),
      ),
      body: Center(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Welcome to SMS Forwarder',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 16),
            Text(
              'This app forwards incoming SMS messages to a specified URL.',
              style: TextStyle(fontSize: 16),
            ),
            SizedBox(height: 16),
            Text(
              'How to Use:',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 8),
            Text(
              '1. Set up app permissions for your phone after installation.',
              style: TextStyle(fontSize: 16),
            ),
            Text(
              '2. Set the sender phone number or name and the URL to which the SMS should be forwarded.',
              style: TextStyle(fontSize: 16),
            ),
            Text(
              '3. Use * as the sender name to forward any SMS to the URL.',
              style: TextStyle(fontSize: 16),
            ),
            Text(
              '4. Press the Test button to make a test request to the server.',
              style: TextStyle(fontSize: 16),
            ),
            Text(
              '5. Press the Syslog button to view errors stored in the Logcat.',
              style: TextStyle(fontSize: 16),
            ),
          ],
        ),
      ),
    );
  }
}
