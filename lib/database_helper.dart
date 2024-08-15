
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  factory DatabaseHelper() => _instance;

  static Database? _database;

  DatabaseHelper._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), 'sms_forwarder.db');
    return await openDatabase(
      path,
      version: 1,
      onCreate: _onCreate,
    );
  }

  Future _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE sms_history (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        sender TEXT,
        messageBody TEXT,
        timestamp INTEGER
      )
    ''');
  }

  Future<int> insertSmsHistory(Map<String, dynamic> row) async {
    Database db = await database;
    return await db.insert('sms_history', row);
  }

  Future<List<Map<String, dynamic>>> queryAllSmsHistory() async {
    Database db = await database;
    return await db.query('sms_history');
  }
}
