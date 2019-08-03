package ble;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import app.GatewayInfo;
import ble.SensorAdvDataBean;
import mqtt.MqttEventMsgBean;
import mqtt.MqttMsgSendHandlerThread;
import util.CommonFunc;

public class BleAdvDataHandler{
	
	public static volatile CopyOnWriteArrayList<String> sensorAdvDatasList=new CopyOnWriteArrayList<>();
    private static  Logger logger = Logger.getLogger(BleAdvDataHandler.class); // 1. create log  
    
    public static void handlleSensorAdvData(SensorAdvDataBean data,String advDataString) {

				//System.out.println(advDataString);
				advDataString+=Long.toString(new Date().getTime());
				sensorAdvDatasList.add(advDataString);
				if(sensorAdvDatasList.size()>500)//clear the list if it is too large
                {
                	sensorAdvDatasList.clear();
	            }
				if(data.sensorState==SensorAdvDataBean.SENSOR_STATE_CHARGING||(data.sensorState==SensorAdvDataBean.SENSOR_STATE_NOT_INIT_NOT_USE&&data.sensorDataType==SensorAdvDataBean.SENSOR_DATA_TYPE_HEARTRATE)||data.sensorDataType==SensorAdvDataBean.SENSOR_DATA_TYPE_UNBIND)
				{
					//ubind
					JSONArray unbindJsonArray=new JSONArray();
					JSONObject unbindDataJson=new JSONObject();
					unbindDataJson.put("sensorId", data.sensorMacAddr);
					unbindJsonArray.add(unbindDataJson);
					reportUnbindData(data,unbindJsonArray);					
				}
				if(data.sensorDataType==SensorAdvDataBean.SENSOR_DATA_TYPE_GSENSOR) //record g-sensor data for every sensor
				{
					//addInteractionEvent(data);
					logger.debug("gSensor data,macAddr:"+data.sensorMacAddr+" "+new SimpleDateFormat("hh:mm:ss:SSS").format(new Date(data.timestamp)));
						//saveDataToFile
					//	logger.debug("get a new g-sensor data,mac:"+data.sensorMacAddr);
						/*
						StringBuilder sBuilder=new StringBuilder();
						SimpleDateFormat dateFormat= new SimpleDateFormat("HH:mm:ss:SSS");
						long time=new Date().getTime();
										
						for(int i=0;i<9;i++)
						{
							String s=dateFormat.format(new Date(time-(18-2*i)*240)).toString()+","+Integer.toString(data.sensorData[i]/16)+","+Integer.toString(data.sensorData[i+9]/16)+","+Integer.toString(data.sensorData[i+18]/16);
							sBuilder.append(s);
							sBuilder.append("\r\n");
							s=dateFormat.format(new Date(time-(18-2*i-1)*240)).toString()+","+Integer.toString(data.sensorData[i]%16)+","+Integer.toString(data.sensorData[i+9]%16)+","+Integer.toString(data.sensorData[i+18]%16);
							sBuilder.append(s);
							sBuilder.append("\r\n");
							//System.out.printf("%d,%d\r\n",data.gSensorData[i]/16,data.gSensorData[i]%16);
						}
						CommonFunc.saveDataToFile(sBuilder.toString(),"e://log.txt");
					*/
				}
				else if(data.sensorDataType==SensorAdvDataBean.SENSOR_DATA_TYPE_CHECK_STOCK) //this a check stock data,should report to server
				{
					reprotCheckStockData(data);
					//System.out.println(data.toString());
				}			
			
	}
 	
	public static SensorAdvDataBean sensorDataPrase(int[] advData)
	{
		if(advData.length==41)  //new sensor ble 5.0 adv package
    	{
    		SensorAdvDataBean data=new SensorAdvDataBean();

    		data.sensorType=advData[0];
    		data.sensorState=advData[1];
    		data.sensorDataType=advData[2];
	    	data.goodsInfoVersion=advData[3];
			data.advCount=advData[4];		
			for(int i=0;i<6;i++)
			{
				String temp=Integer.toHexString(advData[5+i]);
				if(temp.length()==1)
				{
					temp="0"+temp;
				}
				data.sensorMacAddr+=temp;
			}
			//System.out.println(data.sensorMacAddr);
			data.batteryLevle=advData[11];
			data.firmwareVersion=advData[12];				
			for(int i=0;i<27;i++)
			{
				data.sensorData[i]=advData[13+i];
			}
			data.signalIntensity=advData[40];
		    return data;
    	}
		else if(advData.length==14)   //new sensor ble 4.0 adv package
		{
			SensorAdvDataBean data=new SensorAdvDataBean();

    		data.sensorType=advData[0];
    		data.sensorState=advData[1];
    		data.sensorDataType=advData[2];
	    	data.goodsInfoVersion=advData[3];
			data.advCount=advData[4];		
			for(int i=0;i<6;i++)
			{
				String temp=Integer.toHexString(advData[5+i]);
				if(temp.length()==1)
				{
					temp="0"+temp;
				}
				data.sensorMacAddr+=temp;
			}
			//System.out.println(data.sensorMacAddr);
			data.batteryLevle=advData[11];
			data.firmwareVersion=advData[12];				
			for(int i=0;i<27;i++)
			{
				data.sensorData[i]=0;
			}
			data.signalIntensity=advData[13];
			if(data.sensorType==0x7e)
			{
				logger.error("ble apapter adv:"+data.sensorMacAddr);
			}
		    return data;
		}
		else if(advData.length==24)  //old sensor ble 4.0 package
    	{
			SensorAdvDataBean data=new SensorAdvDataBean();
	    	data.sensorState=advData[0];
	    	data.sensorType=advData[1];
	    	data.firmwareVersion=advData[2];
	    	data.sensorMacAddr="";
	    	for(int i=0;i<6;i++)
			{
				String temp=Integer.toHexString(advData[3+i]);
				if(temp.length()==1)
				{
					temp="0"+temp;
				}
				data.sensorMacAddr+=temp;
			}
	    	data.advCount=advData[9];
	    	data.sensorDataType=advData[10];
	    	for(int i=0;i<6;i++)
	    	{
	    		data.sensorData[i]=advData[11+i];
	    	}
	    	data.goodsInfoVersion=advData[17];
	    	data.batteryLevle=advData[18];
	    	for(int i=0;i<4;i++)
	    	{
	    		data.remarks[i]=advData[19+i];
	    	}
	    	data.signalIntensity=advData[23];
	    	return data;
    	}	
     	else
    	{
    		 logger.error("adv format error,adv len:"+advData.length+"data:"+CommonFunc.convertBytesToHexString(advData));
    	}
    	
    	return null;
	}

	public static boolean reportSensorEventData(JSONArray dataJsonArray ){
		if(!GatewayInfo.gatewayInfoOk())
			return false;
		String sensorEventReportTopic = "/mtt/shoptalk/"+GatewayInfo.shopId+"/"+GatewayInfo.clientId+"/event";
    	JSONObject payloadJson = new JSONObject();
		payloadJson.put("deviceId", GatewayInfo.clientId);
		payloadJson.put("version", "1.0");
		payloadJson.put("time", Long.toString(System.currentTimeMillis()));
		payloadJson.put("method", "sensor.event");
		String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
		String uid=CommonFunc.encodeByMD5(originString);
		if(uid==null)
		{
			uid=Integer.toString((int)(1+Math.random()*100000000));
		}
		payloadJson.put("uid", uid);
		JSONObject paramsJson=new JSONObject();
		paramsJson.put("data", dataJsonArray);
		payloadJson.put("params", paramsJson);
		logger.debug("sensor event report:"+payloadJson.toJSONString());
        MqttMessage message = new MqttMessage(payloadJson.toJSONString().getBytes());
        int qos=2;
        message.setQos(qos);
        MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
        mqttEventMsg.mqttMsg=message;
        mqttEventMsg.topic=sensorEventReportTopic;	
        MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg);	
        return true;
	}
	
	public static void reportOrignalAdvData(){
		if(!GatewayInfo.gatewayInfoOk())
			return;
		logger.debug("advDataList size:"+sensorAdvDatasList.size());
		if(sensorAdvDatasList.size()>0)
		{
			JSONArray  orginalAdvDataJsonArray=new JSONArray();			
			for(String orginalAdvData:sensorAdvDatasList)
			{
				//System.out.println(timeStamp);	
				JSONObject advDataJson=new JSONObject();
				//System.out.println("leng:"+orginalAdvData.length());
				if(orginalAdvData.length()==61)
				{
					advDataJson.put("sensorType", "63");
				}
				else 
				{
					advDataJson.put("sensorType", orginalAdvData.substring(0,2));
				}
				advDataJson.put("sensorData", orginalAdvData);
				orginalAdvDataJsonArray.add(advDataJson);
			}
			
				String originalAdvDataReportTopic = "/mtt/shoptalk/"+GatewayInfo.shopId+"/"+GatewayInfo.clientId+"/original_data";		
				JSONObject payloadJson = new JSONObject();
				payloadJson.put("deviceId", GatewayInfo.clientId);
				payloadJson.put("version", "1.0");
				payloadJson.put("time", Long.toString(System.currentTimeMillis()));
				payloadJson.put("method", "sensor.originalData");	
				String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
				String uid=CommonFunc.encodeByMD5(originString);
				if(uid==null)
				{
					uid=Integer.toString((int)(1+Math.random()*100000000));
				}
				payloadJson.put("uid", uid);
				JSONObject paramsJson=new JSONObject();
				paramsJson.put("data", orginalAdvDataJsonArray);
				payloadJson.put("params", paramsJson);
				logger.debug("orignal adv data report");
		        MqttMessage message = new MqttMessage(payloadJson.toJSONString().getBytes());
		        int qos=0;
		        message.setQos(qos);
                MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
                mqttEventMsg.mqttMsg=message;
                mqttEventMsg.topic=originalAdvDataReportTopic;	
                sensorAdvDatasList.clear();
		        MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg);		            
			}
		
	}	
	
    public static void reportExceptionData(JSONArray exceptionJsonArray) {
    	if(!GatewayInfo.gatewayInfoOk())
			return;
    	String deviceExceptionReportTopic="/mtt/shoptalk/"+GatewayInfo.shopId+"/"+GatewayInfo.clientId+"/exception";
    	JSONObject payloadJson = new JSONObject();
		payloadJson.put("deviceId", GatewayInfo.clientId);
		payloadJson.put("version", "1.0");
		payloadJson.put("time", Long.toString(System.currentTimeMillis()));
		payloadJson.put("method", "shop.exception");
		String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
		String uid=CommonFunc.encodeByMD5(originString);
		if(uid==null)
		{
			uid=Integer.toString((int)(1+Math.random()*100000000));
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
    
    public static void reprotCheckStockData(SensorAdvDataBean data) {
    	if(!GatewayInfo.gatewayInfoOk())
			return;
    	JSONArray stockCheckDetailJsonArray=new JSONArray();
    	SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	/*
    	GoodsStockBean goodsStock=ShopInfo.getSensorBindInfo(sensorList.get(data.sensorMacAddr).bindInfo);
    	for(int i=0;i<6;i++)
    	{
    		if(data.sensorData[i]!=0xff) 
    		{
	    		JSONObject checkStock=new JSONObject();
	    		checkStock.put("sizeIndex", data.sensorData[i]);
	    		if(goodsStock==null)
	    		{
	    			checkStock.put("hasStock", false);
	    		}
	    		else if(goodsStock.stock[data.sensorData[i]]>0)
	    		{
	    			checkStock.put("hasStock", true);
	    		}
	    		else
	    		{
	    			checkStock.put("hasStock", false);
	    		}
	    		stockCheckDetailJsonArray.add(checkStock);
    		}
    	}
    	*/
    	for(int i=0;i<6;i++)
    	{
    		JSONObject checkStock=new JSONObject();
    		checkStock.put("sizeIndex", data.sensorData[i]);
    		if(data.sensorData[i]!=0xff) 
    		{ 			    		
	    		checkStock.put("checkStock", true);
    		}
    		else
    		{
    			checkStock.put("checkStock", false);
    		}
    	}  	
    	JSONArray  dataJsonArray=new JSONArray();	
		JSONObject eventDataJson=new JSONObject();
		eventDataJson.put("eventType", 2);
		eventDataJson.put("sensorId", data.sensorMacAddr);//dateFormat.format(new Date(data.timestamp))
		eventDataJson.put("eventTime", dateFormat.format(new Date(data.timestamp)));
		eventDataJson.put("stockCheckDetail", stockCheckDetailJsonArray);
		dataJsonArray.add(eventDataJson);
		
    	String sensorEventReportTopic = "/mtt/shoptalk/"+GatewayInfo.shopId+"/"+GatewayInfo.clientId+"/event";
    	JSONObject payloadJson = new JSONObject();
		payloadJson.put("deviceId", GatewayInfo.clientId);
		payloadJson.put("version", "1.0");
		payloadJson.put("time", Long.toString(System.currentTimeMillis()));
		payloadJson.put("method", "sensor.event");
		String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
		String uid=CommonFunc.encodeByMD5(originString);
		if(uid==null)
		{
			uid=Integer.toString((int)(1+Math.random()*100000000));
		}
		payloadJson.put("uid", uid);
		JSONObject paramsJson=new JSONObject();
		paramsJson.put("data", dataJsonArray);
		payloadJson.put("params", paramsJson);
		logger.debug("sensor event report:"+payloadJson.toJSONString());
        MqttMessage message = new MqttMessage(payloadJson.toJSONString().getBytes());
        int qos=2;
        message.setQos(qos);
        MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
        mqttEventMsg.mqttMsg=message;
        mqttEventMsg.topic=sensorEventReportTopic;	
        MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg);		
    }
    
    public static boolean reportUnbindData(SensorAdvDataBean data,JSONArray unbindJsonArray) {
    	if(!GatewayInfo.gatewayInfoOk())
			return false;
		String sensorunBindReportTopic="/mtt/shoptalk/"+GatewayInfo.shopId+"/"+GatewayInfo.clientId+"/unbind";
		System.out.println(sensorunBindReportTopic);
		JSONObject payloadJson = new JSONObject();
		payloadJson.put("deviceId", GatewayInfo.clientId);
		payloadJson.put("version", "1.0");
		payloadJson.put("time", Long.toString(System.currentTimeMillis()));
		payloadJson.put("method", "sensor.unbind");	
		String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
		String uid=CommonFunc.encodeByMD5(originString);
		if(uid==null)
		{
			uid=Integer.toString((int)(1+Math.random()*100000000));
		}
		payloadJson.put("uid", uid);
		JSONObject paramsJson=new JSONObject();	
		paramsJson.put("data", unbindJsonArray);
		payloadJson.put("params", paramsJson);
		logger.debug("sensor unbind report");
        MqttMessage message = new MqttMessage(payloadJson.toJSONString().getBytes());
        int qos=1;
        message.setQos(qos);
        MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
        mqttEventMsg.mqttMsg=message;
        mqttEventMsg.topic=sensorunBindReportTopic;	
        MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg);  
        return true;
    }
}
	
