package org.easydarwin;


public class SharedPreferencesUtil {

    public SharedPreferencesUtil() {

    }


    //Save tx2 volume
    public void saveTx2spkVolume(int value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.VOLUME_TX2SPK, value);

    }

    // Obtain tx2 volume default is 0
    public int getTx2spkVolume() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeInt(CV.VOLUME_TX2SPK, 200);

    }


    public void saveVolume(int value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode("VOLUME", value);

    }

    public int getVolume() {
        // default is 50
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeInt("VOLUME", 5);

    }


    //============================================================================


    //Save noise reduction switch ，  key   key  value value
    public void saveMethodSwitch(String key, boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(key, value);

    }

    //Obtain noise reduction switch status
    public boolean getMethodSwitch(String key) {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(key);

    }


    //============================================================================


    // Save the selected noise reduction method
    public void saveMethodLevel(String key, String value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(key, value);

    }

    //Obtain the selected noise reduction method
    public String getMethodLevel(String key) {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeString(key);

    }


    //============================================================================


    //Last click
    public void saveLastClick(String value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode("LastClick", value);

    }

    //Last click
    public String getLastClick() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeString("LastClick");

    }


    //============================================================================


    //Save Get LanguageTr Flag
    public void saveLanguage(boolean value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.LANGUAGE, value);

    }
    public boolean getLanguage() {

        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool(CV.LANGUAGE);

    }


    //Save brightness
    public void saveBrightness(float value) {

        SingletonInternalClass.getInstance().getSingleMMKV().encode(CV.SP_BRIGHTNESS, value);

    }

    // Obtain brightness
    public float getBrightness() {
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeFloat(CV.SP_BRIGHTNESS, 0.5f);
    }

    public void savePsw(String value) {
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

    public void saveReset(boolean value) {
        SingletonInternalClass.getInstance().getSingleMMKV().encode("Reset", value);
    }

    public boolean getReset() {
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeBool("Reset");
    }

    public void saveLastAccount(String value) {
        SingletonInternalClass.getInstance().getSingleMMKV().encode("LastAccount", value);
    }

    public String getLastAccount() {
        return SingletonInternalClass.getInstance().getSingleMMKV().decodeString("LastAccount");
    }
}
