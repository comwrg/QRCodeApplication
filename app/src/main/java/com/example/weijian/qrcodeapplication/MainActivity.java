package com.example.weijian.qrcodeapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    int REQUEST_CODE = 1;

    TextView textView, time;
    EditText count, address, weigth;
    Spinner userName;


    private WritableWorkbook wwb;
    private String excelPath;
    private File excelFile;

    MyDBHelper dbHelper;
    List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new MyDBHelper(this, "UserStore.db", null, 1);
        list = new ArrayList<>();
        innitView();
        getCameraPermission();
        ZXingLibrary.initDisplayOpinion(this);


        textView = (TextView) findViewById(R.id.text);
        Button qrButton = (Button) findViewById(R.id.qrcode);
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivityForResult(intent, 200);
            }
        });
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);


            }
        });
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = userName.getSelectedItem().toString();
                excelPath = getExcelDir() + File.separator + fileName + ".xls";
                excelFile = new File(excelPath);
//                Intent intent = new Intent("android.intent.action.VIEW");
//                intent.addCategory("android.intent.category.DEFAULT");
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = getUriForFile(MainActivity.this, excelFile);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.setDataAndType(uri, "application/xls");
                try {
//                    startActivity(intent);
                    excelFile = new File(new URI(uri.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                createExcel(excelFile, "数量", "订单生成时间", "订单号", "寄件人", "地址", "重量");
                writeToExcel(count.getText().toString(), time.getText().toString(),
                        textView.getText().toString(), fileName, address.getText().toString(), weigth.getText().toString());
                textView.setText("");
                count.setText("");
                address.setText("");
                weigth.setText("");
                Toast.makeText(MainActivity.this, "保存成功！", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == 200) {
            getUserData();
            try {
                ArrayAdapter apecadapter = new ArrayAdapter<String>(this, R.layout.spiner_item, list);
                userName.setAdapter(apecadapter);
            } catch (Exception ee) {

            }
        }
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "解析结果:" + result);
                    setIime();
                    textView.setText(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    public void innitView() {
        count = (EditText) findViewById(R.id.count);
        address = (EditText) findViewById(R.id.address);
        weigth = (EditText) findViewById(R.id.weight);
        time = (TextView) findViewById(R.id.time);
        userName = (Spinner) findViewById(R.id.userName);
        getUserData();
        try {
            ArrayAdapter apecadapter = new ArrayAdapter<String>(this, R.layout.spiner_item, list);
            userName.setAdapter(apecadapter);
        } catch (Exception ee) {

        }
        setIime();
    }

    public void setIime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        time.setText(simpleDateFormat.format(date));
    }

    public void getCameraPermission() {
        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //先判断有没有权限 ，没有就在这里进行权限的申请
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.CAMERA}, 2);
            } else {
                //说明已经获取到摄像头权限了 想干嘛干嘛
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //先判断有没有权限 ，没有就在这里进行权限的申请
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            } else {
                //说明已经获取到摄像头权限了 想干嘛干嘛
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //先判断有没有权限 ，没有就在这里进行权限的申请
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            } else {
                //说明已经获取到摄像头权限了 想干嘛干嘛
            }

        } else {
            //这个说明系统版本在6.0之下，不需要动态获取权限。
        }
    }

    // 创建excel表.
    public void createExcel(File file, String count, String time, String orderNum, String userName, String address, String weight) {
        WritableSheet ws = null;
        try {
            if (!file.exists()) {
                wwb = Workbook.createWorkbook(file);

                ws = wwb.createSheet("sheet1", 0);
                // 在指定单元格插入数据
                Label lbl1 = new Label(0, 0, count);
                Label bll2 = new Label(1, 0, time);
                Label bll3 = new Label(2, 0, orderNum);
                Label bll4 = new Label(3, 0, userName);
                Label bll5 = new Label(4, 0, address);
                Label bll6 = new Label(5, 0, weight);

                ws.addCell(lbl1);
                ws.addCell(bll2);
                ws.addCell(bll3);
                ws.addCell(bll4);
                ws.addCell(bll5);
                ws.addCell(bll6);


                // 从内存中写入文件中
                wwb.write();
                wwb.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeToExcel(String count, String time, String orderNum, String userName, String address, String weight) {

        try {
            Workbook oldWwb = Workbook.getWorkbook(excelFile);
            wwb = Workbook.createWorkbook(excelFile,
                    oldWwb);
            WritableSheet ws = wwb.getSheet(0);
            // 当前行数
            int row = ws.getRows();
            Label lbl1 = new Label(0, row, count);
            Label bll2 = new Label(1, row, time);
            Label bll3 = new Label(2, row, orderNum);
            Label bll4 = new Label(3, row, userName);
            Label bll5 = new Label(4, row, address);
            Label bll6 = new Label(5, row, weight);

            ws.addCell(lbl1);
            ws.addCell(bll2);
            ws.addCell(bll3);
            ws.addCell(bll4);
            ws.addCell(bll5);
            ws.addCell(bll6);

            // 从内存中写入文件中,只能刷一次.
            wwb.write();
            wwb.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void readExcel() {
        try {
            Workbook workbook = Workbook.getWorkbook(excelFile);
            //获取第一个工作表的对象
            Sheet sheet = workbook.getSheet(0);
            //获取第一列第一行的的单元格
            Cell cell = sheet.getCell(0, 0);
            //获取单元格中的内容
            String body = cell.getContents();
            Log.d("bb", body);
            //读取数据关闭
            workbook.close();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }

    // 获取Excel文件夹
    public String getExcelDir() {
        // SD卡指定文件夹
        String sdcardPath = Environment.getExternalStorageDirectory()
                .toString();
        File dir = new File(sdcardPath + File.separator + "Excel"
                + File.separator + "Person");

        if (dir.exists()) {
            return dir.toString();

        } else {
            dir.mkdirs();
            Log.d("BAG", "保存路径不存在,");
            return dir.toString();
        }
    }


    //  获取数据
    public void getUserData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String Query = "Select * from userData";
        Cursor cursor = db.rawQuery(Query, new String[]{});
        String title;
        list.clear();

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int titleIndex = cursor.getColumnIndex("name");
                title = cursor.getString(titleIndex);
                list.add(title);

            }
            cursor.close();
        }
    }



    public static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, "com.example.weijian.qrcodeapplication.fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}
