package com.flvpush.ui;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by charlieu777 on 2016/2/2.
 */
public class BufList {

    public  static List<PushData> queue = new LinkedList<PushData>();

    public static void qPush(PushData data)
    {
        queue.add(data);
        //Log.i("TAG","============>add num:" + data.getTimestamp() + "size:" + TestQueue.queue.size());
    }

    public static void qReset()
    {
        //queue.removeAll(queue);
        while (!queue.isEmpty())
        {
            queue.remove(0);
        }
//        queue = null;
//        queue = new LinkedList<PushData>();

    }

    public static PushData qPop(int pos)
    {
        PushData pushData = null;
       if(BufList.queue.size() > pos)
       {
           pushData = BufList.queue.get(pos);
           //Log.i("TAG","============>pop num:" +  pushData.getTimestamp() + "datalen:" + pushData.getDataBuf().length + "size:" + TestQueue.queue.size());
       }
       else
       {
           pushData = BufList.queue.get(BufList.queue.size());
       }
       return  pushData;

    }

}
