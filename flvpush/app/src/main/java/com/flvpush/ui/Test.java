package com.flvpush.ui;

import android.util.Log;

/**
 * Created by charlieu777 on 2016/2/1.
 */
public class Test {


    public native long createRtmp();
    public native int connectRtmp(long lp,String url);
    public native long getRtmpPacket();
    public native int SendgetPacketRtmp(long lp,byte[] buf,int  buf_length ,long timestp,byte type,long packet);
    public native int freeRtmpPacket(long pt);
    public native int closeRtmp(long lp);

    public long mRtmp = 0;  //rtmp
    public long mPacket = 0; //packet

    static
    {
        System.loadLibrary("dump");
    }
}
