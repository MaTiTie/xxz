package app;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.google.zxing.WriterException;

import ble.BleController;
import ble.BleDataHandlerThread;
import mqtt.MqttHandler;
import mqtt.MqttMsgSendHandlerThread;
import shop.ShopInfo;
import util.CommonCode;
import util.CommonFunc;
import util.Config;

public class DataGateWayApp {
	private static  float fmVersion=2.90f;
	static MqttHandler mqtt=null;
	static BleController bleController=null;
	public static String arg=null;
	public static MqttMsgSendHandlerThread mqttMsgSendHandlerThread=null;
	static int periodTimerCount=0;
	public static String runtimePath=".";
	static String appVersion=null;
	public static int mqttReconnCount=0;
	public static volatile boolean watchdogSignal=false;
	private  static Logger logger= Logger.getLogger(DataGateWayApp.class); // 1. create log   
	public static void main(String[] args) {
		//randomDelay();
		Thread.setDefaultUncaughtExceptionHandler(  new UncaughtExceptionHandler() {	
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				// TODO Auto-generated method stub
				try {
					Runtime.getRuntime().exec("sudo reboot");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		prepareStart(args);	
		logger.error("------------------------start gateway app-----------------------------\r\n");
		periodStateCheckTimer();
		watchdogThread();
		startApp();
	}
	public static void watchdogThread()
	{
		Thread watchdogThread=new Thread(new Runnable() {	
			@Override
			public void run() {
				logger.debug("watchdog thread loop start");
				// TODO Auto-generated method stub
				while(true)
				{
				try {
					Thread.sleep(65000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(watchdogSignal==false)
				{
					 try {
            			 logger.error("watdog timeout,exec reboot");
            			 try {
         					Thread.sleep(1000);
         				} catch (InterruptedException e) {
         					// TODO Auto-generated catch block
         					e.printStackTrace();
         				}
						Runtime.getRuntime().exec("sudo reboot");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				watchdogSignal=false;			
			}
			}
		});
		watchdogThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// TODO Auto-generated method stub
		try {
			Runtime.getRuntime().exec("sudo reboot");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
		}
);
		watchdogThread.start();
	}
	public static void randomDelay()
	{
		int delayTime=(int)(1+Math.random()*1000)*60;  //delay time is 0s-60s
		try {
			Thread.sleep(delayTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void periodStateCheckTimer() {
		new Timer().scheduleAtFixedRate(new TimerTask() {
	         @Override
	         public void run() {
	        	 watchdogSignal=true;
	        	 logger.debug("---------state check period timer start----------");
	        	 periodTimerCount++;
	        	 logger.debug("Serial Buffer length:"+BleController.checkDataIndex);
	             if(!MqttHandler.mqttHandlerStateCheck)
	             {  
	            	 mqttReconnCount++;
	            	 GatewayInfo.gatewayStateLedIndicate("disconnect");
	            	 logger.error("mqttHandler error");
	            	 try {
		            	 mqtt=MqttHandler.getInstance();
	                     mqtt.mqttReConnect();
	            	 }
	            	 catch (Exception e) {
						// TODO: handle exception
	            		 logger.debug(e.toString());
					}
	            	 if(mqttReconnCount>5)
	            	 {
	            		 mqttReconnCount=0;
	            		 try {
	            			 logger.error("mqtt not work,exec reboot");
							Runtime.getRuntime().exec("sudo reboot");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            	 } 	            	
	             }
	             else
	             {   
	            	 mqttReconnCount=0;
	            	 if(periodTimerCount>=10)
	            	 {
	            		 periodTimerCount=0;
	            		 try
	            		 {
	            			 GatewayInfo.gatewayHeartbeatReport();
	            			 if(GatewayInfo.shopId==null)
			            	 {
			            		 ShopInfo.shopConfigInfoRequest();
			            	 }
	            		 }catch(Exception e)
	            		 {
	            			 logger.error(e.getMessage());
	            		 }            		
	            	 }
	             }
	             if(!BleController.bleControllerStateCheck)
	             {  
	            	 try 
	            	 {
	            		 GatewayInfo.deviceExceptionReport(CommonCode.GATEWAY_EXCEPTION);
		            	 logger.error("bleController error");
		            	 bleController.interrupt();
		            	 bleController=new BleController();
		            	 bleController.setName("bleController");
		            	 bleController.start();
		            	 System.gc();
	            	 }
	            	 catch (Exception e) {
						// TODO: handle exception
	            		 logger.error(e.getMessage());
					}
	             }
	             if(MqttHandler.mqttHandlerStateCheck&BleController.bleControllerStateCheck)
	             {
	            	 logger.debug("-----------all is ok-------------");            	 
	             }
	             logger.debug("---------state check period timer end----------");
	         }
	     }, 60000 , 20000); 
	}
	public static void startApp() {	
		//sleep 2s first
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			startApp();
		}
		bleController=new BleController();
		bleController.start();
		uartBleDataHandler();
		int bleAdapterStartCount=0;
		while(bleController.bleAdapterMacAddr==null)
		{
			bleAdapterStartCount++;
			if(bleAdapterStartCount==5)
			{
				bleAdapterStartCount=0;
				break;
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block				
				e.printStackTrace();
				startApp();
			}			
		}
		if(bleController.bleAdapterMacAddr!=null)
		{
			logger.debug("ble controller mac addr is:"+bleController.bleAdapterMacAddr);
			//GatewayInfo.setClientId(bleController.bleAdapterMacAddr);
			Config.saveInfo("clientId",bleController.bleAdapterMacAddr, runtimePath);
			if(GatewayInfo.systemOs.startsWith("Win"))
			{
				try {
					CommonFunc.createQrCode(runtimePath+"//"+GatewayInfo.clientId+".jpg",GatewayInfo.clientId,900,"JPEG");
				} catch (WriterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
		{
			logger.error("can not get clientId,start error");
			//return ;
		}
		mqtt= MqttHandler.getInstance();
		startMqttSender();
		mqtt.setMqttServerConfigInfo(GatewayInfo.mqttBroker, GatewayInfo.mqttUserName, GatewayInfo.mqttPassword);
		if(mqtt.mqttConnect())
		{	
			ShopInfo.shopConfigInfoRequest();
		}
		else
		{
			logger.error("start mqtt error");
		}		
	}
	public static void uartBleDataHandler(){
		BleDataHandlerThread bleDataHandlerThread=new BleDataHandlerThread();
		bleDataHandlerThread.setUncaughtExceptionHandler((t, e) -> {
			bleDataHandlerThread.interrupt();
			System.gc();
           logger.error(e.getMessage());
           uartBleDataHandler();
        });
		bleDataHandlerThread.setName("uartBleDataHandlerThread");
		bleDataHandlerThread.start();
    }
	public static void startMqttSender(){
		MqttMsgSendHandlerThread mqttMsgSendHandlerThread=new MqttMsgSendHandlerThread();
		mqttMsgSendHandlerThread.setUncaughtExceptionHandler((t, e) -> {
			mqttMsgSendHandlerThread.interrupt();
			System.gc();
           logger.error(e.getMessage());
           startMqttSender();
        });
		mqttMsgSendHandlerThread.setName("mqttMsgSendHandlerThread");
		mqttMsgSendHandlerThread.start();
    }
	public static void prepareStart(String[] args) {				
		//first,get runtime path
		 String os=System.getProperties().getProperty("os.name");
		 System.out.println("os:"+os);
		 if(args.length>0)
		    {
		    	String path=args[0];
		        int index=path.lastIndexOf('\\');
		        if(index!=-1)
		        {
		        	path=path.substring(0,index);
		        	runtimePath=path;		        
		        	System.setProperty("log.path", path);
		    		PropertyConfigurator.configure(path+"/log4j.properties");  
		        }
		    }
		    else
		    {
		    	if(os.startsWith("Win"))
		    	{
			    	runtimePath=".";
			    	System.setProperty("log.path", ".");
		    		PropertyConfigurator.configure("./log4j.properties"); 
		    	}
		    	else
		    	{
		    		runtimePath="/home/firefly";
			    	System.setProperty("log.path", "/home/firefly");
		    		PropertyConfigurator.configure("/home/firefly/log4j.properties"); 
		    	}
		    }
			  
		   Config.saveInfo("os",os, runtimePath);
		   GatewayInfo.systemOs=os;
		 
		 GatewayInfo.runtimePath=runtimePath;
		 logger.debug("current runtime path is:"+runtimePath);
		 //second,match firmware version	 
		 File file=new File(runtimePath+"/upgrade");
		 if(!file.exists())
		 {
			 file.mkdirs();
		 } 
		appVersion=Float.toString(fmVersion);
		GatewayInfo.firewareVersion=fmVersion;
		logger.debug("current app version:"+appVersion);
		String newAppVersionStr=Config.getAppInfo("appVersion", runtimePath+"/upgrade");
		if(newAppVersionStr!=null)
		{
			float newAppVersionFlt=Float.parseFloat(newAppVersionStr);
			if(newAppVersionFlt-fmVersion>0.001)
			{
				logger.error("last upgrate error,new version is:"+newAppVersionFlt+"current version is:"+fmVersion);
				logger.debug("start upgrade app");
				Thread upgradeThread=new Thread(new Runnable() {				
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(!GatewayInfo.restart(runtimePath))
						{
							logger.debug("restart error error,still run this version");
						}
						else
						{
							logger.debug("restarting");
						}
					}
				});		
				upgradeThread.start();
			}	
		}
		
		String lastAppVersionStr=Config.getAppInfo("appVersion", runtimePath);
		if(lastAppVersionStr==null)
		{
			Config.saveInfo("appVersion",appVersion, runtimePath);
		}
		else
		{
			float lastAppVersionFlt=Float.parseFloat(lastAppVersionStr);
			if(fmVersion-lastAppVersionFlt>0.001)
			{
				logger.debug("upgrade gateway firmware success");
				Config.saveInfo("appVersion",Float.toString(fmVersion), runtimePath);
			}
		}		
						
		{
			try
			{
				GatewayInfo.mqttBroker=Config.getAppInfo("mqttServerAddr", runtimePath);
				GatewayInfo.mqttUserName=Config.getAppInfo("mqttUserName", runtimePath);
				GatewayInfo.mqttPassword=Config.getAppInfo("mqttPassword", runtimePath);
				logger.debug("get mqtt server config from config file:"+"broker:"+GatewayInfo.mqttBroker+",userName:"+GatewayInfo.mqttUserName+",password:"+GatewayInfo.mqttPassword);
				String signalThres=Config.getAppInfo("signalIntensityThreshold", runtimePath);
				if(signalThres!=null)
				{
					GatewayInfo.signalIntensityThreshold=Integer.parseInt(signalThres);
					Config.saveInfo("signalIntensityThreshold",Integer.toString(GatewayInfo.signalIntensityThreshold), runtimePath);
				}
			}
			catch(Exception e)
			{
				logger.error(e.toString());
			}
		}
		
		{
			try 
			{
				String shopType=Config.getAppInfo("shopType", runtimePath);
				if(shopType!=null)
				{
					ShopInfo.shopType=shopType;
				}
				else
				{
					Config.saveInfo("shopType","KC",runtimePath);
					ShopInfo.shopType="KC";
				}
			}catch (Exception e) {
				// TODO: handle exception
				logger.error(e.toString());
			}
		}
		
	}
	 	
}
