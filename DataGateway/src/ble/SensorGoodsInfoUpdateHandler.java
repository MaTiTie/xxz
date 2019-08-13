package ble;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import app.GatewayInfo;
import gnu.io.SerialPort;
import mqtt.MqttEventMsgBean;
import mqtt.MqttMsgSendHandlerThread;
import serial.SendDataToSerialPortFailure;
import serial.SerialPortOutputStreamCloseFailure;
import util.CommonCode;
import util.CommonFunc;
import util.SerialCmd;

public class SensorGoodsInfoUpdateHandler {
	public static SerialPort serialPort=null;
	private static  Logger logger = Logger.getLogger(SensorGoodsInfoUpdateHandler.class); // 1. create log  
	//public boolean currentExecCmd=false;
	public  static volatile SensorGoodsInfoUpdateEvtBean sensorGoodsUpdateEvt=new SensorGoodsInfoUpdateEvtBean();
	private static  Timer goodsUpdateUsetimer=new Timer();
	public static ConcurrentHashMap<String, SensorGoodsInfoUpdateBean> neededUpdateGoodsInfoSensorList =new ConcurrentHashMap<>();
	public SensorGoodsInfoUpdateHandler() {
		// TODO Auto-generated constructor stub
	}
public static void setSerialPort(SerialPort sp) {
	serialPort=sp;
}
public static void currentCmdExecResultHandle(byte cmdType,byte execResult,byte[] data) {
	if(cmdType==sensorGoodsUpdateEvt.currentCmdType) {
		if(sensorGoodsUpdateEvt.cmdExecTimerTask!=null)
		{
			sensorGoodsUpdateEvt.cmdExecTimerTask.cancel();
			sensorGoodsUpdateEvt.cmdExecTimerTask=null;
		}
		if(execResult==SerialCmd.BLE_CMD_SUCCESS)
		{
			logger.debug("cmd exec success:"+SerialCmd.getCmdFromIndex(sensorGoodsUpdateEvt.currentCmdType)+"mac addr:"+sensorGoodsUpdateEvt.SensorMacAddrString);
			if(cmdType==SerialCmd.CONNECT_SENSOR_EVT)
			{
				//sensorGoodsUpdateEvt.connectSensorExecResult=true;
				sensorGoodsUpdateEvt.currentCmdType=SerialCmd.UPDATE_ETAG_GOODS_INFO_EVT;	
				try {
					SerialCmd.setUartCmd(serialPort,sensorGoodsUpdateEvt.currentCmdType , sensorGoodsUpdateEvt.goodsInfoData, sensorGoodsUpdateEvt.goodsInfoDataLength);
				} catch (SendDataToSerialPortFailure | SerialPortOutputStreamCloseFailure e) {
					// TODO Auto-generated catch block
					stopCmdExec();	
					e.printStackTrace();
				}
			}
			else if(cmdType==SerialCmd.UPDATE_ETAG_GOODS_INFO_EVT)
			{
				//sensorGoodsUpdateEvt.sendDataExecResult=true;
				sensorGoodsUpdateEvt.currentCmdType=SerialCmd.DISCONNECT_SENSOR_EVT;	
				try {
					SerialCmd.setUartCmd(serialPort,sensorGoodsUpdateEvt.currentCmdType , null, 0);
				} catch (SendDataToSerialPortFailure | SerialPortOutputStreamCloseFailure e) {
					// TODO Auto-generated catch block
					stopCmdExec();	
					e.printStackTrace();
				}
			}
			else if(cmdType==SerialCmd.DISCONNECT_SENSOR_EVT)
			{
				//sensorGoodsUpdateEvt.sensorGoodsUpdateExecResult=true;
				stopCmdExec();						
				logger.debug("update sensor data success,sensor is:"+sensorGoodsUpdateEvt.SensorMacAddrString);
				SensorGoodsInfoUpdateBean sensorGoodsInfoUpdateBean=neededUpdateGoodsInfoSensorList.get(sensorGoodsUpdateEvt.SensorMacAddrString);
		    	if(sensorGoodsInfoUpdateBean!=null)
		    	{ 		
	    			reportSensorGoodsInfoUpdate(sensorGoodsInfoUpdateBean,CommonCode.SUCCESS);			
	    			sensorGoodsInfoUpdateBean.updateGoodsInfoTimeout.cancel();   		
		    	}			
			}
			if(sensorGoodsUpdateEvt.execCmd)
			{
				sensorGoodsUpdateEvt.cmdExecTimerTask=new TimerTask()
				{							
					@Override
					public void run() {
						// TODO Auto-generated method stub
						logger.debug("exec ble cmd timeout，CmdType:"+SerialCmd.getCmdFromIndex(sensorGoodsUpdateEvt.currentCmdType)+"mac addr:"+sensorGoodsUpdateEvt.SensorMacAddrString);
						stopCmdExec();
						String errorInfo="cmdType:"+sensorGoodsUpdateEvt.currentCmdType+"resultCode:execTimeout";
				    	SensorGoodsInfoUpdateBean sensorGoodsInfoUpdateBean=SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.get(sensorGoodsUpdateEvt.SensorMacAddrString);
				    	if(sensorGoodsInfoUpdateBean!=null)
				    	{
				            sensorGoodsInfoUpdateBean.errorRecord.add(errorInfo);			    			 
				    		if(sensorGoodsInfoUpdateBean.errorRecord.size()>4)
				    		{
				    			reportSensorGoodsInfoUpdate(sensorGoodsInfoUpdateBean,CommonCode.UPDATE_ETAG_EXEC_CMD_TIMEOUT);			
				    			sensorGoodsInfoUpdateBean.updateGoodsInfoTimeout.cancel();
				    		}
				    		   		
				    	}
					}
				};
				goodsUpdateUsetimer.schedule(sensorGoodsUpdateEvt.cmdExecTimerTask, sensorGoodsUpdateEvt.timeoutTime);
			}
		}
		else
		{
			
			System.out.println("exec cmd:"+SerialCmd.getCmdFromIndex(sensorGoodsUpdateEvt.currentCmdType)+"error"+"mac addr:"+sensorGoodsUpdateEvt.SensorMacAddrString);		
		    if(sensorGoodsUpdateEvt.currentCmdType==SerialCmd.DISCONNECT_SENSOR_EVT)//ignore
			{
				//logger.debug("disconnect sensor error");
				//sensorGoodsUpdateEvt.sensorGoodsUpdateExecResult=true;
				stopCmdExec();				
				logger.debug("update sensor data success,sensor is:"+CommonFunc.convertBytesToHexString(sensorGoodsUpdateEvt.SensorMacAddr));
				SensorGoodsInfoUpdateBean sensorGoodsInfoUpdateBean=neededUpdateGoodsInfoSensorList.get(sensorGoodsUpdateEvt.SensorMacAddrString);
		    	if(sensorGoodsInfoUpdateBean!=null)
		    	{ 		
	    			reportSensorGoodsInfoUpdate(sensorGoodsInfoUpdateBean,CommonCode.SUCCESS);			
	    			sensorGoodsInfoUpdateBean.updateGoodsInfoTimeout.cancel();	
		    	}
			}
		    else
		    {
		    	stopCmdExec();	
		    	String errorInfo="cmdType:"+sensorGoodsUpdateEvt.currentCmdType+"resultCode:"+execResult;
		    	SensorGoodsInfoUpdateBean sensorGoodsInfoUpdateBean=neededUpdateGoodsInfoSensorList.get(sensorGoodsUpdateEvt.SensorMacAddrString);
		    	if(sensorGoodsInfoUpdateBean!=null)
		    	{	    		
		    	    sensorGoodsInfoUpdateBean.errorRecord.add(errorInfo);
		    		if(sensorGoodsInfoUpdateBean.errorRecord.size()>4)
		    		{
		    			reportSensorGoodsInfoUpdate(sensorGoodsInfoUpdateBean,CommonCode.UPDATE_ETAG_EXEC_CMD_FAIL);			
		    			sensorGoodsInfoUpdateBean.updateGoodsInfoTimeout.cancel();
		    		}
		    		    		
		    	}
		    }
		}	
		
	}
	else
	{
		logger.error("current cmd is not match");
	}
}
public static  void stopCmdExec() {
	
	logger.debug("stop exec cmd");
	sensorGoodsUpdateEvt.execCmd=false;
}
public static void updateFinishResultHandler(JSONObject payloadData)
{
	long cmdTime=payloadData.getLongValue("time");
	long current_time=new Date().getTime();
	logger.debug("epd update reply:"+cmdTime+"cu:"+current_time);
	if((current_time-cmdTime)>1000*60*60*3) //if cmd comes 3 hours later,then ignore the cmd
	{
		logger.error("epd update reply msg comes time out");
		return;
	}
	  JSONObject data=( payloadData.getJSONObject("params")).getJSONArray("data").getJSONObject(0);
	  String uid=data.getString("uid");
	  JSONArray detail=data.getJSONArray("detail");
	  JSONObject updateRsultInfo=detail.getJSONObject(0);
	  int code= updateRsultInfo.getIntValue("code");
	  String sensorMacAddr=updateRsultInfo.getString("hardwareMac");
	  if(sensorMacAddr==null)
	  {
		  return;
	  }
	  else
	  {
		 if(!SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.containsKey(sensorMacAddr))
		 {
			 return ;
		 }
		 else
		 {
			 String listUid=SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.get(sensorMacAddr).uid;
			 if(listUid!=null)
			 {
				 if(!uid.equals(listUid)) //
				 {
					 logger.error("update reply,but uid not match");
					 return;
				 }
			 }
		 }
	  }
	  if(code==CommonCode.SUCCESS)
	  {
		  logger.debug("update sensor:"+sensorMacAddr +"success");
		  SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.remove(sensorMacAddr);
	  }
	  else
	  {
		  SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.remove(sensorMacAddr);
		  logger.error("update sensor:"+sensorMacAddr+"fail,reason code:"+code);
	  }
}
public static void updateSensorGoodsInfo(String sensorMac,byte[] goodsInfo) {
	if(serialPort==null||goodsInfo==null||sensorMac==null)
	{
		return;
	}
	if(!sensorGoodsUpdateEvt.execCmd)
	{	
		sensorGoodsUpdateEvt.execCmd=true;
		sensorGoodsUpdateEvt.currentCmdType=SerialCmd.CONNECT_SENSOR_EVT;
		sensorGoodsUpdateEvt.SensorMacAddrString=sensorMac;
		sensorGoodsUpdateEvt.SensorMacAddr=new byte[6];
		for(int i=0;i<6;i++)
		{
			sensorGoodsUpdateEvt.SensorMacAddr[i]=(byte) Integer.parseInt(sensorMac.substring(i*2,i*2+2),16);
		}
		sensorGoodsUpdateEvt.goodsInfoData=goodsInfo;
		sensorGoodsUpdateEvt.goodsInfoDataLength=goodsInfo.length;
		sensorGoodsUpdateEvt.cmdExecTimerTask=new TimerTask()
		{							
			@Override
			public void run() {
				// TODO Auto-generated method stub
				logger.debug("exec ble cmd timeout，CmdType:"+SerialCmd.getCmdFromIndex(sensorGoodsUpdateEvt.currentCmdType)+"mac addr:"+sensorGoodsUpdateEvt.SensorMacAddrString);
				stopCmdExec();
				String errorInfo="cmdType:"+sensorGoodsUpdateEvt.currentCmdType+"resultCode:execTimeout";
		    	SensorGoodsInfoUpdateBean sensorGoodsInfoUpdateBean=SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.get(sensorGoodsUpdateEvt.SensorMacAddrString);
		    	if(sensorGoodsInfoUpdateBean!=null)
		    	{	    		
		    		sensorGoodsInfoUpdateBean.errorRecord.add(errorInfo);	    		 
		    		if(sensorGoodsInfoUpdateBean.errorRecord.size()>4)
		    		{
		    			reportSensorGoodsInfoUpdate(sensorGoodsInfoUpdateBean,CommonCode.UPDATE_ETAG_EXEC_CMD_TIMEOUT);			
		    			sensorGoodsInfoUpdateBean.updateGoodsInfoTimeout.cancel();
		    		}		    		   		
		    	}
			}
		};
		goodsUpdateUsetimer.schedule(sensorGoodsUpdateEvt.cmdExecTimerTask, sensorGoodsUpdateEvt.timeoutTime);
		try {
			SerialCmd.setUartCmd(serialPort,sensorGoodsUpdateEvt.currentCmdType , sensorGoodsUpdateEvt.SensorMacAddr, sensorGoodsUpdateEvt.SensorMacAddr.length);
		} catch (SendDataToSerialPortFailure | SerialPortOutputStreamCloseFailure e) {
			// TODO Auto-generated catch block
			stopCmdExec();
			e.printStackTrace();
		}
	}
	else
	{
		logger.debug("execing ble cmd!!!!!");
	}
}
public static  void reportSensorGoodsInfoUpdate(SensorGoodsInfoUpdateBean sensorGoodsInfoUpdateBean,int code) {
	if(!GatewayInfo.gatewayInfoOk())
		return;
	if(sensorGoodsInfoUpdateBean==null)
	{
		return ;
	}
	JSONArray  reportGoodsInfoUpdateJsonArray=new JSONArray();
	JSONObject  reportGoodsInfoUpdateDataJson=new JSONObject();
	
	reportGoodsInfoUpdateDataJson.put("epdInfoLogId", sensorGoodsInfoUpdateBean.epdInfoLogId);
	reportGoodsInfoUpdateDataJson.put("uid", sensorGoodsInfoUpdateBean.uid);
	reportGoodsInfoUpdateDataJson.put("sensorId", sensorGoodsInfoUpdateBean.sensorMacAddr);
	reportGoodsInfoUpdateDataJson.put("code", code);
	reportGoodsInfoUpdateDataJson.put("message", sensorGoodsInfoUpdateBean.errorRecord.toString());
	
	JSONArray sensorGoodsInfoUpdateDetailJsonArray=new JSONArray();
	JSONObject sensorGoodsInfoUpdateDetailJson=new JSONObject();
	
	sensorGoodsInfoUpdateDetailJson.put("epdInfoLogDetailId", sensorGoodsInfoUpdateBean.epdInfoLogDetailId);
	sensorGoodsInfoUpdateDetailJson.put("hardwareMac",sensorGoodsInfoUpdateBean.sensorMacAddr);
	sensorGoodsInfoUpdateDetailJson.put("deviceType",sensorGoodsInfoUpdateBean.deviceType);
	sensorGoodsInfoUpdateDetailJson.put("code", code);
	sensorGoodsInfoUpdateDetailJson.put("message", sensorGoodsInfoUpdateBean.errorRecord.toString());
	sensorGoodsInfoUpdateDetailJsonArray.add(sensorGoodsInfoUpdateDetailJson);
	
	reportGoodsInfoUpdateDataJson.put("detail", sensorGoodsInfoUpdateDetailJsonArray);
	reportGoodsInfoUpdateJsonArray.add(reportGoodsInfoUpdateDataJson);
	
	String shopGoodsInfoUpdateReplyTopic="/mtt/shoptalk/"+GatewayInfo.shopId+"/reply"+"/change_epd_info";
	JSONObject payloadJson = new JSONObject();
	payloadJson.put("deviceId", GatewayInfo.clientId);
	payloadJson.put("version", "1.0");
	payloadJson.put("time", Long.toString(System.currentTimeMillis()));
	payloadJson.put("method", "shop.replyChangeEpdInfo");	
	String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
	String uid=CommonFunc.encodeByMD5(originString);
	if(uid==null)
	{
		uid=Integer.toString((int)(1+Math.random()*100000000));
	}
	payloadJson.put("uid", uid);
	JSONObject paramsJson=new JSONObject();
	paramsJson.put("data", reportGoodsInfoUpdateJsonArray);
	payloadJson.put("params", paramsJson);
	logger.debug("goodsUpdate report"+shopGoodsInfoUpdateReplyTopic);
    MqttMessage message = new MqttMessage(payloadJson.toJSONString().getBytes());
    int qos=2;
    message.setQos(qos);
    MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
    mqttEventMsg.mqttMsg=message;
    mqttEventMsg.topic=shopGoodsInfoUpdateReplyTopic;	
    MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg);	
}
}
