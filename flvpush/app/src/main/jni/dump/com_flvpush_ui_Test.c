#include <jni.h>
//#include "com_flvpush_ui_Test.h"

#include <android/log.h>

#define _FILE_OFFSET_BITS	64

#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <stdio.h>

#include <signal.h>		// to catch Ctrl-C
#include <getopt.h>

#ifdef WIN32
#define fseeko fseeko64
#define ftello ftello64
#include <io.h>
#include <fcntl.h>
#define	SET_BINMODE(f)	setmode(fileno(f), O_BINARY)
#else
#define	SET_BINMODE(f)
#endif

#include "librtmp/rtmp_sys.h"
#include "librtmp/log.h"


#define RD_SUCCESS		0
#define RD_FAILED		1
#define RD_INCOMPLETE		2
#define RD_NO_CONNECT		3

//#define PUSH_URL "rtmp://120.25.192.75/activity/9454291663591902"
#define PUSH_URL "rtmp://120.25.192.75/activity/31415927"

#define HTTP_URL "http://120.25.192.75:27771/live/31415927.flv"

#define FILE_DIR "/sdcard/test.flv"
#define debug 1
#define msTime 1
#define usTime 40000 // 40*10^3  40ms

#define DEF_TIMEOUT	30	/* seconds */
#define DEF_BUFTIME	(10 * 60 * 60 * 1000)	/* 10 hours default */
#define DEF_SKIPFRM	0

#define RTMP_LogPrintf(...)  __android_log_print(ANDROID_LOG_VERBOSE,      "TAG", __VA_ARGS__)
#define RTMP_Log(...)  __android_log_print(ANDROID_LOG_VERBOSE,      "TAG", __VA_ARGS__)


int InitSockets() {
	return TRUE;
}

inline void CleanupSockets() {
	//return;
}
#ifdef _DEBUG
uint32_t debugTS = 0;
int pnum = 0;

FILE *netstackdump = 0;
FILE *netstackdump_read = 0;
#endif

uint32_t nIgnoredFlvFrameCounter = 0;
uint32_t nIgnoredFrameCounter = 0;
#define MAX_IGNORED_FRAMES	50




JNIEXPORT jint JNICALL Java_com_flvpush_ui_Test_connectRtmp(JNIEnv *env, jclass jc,jlong lp,jstring url){
	RTMP *rtmp = (RTMP*)lp;
	if(NULL == rtmp)
	{
		RTMP_Log("NULL == rtmp");
		return -1;
	}
	if(NULL == url)
	{
		RTMP_Log("NULL ==  url");
		return -1;
	}
//	jboolean isCopy;
//	char* pushurl;
//	pushurl = (*env)->GetStringUTFChars(env, url,&isCopy);
	RTMP_Init(rtmp);
	//set connection timeout,default 30s
	rtmp->Link.timeout=30;
	if(!RTMP_SetupURL(rtmp,PUSH_URL))
	{
		RTMP_Log("SetupURL Err\n");
		//RTMP_Free(rtmp);
		CleanupSockets();
		return -1;
	}
	//(*env)->DeleteLocalRef(env,pushurl);
    //(*env)->ReleaseStringUTFChars(env,url,pushurl);

	//if unable,the AMF command would be 'play' instead of 'publish'
	RTMP_EnableWrite(rtmp);	
	
	if (!RTMP_Connect(rtmp,NULL)){
		RTMP_Log("Connect Err\n");
		//RTMP_Free(rtmp);
		return -1;
	}
	
	if (!RTMP_ConnectStream(rtmp,0)){
		RTMP_Log("ConnectStream Err\n");
		//RTMP_Close(rtmp);
		//RTMP_Free(rtmp);
		return -1;
	}

	return 0;
}


JNIEXPORT jlong JNICALL Java_com_flvpush_ui_Test_getRtmpPacket(JNIEnv *env, jclass jc){
   RTMP_Log("==========> GET PACKET!");
   RTMPPacket *packet=NULL;
   packet=(RTMPPacket*)malloc(sizeof(RTMPPacket));
   RTMPPacket_Alloc(packet,1024*64);
   RTMPPacket_Reset(packet);
   return (jlong)packet;
}

JNIEXPORT jint JNICALL Java_com_flvpush_ui_Test_freeRtmpPacket(JNIEnv *env, jclass jc,jlong pt){
  RTMPPacket *packet = (RTMPPacket*)pt;
  if(NULL == packet)
  {
    RTMP_Log("==========> NULL == packet!");
    return -1;
  }
  if (packet!=NULL){
  		RTMPPacket_Free(packet);
  		free(packet);
  		packet=NULL;
  }
  return 0;
}


JNIEXPORT jint Java_com_flvpush_ui_Test_SendgetPacketRtmp(JNIEnv *env, jclass jc,jlong lp,jbyteArray bytes,jint buf_length ,jlong timestp,jchar type,jlong pt) {
   	char* cbuf = NULL;
   	RTMP *rtmp = (RTMP*)lp;
   	if(NULL == rtmp)
   	{
   		RTMP_Log("NULL == rtmp");
   		return -1;
   	}
     RTMPPacket *packet = (RTMPPacket*)pt;
     if(NULL == packet)
     {
       RTMP_Log("==========> NULL == packet!");
       return -1;
     }
   	//jsize  len  =  (*env)-> GetArrayLength(env,bytes);
   	jchar  *  arrayBody  =  (*env)-> GetCharArrayElements(env,bytes,0);
   	(*env)->ReleaseCharArrayElements(env,bytes,arrayBody,0);
   	//env->ReleaseIntArrayElements(buf, cbuf, 0);
   	packet->m_hasAbsTimestamp = 0;
   	packet->m_nChannel = 0x04;
   	packet->m_nInfoField2 = 0;
   	packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
   	packet->m_nTimeStamp = timestp;
   	packet->m_packetType = type;
   	packet->m_nBodySize  = strlen( arrayBody);
   	packet->m_body = (char *)arrayBody;

   	if (!RTMP_IsConnected(rtmp)){
   		RTMP_Log("rtmp is not connect\n");
   		return -1;
   	}
   	if (!RTMP_SendPacket(rtmp,packet,0)){
   			RTMP_Log("Send Error\n");
   			return -1;
   	}
    //(*env)->DeleteLocalRef(env,arrayBody);
   	return 0;

}


JNIEXPORT jlong Java_com_flvpush_ui_Test_createRtmp(JNIEnv *env, jclass jc) {
	RTMP *rtmp=NULL;	
	rtmp=RTMP_Alloc();
	//RTMP_Init(rtmp);
	return (jlong)rtmp;
}

JNIEXPORT jint Java_com_flvpush_ui_Test_closeRtmp(JNIEnv *env, jclass jc,jlong lp) {

	RTMP *rtmp = (RTMP*)lp;
	if(NULL == rtmp)
	{
		RTMP_Log("NULL == rtmp");
		return -1;
	}
    if (rtmp!=NULL){
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        rtmp=NULL;
    }
	return 0;
	
}




JNIEXPORT jstring Java_com_flvpush_ui_Test_init(JNIEnv *env, jclass jc) {
	return (*env)->NewStringUTF(env, "===========>init!");
}
