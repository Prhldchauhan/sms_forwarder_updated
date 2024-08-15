
import 'dart:convert';
import 'package:path_provider/path_provider.dart';
import 'dart:io';

class ConfigHelper {
  static final ConfigHelper _instance = ConfigHelper._internal();
  factory ConfigHelper() => _instance;

  ConfigHelper._internal();

  Future<String> get _localPath async {
    final directory = await getApplicationDocumentsDirectory();
    return directory.path;
  }

  Future<File> get _localFile async {
    final path = await _localPath;
    return File('$path/config.json');
  }

  Future<File> writeConfig(Map<String, dynamic> config) async {
    final file = await _localFile;
    return file.writeAsString(json.encode(config));
  }

  Future<Map<String, dynamic>> readConfig() async {
    try {
      final file = await _localFile;
      final contents = await file.readAsString();
      return json.decode(contents);
    } catch (e) {
      return {};
    }
  }
}
