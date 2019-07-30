package ble;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import app.DataGateWayApp;
import app.GatewayInfo;
import brand.EpdInfoUpdateHandler;
import shop.ShopInfo;
import util.CommonFunc;
import util.SerialCmd;

public class BleDataHandlerThread extends Thread{
	private  static Logger logger = Logger.getLogger(BleDataHandlerThread.class); // 1. create log  
	public static int periodTimerCount=0;
	public static ArrayList<UartBleDataBean> uartBleDataList=new ArrayList<UartBleDataBean>();
	private static Timer periodTimer=null;
	public BleDataHandlerThread() {
		// TODO Auto-generated constructor stub
		if(periodTimer==null) {
			periodTimer=new Timer();
			periodTimer.schedule(new TimerTask() {
		         @Override
		         public void run() {
		        	 logger.debug("------------period timer!------------");
		        	 periodTimerCount++;
		        	 BleAdvDataHandler.reportOrignalAdvData();
		        	 if(periodTimerCount==60)
		        	 {
		        		 periodTimerCount=0;
		        	    // BleAdvDataHandler.sensorStatehandler();
		        	 }	            
		         }
		     }, 10000 , 10000); 
		}
	}
	public static void appendUartBleDataList(UartBleDataBean data) {
		synchronized (uartBleDataList)
	   	 {
			uartBleDataList.add(data);
			uartBleDataList.notifyAll();	   		
	   	 }
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
            while (true) {
                synchronized (uartBleDataList) 
                {
                    if (uartBleDataList.size() == 0) {
                    	uartBleDataList.wait();//then this thread go into  sleep
                    	uartBleDataList.notifyAll();//notify all the producer thread
                    }
                    else
                    {
                    	UartBleDataBean data = uartBleDataList.get(0); 
                    	handleUartBleData(data);
                    	uartBleDataList.notifyAll();               	
                    	uartBleDataList.remove(0);           
                    }
                }                             
                
            }
        } catch (InterruptedException e) {
        	
        	logger.error(e.toString());
        	uartBleDataList.remove(0);
        	DataGateWayApp.uartBleDataHandler();
            e.printStackTrace();
        }
	}
	
	public void handleUartBleData(UartBleDataBean uartData ) {
		if(uartData==null)	
			return;
		if(!SerialCmd.getCmdFromIndex(uartData.data[3]).equals("SENSOR_ADV_DATA_EVT"))
		{
			 logger.debug("verfy cmd success,cmd is:"+SerialCmd.getCmdFromIndex(uartData.data[3])+", value:"+SerialCmd.getCmdReturnValueFromIndex(uartData.data[4])); 
			 SensorGoodsInfoUpdateHandler.currentCmdExecResultHandle((byte)(uartData.data[3]&0xff),(byte)(uartData.data[4]&0xff),null);
		}
		else
		{		
			SensorAdvDataBean data=BleAdvDataHandler.sensorDataPrase(SerialCmd.getCmdData(uartData.data,uartData.len-6));	
            if(data==null)
            {
            	return ;
            }
            if(data.signalIntensity>GatewayInfo.signalIntensityThreshold)
            {
            	logger.debug("signalIntensity out of threshold");
            	return ;
            }
            if(SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.containsKey(data.sensorMacAddr))
            {
            	SensorGoodsInfoUpdateBean sensorGoodsInfoUpdateBean=SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.get(data.sensorMacAddr);
				if(sensorGoodsInfoUpdateBean!=null)
				{
					if(data.goodsInfoVersion!=sensorGoodsInfoUpdateBean.goodsEatgInfoBean.version)
					{
						logger.debug("there is sensor need update goods info");
						logger.debug("start update goods info:"+data.sensorMacAddr);
						sensorGoodsInfoUpdateBean.goodsEatgInfoBean.qrcode=ShopInfo.websiteAddr+data.sensorMacAddr;
						SensorGoodsInfoUpdateHandler.updateSensorGoodsInfo(data.sensorMacAddr,EpdInfoUpdateHandler.creatEpdData(sensorGoodsInfoUpdateBean.goodsEatgInfoBean));
					}
					else
					{
						logger.debug("the current epd version is new ,update is not need,mac addr:"+data.sensorMacAddr);
						SensorGoodsInfoUpdateHandler.reportSensorGoodsInfoUpdate(sensorGoodsInfoUpdateBean,200);
						sensorGoodsInfoUpdateBean.updateGoodsInfoTimeout.cancel();
						//SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.remove(data.sensorMacAddr);
					}
				}		
            }
            
			byte[] advDataByte=new byte[uartData.len-6];
			for(int i=0;i<advDataByte.length;i++)
			{
				advDataByte[i]=(byte) (uartData.data[i+4]&0x000000ff);
			}
			String advDataString=CommonFunc.convertBytesToHexString(advDataByte);
			if(data.sensorType==0x7f)
			{
				String tempData="000000000000000000000000000000000000000000000000000000";
				String signal=advDataString.substring(advDataString.length()-2);
				advDataString=advDataString.substring(0, advDataString.length()-2);
				advDataString+=tempData;
				advDataString+=signal;
			}
			BleAdvDataHandler.handlleSensorAdvData(data,advDataString);  
			 			
		}

	}

}
