package mqtt;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import app.GatewayInfo;


public class MqttHandler{
    private String broker = "tcp://47.94.131.119:1883";
	private String userName = "kcmqtt";
    private String password = "!!Kc2019";
	//private String broker = "tcp://118.190.205.47:1883";
	//private String userName = "mtt";
	//private String password = "mtt2019";;
    private static MqttHandler mqttHandler =null;
	private MqttClient sampleClient;
	//private ConcurrentLinkedQueue<MqttEventMsg> mqttMessageQueue=new ConcurrentLinkedQueue<>();
	public static volatile boolean mqttHandlerStateCheck=false;
    private static Logger logger= Logger.getLogger(MqttHandler.class); // 1. create log  
	public static MqttHandler getInstance(){ 
		if(mqttHandler==null)
		{
			mqttHandler=new MqttHandler();
		}
		return mqttHandler;
     }
	public void setMqttServerConfigInfo(String mqttBroker,String mqttUserName,String mqttPassword)
	{
		if(mqttBroker!=null&&mqttUserName!=null&&mqttPassword!=null)
		{
			broker=mqttBroker;
			userName=mqttUserName;
			password=mqttPassword;
			logger.debug("set mqtt server ok!broker:"+broker+",userName:"+userName+",password:"+password);
		}
	}
	public void close() {
		mqttHandlerStateCheck=false;
		mqttHandler=null;
		try {
			sampleClient.close();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.toString());
		}
	}

	public boolean mqttConnect() {
		if(GatewayInfo.clientId==null)
		{
			logger.error("start mqtt error for client is null");
			return false;
		}
		MemoryPersistence persistence = new MemoryPersistence();
	    try {
	        // 鍒涘缓瀹㈡埛绔�
	        sampleClient = new MqttClient(broker, GatewayInfo.clientId, persistence);
	        
	        // 鍒涘缓閾炬帴鍙傛暟
	        MqttConnectOptions connOpts = new MqttConnectOptions();
	       // connOpts.
	        // 鍦ㄩ噸鏂板惎鍔ㄥ拰閲嶆柊杩炴帴鏃惰浣忕姸鎬�
	        connOpts.setCleanSession(false);
	        // 璁剧疆杩炴帴鐨勭敤鎴峰悕
	        connOpts.setUserName(userName);
	        connOpts.setAutomaticReconnect(false);
	        connOpts.setKeepAliveInterval(90);
	        //connOpts.setMaxReconnectDelay(100000);
	        connOpts.setPassword(password.toCharArray());
	        // 寤虹珛杩炴帴
	        sampleClient.setCallback(new MqttCallback() {
	            public void connectionLost(Throwable cause) {
	            	mqttHandlerStateCheck=false;
	            	try {
						sampleClient.close(true);
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.error(e.toString());
					}
	            	sampleClient=null;
	            	logger.error("connectionLost");               
	            }
	            public void messageArrived(String topic, MqttMessage message) throws Exception {
	                MqttEventMsgBean topicMsg=new MqttEventMsgBean();
	                topicMsg.topic=topic;
	                topicMsg.mqttMsg=message;
	                new MqttMsgReceiveHandlerThread(topicMsg).start();
	            }
	            public void deliveryComplete(IMqttDeliveryToken token) {
	                logger.debug("deliveryComplete---------"+ token.isComplete());
	            }
	        });
	        sampleClient.connect(connOpts);
	        this.subscribeTopic();
	        mqttHandlerStateCheck=true;
	        GatewayInfo.gatewayStateLedIndicate("connect");
	        logger.debug("start mqttHandler success");
	        return true;
	    } catch (MqttException me) {
	    	sampleClient=null;
	        mqttHandlerStateCheck=false;
	    	logger.error("reason " + me.getReasonCode());
	    	logger.error("msg " + me.getMessage());
	    	logger.error("loc " + me.getLocalizedMessage());
	    	logger.error("cause " + me.getCause());
	    	logger.error("excep " + me);
	    	logger.error(me.toString());
	        return false;
	    }
	}
	public boolean mqttReConnect() {
		logger.debug("restart mqtt handler");
		if(sampleClient!=null)
		{
			try {
				sampleClient.close();
				sampleClient=null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//return false;
			}
		}
		return mqttConnect();
	}
 
    public void subscribeTopic() throws MqttException {
    	   // sampleClient.unsubscribe("/mtt/shoptalk/#");
			sampleClient.subscribe("/mtt/shoptalk/"+GatewayInfo.clientId+"/reply/#");
			sampleClient.subscribe("/mtt/shoptalk/"+GatewayInfo.clientId+"/getway_firmware_update");
			sampleClient.subscribe("/mtt/shoptalk/"+GatewayInfo.clientId+"/ping");
    }
    public boolean subscribeTopic(String topic) {
    	if(sampleClient==null)
    		return false;
    	try {
			sampleClient.subscribe(topic);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    public boolean unSubscribeTopic(String topic) {
    	if(sampleClient==null)
    		return false;
    	try {
			sampleClient.unsubscribe(topic);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    public  boolean publishTopic(String topic,MqttMessage msg) {
    	
    	    if(sampleClient==null)
    	    {
    	    	return false;
    	    }
    		try {
     			sampleClient.publish(topic, msg);
     			return true;
     		} catch (MqttException e) {
     			// TODO Auto-generated catch block
     			logger.error(e.toString());
     			mqttHandlerStateCheck=false;
     			return false;
     		}
    	}   	 
}

