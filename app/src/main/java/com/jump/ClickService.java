package com.jump;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.OutputStream;

public class ClickService extends Service {

    private static final String TAG = "ClickService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void start() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    screenCap();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    exec(getDis());
                }
            }
        }).start();
    }

    private int getDis() {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Mat edgeMat = new Mat();

        BitmapFactory.Options opts = new BitmapFactory.Options();//保证图片为原尺寸
        opts.inScaled = false;

        String path = Environment.getExternalStorageDirectory() + "/01.png";
        Bitmap srcBitmap = BitmapFactory.decodeFile(path, opts);
        Utils.bitmapToMat(srcBitmap, rgbMat);
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(grayMat, edgeMat, 20, 100);

        //起始点（x1，y1）
        int x1 = 0;
        int y1 = 100000;
        for (int i = 400; i < grayMat.rows() - 200; i++) {
            for (int j = 50; j < grayMat.cols() - 50; j++) {
                if (grayMat.get(i, j)[0] < 60 && grayMat.get(i + 150, j)[0] < 60 && y1 > i) {
                    y1 = i;
                    x1 = j;
                }
            }
        }

        //终点（x2，y2）
        int x2 = 0;
        int y2 = 100000;
        for (int i = 400; i < edgeMat.rows() - 200; i++) {
            for (int j = 50; j < edgeMat.cols() - 50; j++) {
                if (edgeMat.get(i, j)[0] == 255 && y2 > i) {
                    y2 = i;
                    x2 = j;
                }
            }
        }
        //修正位置
        y1 = y1 + 200;

        int dis = (int) (Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) * 1.35);
        dis=dis>1500?1500:dis;
        Log.e(TAG, "onClick: " + x1 + "aaa" + y1 + "aaa" + x2 + "aaa" + y2 + "aaa" + dis);
        return dis;
    }


    public static void exec(int dis) {

        //破解反外挂机制
        int aa = (int) (500 + 300 * Math.random());
        int bb = (int) (1360 + 140 * Math.random());
//        String cmd = "input tap 125 340 \n";
        String cmd = "input swipe" + " " + aa + " " + bb + " " + aa + " " + bb + " " + dis + " " + "\n";
        try {
            OutputStream os = Runtime.getRuntime().exec("su").getOutputStream();
            os.write(cmd.getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void screenCap() {
        try {
            OutputStream os = Runtime.getRuntime().exec("su").getOutputStream();
            String cmd = "screencap -p /sdcard/01.png";
            os.write(cmd.getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
