package com.example.weijian.qrcodeapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText userName;
    MyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbHelper = new MyDBHelper(this, "UserStore.db", null, 1);
        innitView();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.userRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFatal()) {
                    if (CheckIsDataAlreadyInDBorNot(userName.getText().toString())) {
                        Toast.makeText(RegisterActivity.this, "该用户名已被注册，注册失败", Toast.LENGTH_SHORT).show();
                    } else {

                        if (register(userName.getText().toString())) {
                            Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                            setResult(200);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "注册失败！", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            }
        });
    }

    //    初始化界面
    public void innitView() {
        userName = (EditText) findViewById(R.id.userName);
    }

    // 校验数据合法性
    public boolean isFatal() {
        if (userName.getText().toString().equals("")) {
            Toast.makeText(this, "请输入完整信息，再进行注册！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //检验用户名是否已存在
    public boolean CheckIsDataAlreadyInDBorNot(String value) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String Query = "Select * from userData where name =?";
        Cursor cursor = db.rawQuery(Query, new String[]{value});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }


    //向数据库插入数据
    public boolean register(String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        /*String sql = "insert into userData(name,password) value(?,?)";
        Object obj[]={username,password};
        db.execSQL(sql,obj);*/
        ContentValues values = new ContentValues();
        values.put("name", username);
        ;
        db.insert("userData", null, values);
        db.close();
        //db.execSQL("insert into userData (name,password) values (?,?)",new String[]{username,password});
        return true;
    }

}
