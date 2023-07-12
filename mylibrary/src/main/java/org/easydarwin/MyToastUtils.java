package org.easydarwin;


//import com.blankj.utilcode.util.ToastUtils;

public class MyToastUtils {

    public static boolean isShow = true;

    public static void show(String info) {
//        ToastUtils.showShort(info);
    }

    public static void showToast(int i, String toastInfo) {

        if (isShow) {

            switch (i) {

                case CV.TOAST_TAG1:

//                    ToastUtils.setGravity(1, 1, -200);

                    break;
                case CV.TOAST_TAG2:

//                    ToastUtils.setGravity(1, 1, -100);

                    break;
                case CV.TOAST_TAG3:

//                    ToastUtils.setGravity(1, 1, 0);

                    break;
            }
//            ToastUtils.showShort(toastInfo);

        }
    }

    //帐号密码不能为空
    public static void show1() {
        show(Language.ACCOUNT_PSW_CANNOT_BE_EMPTY);
    }

    //帐号不能少于6位
    public static void show2() {
        show(Language.ACCOUNT_NUMBER_CANNOT_BE_LESS_THAN_6_DIGITS);

    }

    //密码不能少于6位
    public static void show3() {
        show(Language.PSW_MUST_NOT_BE_LESS_THAN_6_DIGITS);


    }

    //提示帐号或密码错误
    public static void show4() {
        show(Language.ACCOUNT_OR_PSW_INCORRECT);

    }

    //两个密码不一致
    public static void show5() {
        show(Language.PSW_ATYPISM);

    }

    //提示修改成功
    public static void show6() {
        show(Language.CHANGE_SUCCEEDED);
    }

    //提示账号已存在
    public static void show7() {
        show(Language.ACCOUNT_ALREADY_EXISTS);

    }

    //账号注册成功
    public static void show8() {
        show(Language.ACCOUNT_REGISTRATION_SUCCESSFUL);

    }
}

