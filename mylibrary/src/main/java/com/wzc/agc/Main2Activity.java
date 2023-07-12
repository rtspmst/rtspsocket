package com.wzc.agc;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = Main2Activity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//                } else {
////                doAgc();
//                }
//        }
    }

    /**
     * 开始测试agc
     * 源文件在工程目录raw目录下 test_input.pcm
     * 输入文件位于手机根目录下 /agc_out.pcm
     */
//    private void doAgc() {
//        Toast.makeText(this, "测试开始", Toast.LENGTH_LONG).show();
//        try {
//            AgcUtils agcUtils = new AgcUtils();
//            agcUtils.setAgcConfig(0, 15, 1).prepare();
//
//            InputStream fInt = getResources().openRawResource(R.raw.test0);
//            FileOutputStream fOut = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/agc_out300.pcm");
//            byte[] buffer = new byte[160];
//            int bytes;
//            int micOutLevel = 0;
//
//            Log.e(TAG, "doAgc: 11111111111111111111" );
//
//            while ((bytes = fInt.read(buffer)) != -1) {
//
//                short[] data = new short[80];
//                short[] outData = new short[80];
//                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(data);
//                int status = agcUtils.agcProcess(data, 0, 80, outData, 0, micOutLevel, 0, 1);
//
//
//                Log.e(TAG, "agc 555555555555555555555status =  " + status);
//                fOut.write(shortArrayToByteArray(outData));
//            }
//
//            fInt.close();
//            fOut.close();
//
//            Toast.makeText(this, "测试结束，输出文件位于手机根目录下/agc_out.pcm", Toast.LENGTH_LONG).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    //short array to byte array
    public byte[] shortArrayToByteArray(short[] data) {
        byte[] byteVal = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            byteVal[i * 2] = (byte) (data[i] & 0xff);
            byteVal[i * 2 + 1] = (byte) ((data[i] & 0xff00) >> 8);
        }
        return byteVal;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;   //发现有未通过权限
                    break;
                }
            }
        }
        if (hasPermissionDismiss) {
        } else {
//            doAgc();
        }
    }

}
