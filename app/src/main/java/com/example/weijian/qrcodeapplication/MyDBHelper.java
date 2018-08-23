package com.example.weijian.qrcodeapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by guchangyou on 2017/12/30.
 */
//数据库操作类
public class MyDBHelper extends SQLiteOpenHelper {

    public static final String CREATE_USERDATA = "create table userData(" +
            "id integer primary key autoincrement,"
            + "name text)";
    private Context mContext;

    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(context, name, cursorFactory, version);
        mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERDATA);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onCreate(db);
    }

}