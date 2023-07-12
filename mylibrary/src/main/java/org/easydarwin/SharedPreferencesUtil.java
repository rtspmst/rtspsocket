package org.easydarwin;


public class SharedPreferencesUtil {

    public SharedPreferencesUtil() {

    }


    //============================================================================


    //保存tx2 音量
    public void saveTx2spkVolume(int value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.VOLUME_TX2SPK, value);

    }

    // 获取tx2音量 default is 0
    public int getTx2spkVolume() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeInt(CV.VOLUME_TX2SPK, 200);

    }


    //============================================================================


    //获取是否是第一次启动
    public int getStartFirst() {
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeInt(CV.STARTFIRST, 0);
    }

    //保存是否是第一次启动
    public void saveStartFirst(int value) {
        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.STARTFIRST, value);
    }


    //============================================================================


    public void saveVolume(int value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode("VOLUME", value);

    }

    public int getVolume() {
        // default is 50
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeInt("VOLUME", 5);

    }


    //============================================================================


    //保存降噪开关  key   key  value value
    public void saveMethodSwitch(String key, boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(key, value);

    }

    //获取降噪开关状态
    public boolean getMethodSwitch(String key) {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(key);

    }


    //============================================================================


    // 保存已选择的降噪方法
    public void saveMethodLevel(String key, String value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(key, value);

    }

    //获取已选择的降噪方法
    public String getMethodLevel(String key) {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeString(key);

    }


    //============================================================================


    //最后一次点击
    public void saveLastClick(String value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode("最后一次点击", value);

    }

    //最后一次点击
    public String getLastClick() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeString("最后一次点击");

    }


    //============================================================================


    //保存 获取 语言标志 中英文切换
    public void saveLanguage(boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.PROJECT_LANGUAGE, value);

    }

    public boolean getLanguage() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(CV.PROJECT_LANGUAGE);

    }


    //============================================================================


    //保存 获取有线模式 true为隐藏 false为显示 默认显示
    public void saveWired(boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.HIDDEN_WIRE_MODE, value);

    }

    public boolean getWired() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(CV.HIDDEN_WIRE_MODE);

    }


    //============================================================================


    //保存 获取晃动模式 true为隐藏 false为显示 默认显示
    public void saveShakeMode(boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.HIDDEN_SHAKING_MODE, value);

    }

    public boolean getShakeMode() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(CV.HIDDEN_SHAKING_MODE);

    }


    //============================================================================


    //保存 获取NONE true为隐藏 false为显示 默认显示
    public void saveNONE(boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.HIDDEN_NONE, value);

    }

    public boolean getNONE() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(CV.HIDDEN_NONE);

    }


    //============================================================================


    //保存 获取 降噪 true为隐藏 false为显示 默认显示
    public void saveNoiseReduction(boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.HIDDEN_NOISEREDUCTION, value);

    }

    public boolean getNoiseReduction() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(CV.HIDDEN_NOISEREDUCTION);

    }


    //============================================================================


    //保存 获取 开机自检 true为隐藏 false为显示 默认显示
    public void savePOST(boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.HIDDEN_POST, value);

    }

    public boolean getPOST() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(CV.HIDDEN_POST);

    }


    //============================================================================


    //保存 获取 电动云台 true为隐藏 false为显示 默认显示
    public void savePTZ(boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.HIDDEN_PTZ, value);

    }

    public boolean getPTZ() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(CV.HIDDEN_PTZ);

    }


    //============================================================================


    //保存亮度
    public void saveBrightness(float value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.SP_BRIGHTNESS, value);

    }

    // 获取亮度
    public float getBrightness() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeFloat(CV.SP_BRIGHTNESS, 0.5f);

    }


    //============================================================================

    private String wifikey = "wifikey";

    //保存绑定的WiFi名称
    public void saveWiFiName(String value) {
        SingletonInternalClass.getInstance().getSingleMMKV().encode(wifikey, value);
    }

    //获取绑定的WiFi名称
    public String getWiFiName() {
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeString(wifikey);
    }

    //============================================================================

    public void savePsw( String value) {
        SingletonInternalClass.getInstance().getSingleMMKV().encode("PSD", value);

    }
    public String getPsw() {
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeString("PSD");
    }

    public void saveUserName(String value) {
        SingletonInternalClass.getInstance().getSingleMMKV().encode("KEY", value);
    }
    public String getUserName() {
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeString("KEY");
    }

    //保存是否是复位
    public void saveReset(boolean value) {
        SingletonInternalClass.getInstance().getSingleMMKV().encode("Reset", value);
    }

    public boolean getReset() {
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool("Reset");
    }


    //记录最后一次登录的账号
    public void saveLastAccount(String value) {
        SingletonInternalClass.getInstance().getSingleMMKV().encode("LastAccount", value);
    }

    public String getLastAccount() {
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeString("LastAccount");
    }
}
