package com.example.audiorecordtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lena on 15/11/27.
 */
public class SQLiteEx {

    private final static String DB_NAME="recordtest.db"; // DB名
    private final static String DB_TABLE="record_data"; // テーブル名
    private final static int    DB_VERSION=1;           // バージョン
    private SQLiteDatabase db;                          // データベースオブジェクト

    // データベースのデータ用フィールド /////////////////////
    public int id;
    public String date;
    public String filename; // path:/storage/emulated/0/filename
    public int recordtime;

    // コンストラクタ ////////////////////////////////////
    public SQLiteEx(Context context){
        // データベースオブジェクトの取得
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // データベースヘルパーの定義 //////////////////////////
    private static class DBHelper extends SQLiteOpenHelper{
        // データベースヘルパーのコンストラクタ
        public DBHelper(Context context) {
            super(context,DB_NAME,null,DB_VERSION);
        }

        // データベースの生成
        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(
                "create table if not exists "
                + DB_TABLE
                + " (id integer PRIMARY KEY AUTOINCREMENT NOT NULL, date DATE, filename TEXT, recordtime INT)"
            );
        }

        // データベースのアップグレード
        @Override
        public void onUpgrade(SQLiteDatabase db,
              int oldVersion, int newVersion) {
            db.execSQL("drop table if exists "+ DB_TABLE);
            onCreate(db);
        }
    }

    // データベースへの書き込み ////////////////////////////

    // 全部書き込み
    public void doAddRecord(int _id, String _date, String _filename){
        ContentValues values = new ContentValues();
        values.put("id",_id);
        values.put("date",_date);
        values.put("filename",_filename);
        int colNum = db.update(DB_TABLE,values,null,null);
        if(colNum == 0) db.insert(DB_TABLE,"",values);
        this.id = _id;
        this.date = _date;
        this.filename = _filename;
    }

    // IDの書き込み
    public void write_id(int info) throws Exception {
        ContentValues values = new ContentValues();
        values.put("id",info);
        int colNum = db.update(DB_TABLE,values,null,null);
        if(colNum == 0) db.insert(DB_TABLE,"",values);
        this.id = info;
    }

    // 日付の書き込み
    public void write_date(String info) throws Exception {
        ContentValues values = new ContentValues();
        values.put("date",info);
        int colNum = db.update(DB_TABLE,values,null,null);
        if(colNum == 0) db.insert(DB_TABLE,"",values);
        this.date = info;
    }

    // ファイル名の書き込み
    public void write_filename(String info) throws Exception {
        ContentValues values = new ContentValues();
        values.put("filename", info);
        int colNum = db.update(DB_TABLE, values, null, null);
        if(colNum == 0) db.insert(DB_TABLE,"",values);
        this.filename = info;
    }

    // 録音時間の書き込み
    public void recordtime(int info) throws Exception {
        ContentValues values = new ContentValues();
        values.put("recordtime", info);
        int colNum = db.update(DB_TABLE, values, null, null);
        if(colNum == 0) db.insert(DB_TABLE,"",values);
        this.recordtime = info;
    }

    // データベースからの読み込み //////////////////////////

    // IDの読み込み
    public int load_id(int _id) throws Exception {
        // Cursorを確実にcloseするために, try{}~finally{}にする
        Cursor c = null;
        String[] selectId = {""+_id};
        try {
            // query(テーブル名, String[]{カラム名}, 検索条件, 検索条件のパラメータ（？で指定）に置き換わる値を指定,
            // groupBy句, having句, orderBy句, 検索結果の上限レコードを数を指定
            c = db.query(DB_TABLE, new String[]{"id", "date", "filename"}, "id=?", selectId, null, null, null);
            if (c.getCount() == 0) throw new Exception();
            c.moveToFirst();
            int id = c.getInt(0);
            c.close();
            this.id = id;
            return id;
        }finally {
            // Cursorを忘れずにcloseする
            if(c != null){
                c.close();
            }
        }
    }

    // 日付の読み込み
    public String load_date(int _id) throws Exception {
        // Cursorを確実にcloseするために, try{}~finally{}にする
        Cursor c = null;
        String[] selectId = {""+_id};
        try {
            // DB_TABLE
            c = db.query(DB_TABLE, new String[]{"id", "date", "filename"}, "id=?", selectId, null, null, null);
            if (c.getCount() == 0) throw new Exception();
            c.moveToFirst();
            String date = c.getString(1);
            c.close();
            this.date = date;
            return date;
        } finally {
            // Cursorを忘れずにcloseする
            if(c != null){
                c.close();
            }
        }
    }

    public String load_filename(int _id) throws Exception {
        // Cursorを確実にcloseするために, try{}~finally{}にする
        Cursor c = null;
        String[] selectId = {""+_id};
        try {
            c = db.query(DB_TABLE, new String[]{"id", "date", "filename"}, "id=?", selectId, null, null, null);
            if (c.getCount() == 0) throw new Exception();
            c.moveToFirst();
            String filename = c.getString(2);
            c.close();
            this.filename = filename;
            return filename;
        }finally {
            // Cursorを忘れずにcloseする
            if(c != null){
                c.close();
            }
        }
    }
}
