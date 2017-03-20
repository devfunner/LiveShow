package com.liveshow.media;

import com.liveshow.rtmp.RtmpJni;

/**
 * Created by liuyt on 2017/3/20.
 */

public class RtmpPublisher {

    private long cPtr;
    private long timeOffset;

    public int init(String url, int w, int h, int timeOut) {
        cPtr = RtmpJni.init(url, w, h, timeOut);
        if (cPtr != 0) {
            return 0;
        }
        return -1;
    }


    public int sendSpsAndPps(byte[] sps, int spsLen, byte[] pps, int ppsLen, long timeOffset) {
        this.timeOffset = timeOffset;
        return RtmpJni.sendSpsAndPps(cPtr, sps, spsLen, pps, ppsLen, 0);
    }

    public int sendVideoData(byte[] data, int len, long timestamp) {
        if(timestamp-timeOffset<=0){return -1;}
        return RtmpJni.sendVideoData(cPtr, data, len, timestamp - timeOffset);
    }

    public int sendAacSpec(byte[] data, int len) {
        return RtmpJni.sendAacSpec(cPtr, data, len);
    }

    public int sendAacData(byte[] data, int len, long timestamp) {
        if(timestamp-timestamp<0){return -1;}
        return RtmpJni.sendAacData(cPtr, data, len, timestamp - timeOffset);
    }

    public int stop() {
        return RtmpJni.stop(cPtr);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stop();
    }
}
