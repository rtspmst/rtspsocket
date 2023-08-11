package org.easydarwin;


public class LanguageTr {


    //一键自检弹窗语言选择 0为中文 1为印尼语 2为英语
    public static int anInt0 = 0;


    public static String  AUDIO_CONNECTION_FAILED = "Audio connection failed, please check !";
    public static String  COMMUNICATION_FAILED = "Communication connection failed, please check !";
    public static String  CONTROL_BOARD_CONNECTION_FAILED = "Control board connection failed, please check !";


    public static void setAnInt(int anInt){
        anInt0 = anInt;
        switch (anInt0) {
            case 0:
            case 1:
                switchEnglish();
                break;
            case 2:
                switchIndonesian();
                break;
        }

    }

    private static void switchIndonesian() {

        AUDIO_CONNECTION_FAILED = "Koneksi audio gagal, silakan periksa koneksi perangkat!";
        COMMUNICATION_FAILED = "Koneksi komunikasi gagal, silakan periksa koneksi perangkat!";
        CONTROL_BOARD_CONNECTION_FAILED = "Koneksi papan kontrol utama gagal, silakan periksa koneksi perangkat!";
    }


    private static void switchEnglish() {

        AUDIO_CONNECTION_FAILED = "Audio connection failed, please check !";
        COMMUNICATION_FAILED = "Communication connection failed, please check !";
        CONTROL_BOARD_CONNECTION_FAILED = "Control board connection failed, please check !";
    }
}
