package com.flvpush.ui;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends ActionBarActivity {

    String url = "rtmp://120.25.192.75/activity/31415927";
    final String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.flv";
    final String filename = "test.flv";
    public  final int MTU = 1300;
    final int TS = 2000;
    final int DELAY = 40;
    final int SHOW_TIME = 10;
    int totoltime = 0;
    final long TESTTIME = 10*1000; //
    TextView tv;
    TextView tvMsg;
    Button btn_start;
    boolean isstopRead = true;
    boolean isStopPush = true;
    Runnable pushRun;
    Runnable readRun;
    File file;

    Test test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        file = new File(filepath);
        tv = (TextView)findViewById(R.id.tv_delay);
        tvMsg = (TextView)findViewById(R.id.tv_msg);
        btn_start = (Button)findViewById(R.id.btn_start);
        tvMsg.setText("...");
        test = new Test();
        test.mRtmp = test.createRtmp();

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isstopRead)
                {
                    AsyncTaskConnSync sync = new AsyncTaskConnSync();
                    sync.execute();
                }

            }
        });



        Log.i("TAG", "===============================>onCreate");

        pushRun  = new Runnable() {
            @Override
            public void run() {
                Log.i("TAG","===============>runpush!begin!" );


                Log.i("TAG","===============>Connect rtmp:" + url);
                test.mPacket = test.getRtmpPacket();

                while (!isStopPush)
                {
                    PushData pushData = TestQueue.qPop();
                    if(pushData != null)
                    {
                        if(test.SendgetPacketRtmp(test.mRtmp,pushData.getDataBuf(),pushData.getDataBuf().length,pushData.getTimestamp(),pushData.getmType(),test.mPacket) !=0)
                        {
//                            if(test.closeRtmp(test.mRtmp) != 0)
//                            {
//                                return;
//                            }
                            Log.i("TAG","===============>Close rtmp!");
                        }
                    }

                    try {
                        Thread.sleep(DELAY);
                    }catch (Exception e)
                    {
                    }
                }
//                if(test.closeRtmp(test.mRtmp) != 0)
//                {
//                    return;
//                }
//                if(  test.freeRtmpPacket(test.mPacket) != 0)
//                {
//                    return;
//                }
                Log.i("TAG","===============>Close rtmp!");

            }
        };

        readRun = new Runnable() {
            @Override
            public void run() {
                Log.i("TAG","===============>runread!begin!" );
                InputStream   in = null;
                try {
                    byte[] type = new byte[1];  // type
                    byte[] datalength = new byte[3];  // type
                    byte[] timestamp = new byte[4];  // type
                    byte[] streamid = new byte[3];  // type
                    byte[] preTagsize = new byte[4];

                    //File file = new File(filepath);
                    in  = getAssets().open(filename);

                    //jump over FLV Header  9
                    //jump over previousTagSizen  4
                    //not quite the same as FLV spec
                    in.skip(13);
                    int cyclenum = 0;
                    while (!isstopRead)
                    {
                        //in.mark((int )file.length());
                        in.read(type);
                        in.read(datalength);
                        in.read(timestamp);
                        in.read(streamid);

                        long dataLong = PublicUtil.getDataLong(datalength);
                        long timeStp = PublicUtil.getTimeStp(timestamp) + TS*cyclenum;
                        //Log.i("TAG","============>num："+ num++ );
                        //Log.i("TAG","============>timeStp："+ timeStp);
                        if ((type[0]&0xff)!=0x08&&(type[0]&0xff)!=0x09){
                            //jump over non_audio and non_video frame，
                            //jump over next previousTagSizen at the same time
                            //fseek(fp,datalength+4,SEEK_CUR);
                            in.skip(dataLong+4);
                            continue;
                        }

                        byte[] data = new byte[(int)dataLong];
                        in.read(data);

                        PushData data1 = new PushData();
                        data1.setDataBuf(data);
                        data1.setTimestamp(timeStp);
                        data1.setmType(type[0]);
                        TestQueue.qPush(data1);

                        int res = in.read(preTagsize);

                        if(res == -1)
                        {
                            cyclenum ++;
                            //in.close();
                            in.reset();
                            in.skip(13);
                        }
                        try {
                            Thread.sleep(DELAY-1);
                        }catch (Exception e)
                        {
                            Log.i("TAG",e.toString());
                        }

                    }
                    in.close();
                }
                catch (Exception e)
                {
                    Log.i("TAG", e.toString());

                }
                finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

        };




    }


    private class AsyncTaskConnSync extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {

            try {
                if(test.connectRtmp(test.mRtmp, url)!= 0)
                {
                    Log.i("TAG","===============>Connect rtmp error!" );
                    Message message = new Message();
                    message.what = RTMP_CON_ERR;
                    handler.sendMessage(message);
                    return null;
                }
            }catch (Exception e)
            {

            }




            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvMsg.setText("Conn...");
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.i("TAG","==================>");

            tvMsg.setText("testing...");
            handler.postDelayed(runnable, SHOW_TIME);
            isstopRead = false;
            isStopPush = false;
            new Thread(readRun).start();
            new Thread(pushRun).start();

        }
    }

    final int RTMP_CON_ERR = 1;


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case RTMP_CON_ERR:
                    Log.i("TAG","===============>Connect rtmp error!" );
                    Toast.makeText(MainActivity.this,"Connect rtmp error!",Toast.LENGTH_LONG);
                    tvMsg.setText("Connect rtmp error!");
                    resetAll();
                    break;
                default:
                    break;


            }
            super.handleMessage(msg);
        }
    };
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {

                totoltime += SHOW_TIME;
                int delay =  TestQueue.queue.size();
                if(totoltime > TESTTIME)
                {

                    resetAll();
                    //test.closeRtmp(test.lp);
                    tv.setText("Over delay: " + delay * delay);
                    tvMsg.setText("Over!");
                    return;
                }
                else
                {
                    handler.postDelayed(this, SHOW_TIME);
                }



                tv.setText("delay: " + delay * delay);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("exception...");
            }
        }
    };

    public void resetAll()
    {
        isStopPush = true;
        isstopRead = true;
        totoltime = 0;
        TestQueue.qReset();
    }


    private long exitTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if ((System.currentTimeMillis() - exitTime) > 2000) // System.currentTimeMillis()无论何时调用，肯定大于2000
            {

                Toast.makeText(this, "再按一下退出！",Toast.LENGTH_LONG).show();
                exitTime = System.currentTimeMillis();
            }
            else
            {
                System.exit(0);
            }

            return true;
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
