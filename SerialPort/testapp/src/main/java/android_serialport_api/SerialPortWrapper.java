package android_serialport_api;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口包装类
 * @author zhaocheng
 */
public class SerialPortWrapper {

    public interface SerialPortListener {
        void onDataReceived(byte[] data, int size);
    }

    private final String TAG = "SerialPortWrapper";

    private SerialPortListener mSerialPortListener;

    private SerialPort mSerialPort;

    private InputStream mInputStream;

    private OutputStream mOutputStream;

    private String DEVICEPATH;

    private int BAUDRATE;

    private boolean RECEIVE;

    public void setSerialPortListener(SerialPortListener serialPortListener) {
        mSerialPortListener = serialPortListener;
    }

    public SerialPortWrapper(String devicePath, int baudRate) {
        DEVICEPATH = devicePath;
        BAUDRATE = baudRate;
    }

    public void open() {
        if (RECEIVE == true) return;
        try {
            mSerialPort = new SerialPort(new File(DEVICEPATH), BAUDRATE, 0);
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();
            new Thread() {
                @Override
                public void run() {
                    if (mInputStream == null || mOutputStream == null) {
                        Log.i(TAG, "open serialport " + DEVICEPATH + " fail");
                        return;
                    } else {
                        Log.i(TAG, "open serialport " + DEVICEPATH + " success");
                    }
                    RECEIVE = true;
                    while (RECEIVE) {
                        try {
                            if (mInputStream == null) {
                                RECEIVE = false;
                                return;
                            }
                            int size = mInputStream.available();
                            if (size > 0) {
                                byte[] data = new byte[size];
                                mInputStream.read(data, 0, size);
                                if (mSerialPortListener != null) {
                                    Log.i(TAG, "onDataReceived:" + size);
                                    mSerialPortListener.onDataReceived(data, size);
                                }
                            }
                            Thread.sleep(30);
                        } catch (Exception e) {
                            RECEIVE = false;
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] msg) {
        if(mOutputStream == null) return;
        try {
            this.mOutputStream.write(msg);
            this.mOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        RECEIVE = false;
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mSerialPort != null) {
                mSerialPort.close();
                Log.i(TAG, "close serialport " + DEVICEPATH + " success");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
