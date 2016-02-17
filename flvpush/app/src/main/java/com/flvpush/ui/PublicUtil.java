package com.flvpush.ui;

import android.util.Log;

/**
 * Created by charlieu777 on 2016/2/2.
 */
public class PublicUtil {
    public  static  long getDataLong(byte[] bytes)
    {
        return bytes[2]&0xff | bytes[1]<<8&0xff00 | bytes[0]<<16&0xff0000;
    }

    public  static  long getTimeStp(byte[] bytes)
    {
        return bytes[3]<<24&0xff000000 | bytes[2]&0xff | bytes[1]<<8&0xff00 | bytes[0]<<16&0xff0000;
    }
}
