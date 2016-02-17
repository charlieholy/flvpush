package com.flvpush.ui;

/**
 * Created by charlieu777 on 2016/2/2.
 */
public class PushData {

    public byte[] getDataBuf() {
        return DataBuf;
    }

    public void setDataBuf(byte[] dataBuf) {
        DataBuf = dataBuf;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public byte getmType() {
        return mType;
    }

    public void setmType(byte mType) {
        this.mType = mType;
    }


    protected byte[] DataBuf = null;
    long timestamp = 0;
    byte mType;



}
