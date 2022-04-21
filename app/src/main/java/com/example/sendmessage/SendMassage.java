package com.example.sendmessage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class SendMassage extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    private IntentFilter sendFilter;
    private SendStatusReceiver sendStatusReceiver;
    private File picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_massage);
        EditText message = (EditText) findViewById(R.id.message);
        Button send = (Button) findViewById(R.id.send);
        Button photo = (Button) findViewById(R.id.photo);
        Intent intent = getIntent();
        String phone = intent.getStringExtra("phone");

        //动态注册接收短信
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(100);
        MessageReceiver messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, intentFilter);

        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();



        //为发送信息按钮添加点击事件
        /*Log.d("adf",phone);*/
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //发送短信
                //获得广播接收器实例和IntentFilter实例
                sendStatusReceiver = new SendStatusReceiver();
                sendFilter = new IntentFilter();
                sendFilter.addAction("SENT_SMS_ACTION");
                //注册广播监听
                registerReceiver(sendStatusReceiver, sendFilter);
                //构造PendingIntent启动短信发送状态监控广播
                Intent sendIntent = new Intent("SENT_SMS_ACTION");
                PendingIntent pi = PendingIntent.getBroadcast(SendMassage.this, 0, sendIntent, 0);

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone, null,
                        message.getText().toString(), pi, null);
            }
        });


        //为拍照按钮添加点击事件
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //调用系统相机
                String fileName = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                picture = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath()+"/service",fileName);
                Uri imageUri = Uri.fromFile(picture);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);          //直接使用，没有缩小
                startActivityForResult(intent, TAKE_PHOTO );// 100 是请求码


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int result,Intent data) {
        super.onActivityResult(requestCode, result, data);
        //保存图片到本地相册
        if(result==-1){    //result 返回的是-1 表示拍照成功 返回的是 0 表示拍照失败
            Uri uri = Uri.fromFile(picture);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(uri);
            this.sendBroadcast(intent);  // 这里我们发送广播让MediaScanner 扫描我们制定的文件
            // 这样在系统的相册中我们就可以找到我们拍摄的照片了
        }
    }


    //发送短信的广播类 判断是否发送成功
    class SendStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == RESULT_OK){
                Toast.makeText(context, "successful", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册的广播
        unregisterReceiver(sendStatusReceiver);
    }


}