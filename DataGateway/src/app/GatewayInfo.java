package app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import mqtt.MqttEventMsgBean;
import mqtt.MqttMsgSendHandlerThread;
import util.CommonFunc;
import util.Config;


public class GatewayInfo {
	
	public static volatile String shopId=null;
	public static volatile String clientId=null;
	public static float firewareVersion=1.0f;
	public static String bleAdapterFirmwareVersion="";
	public static String mqttBroker=null;
	public static String mqttUserName=null;
	public static String mqttPassword=null;
	public static String runtimePath=".";
	public static String systemOs="";
	public static int signalIntensityThreshold=80;
	private  static Logger logger = Logger.getLogger(GatewayInfo.class); 
	public static void setShopId(String id) {
		shopId=id;
	}
	public static void setClientId(String id) {
		clientId=id;
	}
	public static boolean gatewayInfoOk() {
		if(clientId!=null&&shopId!=null)
			return true;
		logger.debug("Gateway info is not all ok:"+"shopid:"+shopId+"clientid:"+clientId);
		return false;
	}
	public static void gatewayFirmwareUpdateHandler(JSONObject payloadData)
	{
    	JSONObject data=( payloadData.getJSONObject("params")).getJSONArray("data").getJSONObject(0);
    	String fileName=data.getString("fileName");	
    	String filePath=data.getString("filePath");
    	String fileData=data.getString("fileData");
    	byte[] fileDataBytes=CommonFunc.convertBase64ToBytes(fileData);
    	FileOutputStream fos;
		try {
			String file=GatewayInfo.runtimePath+filePath+fileName;
			System.out.println("file is:"+file);
			fos = new FileOutputStream(file);
			fos.write(fileDataBytes);
			fos.close();	
			try {
				String os=Config.getAppInfo("os", GatewayInfo.runtimePath);
				if(os!=null)
				{
					if(os.startsWith("Win"))
					{
						//Runtime.getRuntime().exec("cmd /k start "+GatewayInfo.runtimePath+"\\"+"update.bat");
					}
					else
					{
						if(fileName.contains(".jar"))
						{
							Runtime.getRuntime().exec("sudo sh /home/firefly/update.sh");
						}
						else
						{
							Runtime.getRuntime().exec("sudo sh filePath+fileName");
						}
						
					}
				}
				logger.debug("exec update cmd success");
				//DataGateWayApp.closeApp(); //关闭程序以便重启			
				//System.exit(0);
			} catch (IOException e) {
				logger.debug("exec update cmd fail");
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			logger.debug("get update file error");
			e1.printStackTrace();
		}
	}
	public static void gatewayPingResponse()
	{
		JSONObject pingResponseJson=new JSONObject();
		pingResponseJson.put("deviceType", "60");	
    	String heartbeatReportTopic="/mtt/shoptalk/"+GatewayInfo.clientId+"/pong";
    	pingResponseJson.put("deviceId", GatewayInfo.clientId);
    	pingResponseJson.put("version", "1.0");
    	pingResponseJson.put("time", Long.toString(System.currentTimeMillis()));
    	pingResponseJson.put("method", "device.pong");	
		String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
		String uid=CommonFunc.encodeByMD5(originString);
		if(uid==null)
		{
			uid=Integer.toString((int)(1+Math.random()*100000000));
		}
		pingResponseJson.put("uid", uid);
		JSONObject paramsJson=new JSONObject();
		paramsJson.put("data", "");
		String networkBoardMacAddr=CommonFunc.getMACAddress();
		paramsJson.put("networkBoardMacAddr", networkBoardMacAddr);
		String ipAddr=CommonFunc.getLocalIpAddr();
		paramsJson.put("ipAddr", ipAddr);
		paramsJson.put("bleAdapterFirmwareVersion", GatewayInfo.bleAdapterFirmwareVersion);
		pingResponseJson.put("params", paramsJson);
		logger.debug("gateway pong report");
        MqttMessage message = new MqttMessage(pingResponseJson.toJSONString().getBytes());
        int qos=1;
        message.setQos(qos);
        MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
        mqttEventMsg.mqttMsg=message;
        mqttEventMsg.topic=heartbeatReportTopic;	
        MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg);
	}
	public static void gatewayStateLedIndicate(String state) {
		String os=Config.getAppInfo("os", GatewayInfo.runtimePath);
		try {
		if(os!=null)
		{
			if(!os.startsWith("Win"))
			{
				if(state.equals("connect"))
				{
					logger.debug("led on");
					//Runtime.getRuntime().exec("sudo echo default-on >/sys/class/leds/firefly:blue:power/trigger");
					Runtime.getRuntime().exec("sudo sh /home/firefly/net_led_off.sh");
				}
				else if(state.equals("disconnect"))
				{
					logger.debug("led off");
					//Runtime.getRuntime().exec("sudo echo none >/sys/class/leds/firefly:blue:power/trigger");
					Runtime.getRuntime().exec("sudo sh /home/firefly/net_led_on.sh");
				}
			}			
		}
		} catch (IOException e) {
			logger.debug("exec linux cmd error");
			e.printStackTrace();
		}
	}
	public static void gatewayHeartbeatReport() {
		    if(!gatewayInfoOk())
		    {
		    	return ;
		    }
	    	JSONArray heartbeatJsonArray=new JSONArray();
	 		SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
			JSONObject sensorHeartbeatJson=new JSONObject();
			sensorHeartbeatJson.put("deviceType", "60");
			sensorHeartbeatJson.put("deviceId", GatewayInfo.clientId);
			sensorHeartbeatJson.put("getwayVersion", GatewayInfo.firewareVersion);
			sensorHeartbeatJson.put("os", GatewayInfo.systemOs);
			sensorHeartbeatJson.put("bleAdapterFirmwareVersion", GatewayInfo.bleAdapterFirmwareVersion);
			sensorHeartbeatJson.put("time", dateFormat.format(new Date()));
			heartbeatJsonArray.add(sensorHeartbeatJson);
	    	String heartbeatReportTopic="/mtt/shoptalk/"+GatewayInfo.shopId+"/"+GatewayInfo.clientId+"/heartbeat";
	    	JSONObject payloadJson = new JSONObject();
			payloadJson.put("deviceId", GatewayInfo.clientId);
			payloadJson.put("version", "1.0");
			payloadJson.put("time", Long.toString(System.currentTimeMillis()));
			payloadJson.put("method", "device.heartbeat");	
			String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
			String uid=CommonFunc.encodeByMD5(originString);
			if(uid==null)
			{
				uid=Integer.toString((int)(1+Math.random()*100000000));
			}
			payloadJson.put("uid", uid);
			JSONObject paramsJson=new JSONObject();
			paramsJson.put("data", heartbeatJsonArray);
			payloadJson.put("params", paramsJson);
			logger.debug("gateway heratbeat report");
	        MqttMessage message = new MqttMessage(payloadJson.toJSONString().getBytes());
	        int qos=1;
	        message.setQos(qos);
	        MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
	        mqttEventMsg.mqttMsg=message;
	        mqttEventMsg.topic=heartbeatReportTopic;	
	        MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg);
	    
	    }
    
	public static void deviceExceptionReport(int exceptionCode) {    	
	    	if(!gatewayInfoOk())
	    	{
				return ;
	    	}
	    	JSONArray exceptionJsonArray=new JSONArray();
			SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
			JSONObject deviceExceptionJson=new JSONObject();
	 		String exceptionMessage = "gateway error";
	 		deviceExceptionJson.put("deviceType", "60");
	 		deviceExceptionJson.put("deviceId", GatewayInfo.clientId);
	 		deviceExceptionJson.put("exceptionCode", exceptionCode);
	 		deviceExceptionJson.put("exceptionMessage",exceptionMessage);
	 		deviceExceptionJson.put("time", dateFormat.format(new Date()));
	 		exceptionJsonArray.add(deviceExceptionJson);
	    	String deviceExceptionReportTopic="/mtt/shoptalk/"+GatewayInfo.shopId+"/"+GatewayInfo.clientId+"/exception";
	    	JSONObject payloadJson = new JSONObject();
			payloadJson.put("deviceId",GatewayInfo.clientId);
			payloadJson.put("version", "1.0");
			payloadJson.put("time", Long.toString(System.currentTimeMillis()));
			payloadJson.put("method", "shop.exception");
			String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
			String uid=CommonFunc.encodeByMD5(originString);
			if(uid==null)
			{
				return ;
			}
			payloadJson.put("uid", uid);
			JSONObject paramsJson=new JSONObject();
			paramsJson.put("data", exceptionJsonArray);
			payloadJson.put("params", paramsJson);
			logger.debug("device exception report");
	        MqttMessage message = new MqttMessage(payloadJson.toJSONString().getBytes());
	        int qos=2;
	        message.setQos(qos);        
	        MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
	        mqttEventMsg.mqttMsg=message;
	        mqttEventMsg.topic=deviceExceptionReportTopic;	
	        MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg); 
	    }
	 
	public static boolean restart(String path) {
		try {
			String os=Config.getAppInfo("os", GatewayInfo.runtimePath);
			if(os!=null)
			{
				if(os.startsWith("Win"))
				{
					Runtime.getRuntime().exec("cmd /k start "+path+"\\"+"update.bat");
				}
				else
				{
					Runtime.getRuntime().exec("sudo sh /home/firefly/update.sh");
				}
			}
			//DataGateWayApp.closeApp(); //关闭程序以便重启			
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
