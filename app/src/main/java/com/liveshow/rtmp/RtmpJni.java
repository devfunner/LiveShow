package com.liveshow.rtmp;

/**
 * Created by Administrator on 2017/3/20.
 */

public class RtmpJni {
    static {
        System.loadLibrary("publishrtmp");
    }
    public static native long init(String url, int w, int h, int timeOut);

    public static native int sendSpsAndPps(long cptr, byte[] sps, int spsLen, byte[] pps, int ppsLen, long timestamp);

    public static native int sendVideoData(long cptr, byte[] data, int len, long timestamp);

    public static native int sendAacSpec(long cptr, byte[] data, int len);

    public static native int sendAacData(long cptr, byte[] data, int len, long timestamp);

    public static native int stop(long cptr);
}
