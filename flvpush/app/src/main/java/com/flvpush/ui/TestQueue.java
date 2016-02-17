package com.flvpush.ui;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by charlieu777 on 2016/2/2.
 */
public class TestQueue {

    public  static Queue<PushData> queue = new LinkedList<PushData>();

    public static void qPush(PushData data)
    {
        queue.add(data);
        //Log.i("TAG","============>add num:" + data.getTimestamp() + "size:" + TestQueue.queue.size());
    }

    public static void qReset()
    {
        //queue.removeAll(queue);
        queue = null;
        queue = new LinkedList<PushData>();
    }

    public static PushData qPop()
    {
        PushData pushData = null;
       if(TestQueue.queue.size() != 0)
       {
           pushData = TestQueue.queue.remove();
           //Log.i("TAG","============>pop num:" +  pushData.getTimestamp() + "datalen:" + pushData.getDataBuf().length + "size:" + TestQueue.queue.size());
       }
       return  pushData;

    }

}
