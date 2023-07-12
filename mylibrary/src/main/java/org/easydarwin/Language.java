package org.easydarwin;


public class Language {


    //一键自检弹窗语言选择 0为中文 1为印尼语 2为英语
    public static int anInt = 0;

    //------------------------------------------------------------------------------------------------------
    public static String LOGIN = "登陆";
    public static String ACCOUNT_PSW_CANNOT_BE_EMPTY = "帐号密码不能为空";
    public static String ACCOUNT_NUMBER_CANNOT_BE_LESS_THAN_6_DIGITS = "帐号不能少于6位";
    public static String PSW_MUST_NOT_BE_LESS_THAN_6_DIGITS = "密码不能少于6位";
    public static String ACCOUNT_OR_PSW_INCORRECT = "帐号或密码错误";
    public static String CHANGE_PSW = "修改密码";
    public static String SUBMIT = "提交";
    public static String PSW_ATYPISM = "两次密码输入不一致";
    public static String CHANGE_SUCCEEDED = "修改成功";
    public static String ALL_USERS = "所有成员";
    public static String ADD = "添加";
    public static String ACCOUNT_ALREADY_EXISTS = "账户已存在";
    public static String ACCOUNT_MANAGEMENT = "账户管理";
    public static String ACCOUNT_REGISTRATION_SUCCESSFUL = "账号注册成功";
    public static String PASSWORD = "密码";
    public static String PASSWORD_AGAIN = "再次输入密码";
    public static String USER = "用户名";
    public static String DELETE_USER = "删除用户";
    public static String ADD_USER = "新建账户";
    public static String AUDIO_CONNECTION_FAILED = "音频连接失败，请检查设备连接!";
    public static String COMMUNICATION_FAILED = "通信连接失败，请检查设备连接!";
    public static String CONTROL_BOARD_CONNECTION_FAILED = "主控板连接失败，请检查设备连接!";
    //自动调校完成
    public static String AUTOMATIC_ADJUSTMENT_COMPLETED = "自动调校完成";
    //开始校准电机编码值
    public static String CALIBRATION_MOTOR_CODE_VALUE = "开始校准电机编码值";
    //设置位置
    public static String SET_LOCATION = "设置位置";
    //保存位置
    public static String EXIT_SETTINGS = "保存位置";
    //有线模式
    public static String WIRED_MODE = "有线模式";
    //正常模式
    public static String NORMALMODE = "正常模式";
    //晃动模式
    public static String WOBBLEMODE = "晃动模式";
    //距离	Distance
    public static String DISTANCE = "距离:";
    //自动对焦
    public static String AUTO_FOCUS = "自动对焦";
    //开始录像
    public static String RECORDING = "开始录像";
    //录像回放
    public static String PLAYBACK = "录像回放";
    //降噪方法
    public static String DENOISE_METHOD = "降噪方法";
    //系统复位
    public static String SYSTEM_RESET = "系统复位";
    //请先关闭录像或自动对焦
    public static String TURN_OFF_VIDEO = "请先关闭录像或自动对焦";
    //再点一次退出
    public static String DROP_OUT_PROMPT = "再点一次退出";
    //请不要频繁点击
    public static String NOT_CLICK_FREQUENTLY = "请不要频繁点击";
    //拍照
    public static String PHOTOGRAPH = "拍照";
    //省电模式
    public static String POWER_SAVE = "省电模式";
    //一键自检	One-key Self-checking
    public static String ONE_KEY_SELF_CHECKING = "一键自检";
    //长按屏幕恢复
    public static String LONG_PRESS_THE_SCREEN_TO_RESTORE = "长按屏幕恢复";
    //手动对焦
    public static String MANUAL_FOCUS = "手动对焦";
    //左电机
    public static String LEFT_MOTOR = "左电机";
    //右电机
    public static String RIGHT_MOTOR = "右电机";
    //完成对焦
    public static String COMPLETE_FOCUS = "完成对焦";
    //声音
    public static String SOUND = "声音";
    //请先完成准星校准
    public static String SIGHT_CALIBRATION = "请先完成准星校准";
    //慢
    public static String SLOW = "慢";
    //快
    public static String FAST = "快";
    //云台
    public static String BALL_HEAD = "云台";
    //调整架
    public static String ADJUSTING_FRAME = "调整架";
    public static String CANCEL = "取消";
    public static String SELECT_ALL = "全选";
    public static String DELETE_SUCCESS = "删除成功";
    public static String CLICK_AGAIN_DELETE = "再按一次删除文件";
    public static String SELECT_FILE = "请至少选择一个文件";
    public static String VIDEO_PLAYBACK = "视频回放";
    public static String DELETE = "删除";
    public static String NO_DATA = "无数据";
    public static String SIZE = "文件大小 ：";


    public static void drfe() {
        switchIndonesian();
//        anInt = MyApplication.getMMKV().decodeInt(CV.LANGUAGE, 1);
//
//        switch (anInt) {
//            case 0:
//                break;
//            case 1:
//                switchIndonesian();
//                break;
//            case 2:
//                switchEnglish();
//                break;
//        }
    }


//------------------------------------------------------------------------------------------------------
//-----------------------------------印尼----------------------------------------------------------------
//------------------------------------------------------------------------------------------------------


    private static void switchIndonesian() {

        LOGIN = "hantar";
        //帐号密码不能为空
        ACCOUNT_PSW_CANNOT_BE_EMPTY = "Akun tidak dapat kosong";
        //帐号不能少于6位
        ACCOUNT_NUMBER_CANNOT_BE_LESS_THAN_6_DIGITS = "Akun tidak dapat kurang dari 6 digit";
        //密码不能少于6位
        PSW_MUST_NOT_BE_LESS_THAN_6_DIGITS = "Kata sandi tidak dapat kurang dari 6 digit";
        //帐号或密码错误
        ACCOUNT_OR_PSW_INCORRECT = "Nomor akun atau kata sandi salah";
        //修改密码
        CHANGE_PSW = "Ubah kata sandi";
        //提交
        SUBMIT = "hantar";
        //两次密码输入不一致
        PSW_ATYPISM = "Dua kata sandi tidak konsisten";
        //修改成功
        CHANGE_SUCCEEDED = "Ubah berhasil";
        //所有成员
        ALL_USERS = "Semua pengguna";
        //账户已存在
        ACCOUNT_ALREADY_EXISTS = "Akun sudah ada";
        //添加
        ADD = "Tambah";
        //账号注册成功
        ACCOUNT_REGISTRATION_SUCCESSFUL = "Pendaftaran akun berhasil";
        //账户管理
        ACCOUNT_MANAGEMENT = "mengelola akun";
        //用户名
        USER = "Nama pengguna";
        //密码
        PASSWORD = "Kata sandi";
        //再次输入密码
        PASSWORD_AGAIN = "Masukkan kata sandi lagi";
        //新建账户 添加用户
        ADD_USER = "tambah akun";
        //删除用户
        DELETE_USER = "Hapus akun";
        //音频连接失败，请检查设备连接!
        AUDIO_CONNECTION_FAILED = "Koneksi audio gagal, silakan periksa koneksi perangkat!";
        //通信连接失败，请检查设备连接!
        COMMUNICATION_FAILED = "Koneksi komunikasi gagal, silakan periksa koneksi perangkat!";
        //主控板连接失败，请检查设备连接!
        CONTROL_BOARD_CONNECTION_FAILED = "Koneksi papan kontrol utama gagal, silakan periksa koneksi perangkat!";
        //自动调校完成
        AUTOMATIC_ADJUSTMENT_COMPLETED = "automatic adjustment completed";
        //开始校准电机编码值
        CALIBRATION_MOTOR_CODE_VALUE = "开始校准电机编码值";
        //设置位置
        SET_LOCATION = "设置位置";
        //保存位置
        EXIT_SETTINGS = "保存位置";
        //有线模式
        WIRED_MODE = "有线模式";
        //正常模式
        NORMALMODE = "正常模式";
        //晃动模式
        WOBBLEMODE = "晃动模式";
        //距离	Distance
        DISTANCE = "jarak:";
        //自动对焦
        AUTO_FOCUS = "Fokus otomatis";
        //开始录像
        RECORDING = "Mulai merekam";
        //录像回放
        PLAYBACK = "Main balik video";
        //降噪方法
        DENOISE_METHOD = "Kebisingan";
        //系统复位
        SYSTEM_RESET = "Set semula";
        //请先关闭录像或自动对焦
        TURN_OFF_VIDEO = "Harap matikan video atau fokus otomatis terlebih dahulu";
        //再点一次退出
        DROP_OUT_PROMPT = "Klik keluar lagi dalam dua detik";
        //请不要频繁点击
        NOT_CLICK_FREQUENTLY = "Tolong jangan sering klik";
        //拍照
        PHOTOGRAPH = "Ambil gambar";
        //省电模式
        POWER_SAVE = "Penghematan";
        //一键自检
        ONE_KEY_SELF_CHECKING = "Pemeriksaan";
        //长按屏幕恢复
        LONG_PRESS_THE_SCREEN_TO_RESTORE = "Tekan layar panjang untuk memulihkan";
        //手动对焦
        MANUAL_FOCUS = "Fokus manual";
        //左电机
        LEFT_MOTOR = "Motor kiri";
        //右电机
        RIGHT_MOTOR = "Motor kanan";
        //完成对焦
        COMPLETE_FOCUS = "focus finish";
        //声音
        SOUND = "声音";
        //请先完成准星校准
        SIGHT_CALIBRATION = "please complete the front sight calibration first!";
        //慢
        SLOW = "lambat";
        //快
        FAST = "cepat";
        //云台
        BALL_HEAD = "P T Z";
        //调整架
        ADJUSTING_FRAME = "Rangka  penyesuaian";
        //取消
        CANCEL = "batal";
        //全选
        SELECT_ALL = "pilih semua";
        //删除成功
        DELETE_SUCCESS = "hapus berhasil";
        //再按一次删除文件
        CLICK_AGAIN_DELETE = "tekan lagi untuk menghapus file";
        //请至少选择一个文件
        SELECT_FILE = "silakan pilih setidaknya satu file";
        //视频回放
        VIDEO_PLAYBACK = "Main balik video";
        //删除
        DELETE = "hapus";
        //无数据
        NO_DATA = "tidak ada data";
        //文件大小
        SIZE = "ukuran file ：";
    }


//-----------------------------------英文----------------------------------------------------------------


    private static void switchEnglish() {

        LOGIN = "LOGIN";
        //帐号密码不能为空
        ACCOUNT_PSW_CANNOT_BE_EMPTY = "Account password cannot be empty";
        //帐号不能少于6位
        ACCOUNT_NUMBER_CANNOT_BE_LESS_THAN_6_DIGITS = "Account number cannot be less than 6 digits";
        //密码不能少于6位
        PSW_MUST_NOT_BE_LESS_THAN_6_DIGITS = "Password must not be less than 6 digits";
        //帐号或密码错误
        ACCOUNT_OR_PSW_INCORRECT = "Account or password incorrect";
        //修改密码
        CHANGE_PSW = "CHANGE PASSWORD";
        //提交
        SUBMIT = "SUBMIT";
        //两次密码输入不一致
        PSW_ATYPISM = "The two password entries are inconsistent";
        //修改成功
        CHANGE_SUCCEEDED = "Change Succeeded";
        //所有成员
        ALL_USERS = "All Users";
        //添加
        ADD = "ADD";
        //账户已存在
        ACCOUNT_ALREADY_EXISTS = "Account already exists";
        //账号注册成功
        ACCOUNT_REGISTRATION_SUCCESSFUL = "Account registration is successful";
        //账户管理
        ACCOUNT_MANAGEMENT = "Account management";
        //用户名
        USER = "user name";
        //密码
        PASSWORD = "Password";
        //再次输入密码
        PASSWORD_AGAIN = "Enter the password again";
        //新建账户 添加用户
        ADD_USER = "new account";
        //删除用户
        DELETE_USER = "delete user";
        //音频连接失败，请检查设备连接!
        AUDIO_CONNECTION_FAILED = "Audio connection failed, please check !";
        //通信连接失败，请检查设备连接!
        COMMUNICATION_FAILED = "Communication connection failed, please check !";
        //主控板连接失败，请检查设备连接!
        CONTROL_BOARD_CONNECTION_FAILED = "Control board connection failed, please check !";
        //自动调校完成
        AUTOMATIC_ADJUSTMENT_COMPLETED = "automatic adjustment completed";
        //开始校准电机编码值
        CALIBRATION_MOTOR_CODE_VALUE = "start to calibrate the motor code value";
        //设置位置
        SET_LOCATION = "set location";
        //保存位置
        EXIT_SETTINGS = "save location";
        //有线模式
        WIRED_MODE = "wired mode";
        //正常模式
        NORMALMODE = "normal";
        //晃动模式
        WOBBLEMODE = "wobble";
        //距离	Distance
        DISTANCE = "distance:";
        //自动对焦
        AUTO_FOCUS = "auto focus";
        //开始录像
        RECORDING = "record";
        //录像回放
        PLAYBACK = "replay";
        //降噪方法
        DENOISE_METHOD = "denoise";
        //系统复位
        SYSTEM_RESET = "reset";
        //请先关闭录像或自动对焦
        TURN_OFF_VIDEO = "please turn off video recording or auto focus or auto focus first！";
        //再点一次退出
        DROP_OUT_PROMPT = "click again to exit within two seconds";
        //请不要频繁点击
        NOT_CLICK_FREQUENTLY = "please do not click frequently!";
        //拍照
        PHOTOGRAPH = "camera";
        //省电模式
        POWER_SAVE = "power save";
        //一键自检	One-key Self-checking
        ONE_KEY_SELF_CHECKING = "self check";
        //长按屏幕恢复
        LONG_PRESS_THE_SCREEN_TO_RESTORE = "Long press the screen to restore";
        //手动对焦
        MANUAL_FOCUS = "manual focus";
        //左电机
        LEFT_MOTOR = "left motor";
        //右电机
        RIGHT_MOTOR = "right motor";
        //完成对焦
        COMPLETE_FOCUS = "focus finish";
        //声音
        SOUND = "声音";
        //请先完成准星校准
        SIGHT_CALIBRATION = "please complete the front sight calibration first!";
        //慢
        SLOW = "slow";
        //快
        FAST = "fast";
        //云台
        BALL_HEAD = "P T Z";
        //调整架
        ADJUSTING_FRAME = "HOST";
        //文件大小
        SIZE = "size ：";
        //无数据
        NO_DATA = "no data";
        //删除
        DELETE = "delete";
        //视频回放
        VIDEO_PLAYBACK = "video playback";
        //请至少选择一个文件
        SELECT_FILE = "please select at least one file";
        //再按一次删除文件
        CLICK_AGAIN_DELETE = "press again to delete the file";
        //删除成功
        DELETE_SUCCESS = "successfully deleted";
        //全选
        SELECT_ALL = "select all";
        //取消
        CANCEL = "cancel";

    }

    //====================================================================
//====================================================================
//====================================================================
//====================================================================
//====================================================================
//====================================================================
//====================================================================
//====================================================================
    static {
        drfe();
    }

//    //继续播放
//    public static String CONTINUE_PLAY = "terus putar";
//    //停止播放
//    public static String STOP_PLAY = "henti pemutaran";
//    //播放地址无效
//    public static String INVALID_PLAY_ADDRESS = "alamat pemutaran tidak valid";
//    //重播
//    public static String TO_REPLAY = "putar ulang";
//    //点击重试
//    public static String CLICK_AND_RETRY = "klik untuk coba lagi";
//    //视频加载失败
//    public static String VIDEO_LOADING_FAILED = "pemuatan video gagal";
}
