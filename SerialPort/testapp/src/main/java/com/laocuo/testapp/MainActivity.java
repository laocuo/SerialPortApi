package com.laocuo.testapp;

import android_serialport_api.SerialPortWrapper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SerialPortWrapper.SerialPortListener {

    // /dev/ttysWK2 /dev/ttysWK0
    private final String DEVICE_PATH = "/dev/ttysWK2";

    private TextView mTextView;

    private Button mStart, mStop;

    private SerialPortWrapper mSerialPortWrapper;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                String content = (String) msg.obj;
                mTextView.setText(content);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        mTextView = findViewById(R.id.sample_text);
        mStart = findViewById(R.id.start);
        mStart.setOnClickListener(this);
        mStop = findViewById(R.id.stop);
        mStop.setOnClickListener(this);

        mSerialPortWrapper = new SerialPortWrapper(DEVICE_PATH, 115200);
        mSerialPortWrapper.setSerialPortListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSerialPortWrapper.close();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                mSerialPortWrapper.open();
                break;
            case R.id.stop:
                mSerialPortWrapper.close();
                break;
        }
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return "";
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    @Override
    public void onDataReceived(byte[] data, int size) {
        String content = bytesToHexString(data);
        Log.i("zhaocheng", "onDataReceived:" + content);
        Message message = mHandler.obtainMessage(1);
        message.obj = content;
        mHandler.sendMessage(message);
    }
}
