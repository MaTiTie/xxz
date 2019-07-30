package mqtt;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import app.DataGateWayApp;

public class MqttMsgSendHandlerThread extends Thread {
	
	public static ArrayList<MqttEventMsgBean> mqttMsgQueue=new ArrayList<>();
	private  static Logger logger = Logger.getLogger(MqttMsgSendHandlerThread.class); 	
	public static void appendMqttmsgQueue(MqttEventMsgBean msg) {
		synchronized (mqttMsgQueue)
	   	 {
			logger.debug("add a mqtt msg in queue");
			MqttMsgSendHandlerThread.mqttMsgQueue.add(msg);
			MqttMsgSendHandlerThread.mqttMsgQueue.notifyAll();	   		
	   	 }
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
            while (true) {
                synchronized (mqttMsgQueue) 
                {
                    if (mqttMsgQueue.size() == 0) {
                    	mqttMsgQueue.wait();//then this thread go into  sleep
                    	mqttMsgQueue.notifyAll();//notify all the producer thread
                    }
                    else
                    {
                    	MqttEventMsgBean msg = mqttMsgQueue.get(0);  
                    	logger.debug("prepare to send a mqtt msg");
                    	mqttMsgQueue.notifyAll();
                    	if(MqttHandler.getInstance().publishTopic(msg.topic, msg.mqttMsg))
                    	{                     	
                    		mqttMsgQueue.remove(0); 
                    		logger.debug(msg.mqttMsg.toString());
                    	}
                    	else
                    	{
                    		 logger.error("mqtt send msg error");
                    		 Thread.sleep(3000);
                    	}
          
                    }
                }                             
                
            }
        } catch (InterruptedException e) {
        	logger.debug(e.toString());
        	mqttMsgQueue.remove(0);
        	DataGateWayApp.startMqttSender();
            e.printStackTrace();
        }
	}

}
