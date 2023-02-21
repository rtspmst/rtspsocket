package org.easydarwin.opus.opus
/**
 * JNI操作
 */
class OpusUtils {
    init {
        System.loadLibrary("opusJni")
    }

    companion object {
        private var opusUtils: OpusUtils? = null
        fun getInstant(): OpusUtils {
            if (opusUtils == null) {
                synchronized(OpusUtils::class.java) {
                    if (opusUtils == null) {
                        opusUtils = OpusUtils()
                    }
                }
            }
            return opusUtils!!
        }
    }

    external fun createEncoder(sampleRateInHz:Int, channelConfig:Int, complexity:Int): Long

//    创建解码器
    external fun createDecoder(sampleRateInHz:Int, channelConfig:Int): Long

    external fun encode(handle: Long, lin: ShortArray, offset: Int, encoded: ByteArray): Int
//    解码  将Opus数据解码为PCM
    external fun decode(handle: Long, encoded: ByteArray, lin: ShortArray): Int
    external fun destroyEncoder(handle: Long)
    external fun destroyDecoder(handle: Long)
}