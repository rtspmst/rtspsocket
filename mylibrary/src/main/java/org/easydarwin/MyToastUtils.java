package org.easydarwin;


import com.blankj.utilcode.util.ToastUtils;

public class MyToastUtils {

    public static boolean isShow = true;

    public static void show(String info) {
        ToastUtils.showShort(info);
    }

    public static void showToast(int i, String toastInfo) {

        if (isShow) {

            switch (i) {

                case CV.TOAST_TAG1:

                    ToastUtils.setGravity(1, 1, -200);

                    break;
                case CV.TOAST_TAG2:

                    ToastUtils.setGravity(1, 1, -100);

                    break;
                case CV.TOAST_TAG3:

                    ToastUtils.setGravity(1, 1, 0);

                    break;
                default:
                    break;
            }
            ToastUtils.showShort(toastInfo);

        }
    }

}

