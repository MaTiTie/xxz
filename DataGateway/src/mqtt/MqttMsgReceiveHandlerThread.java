package mqtt;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSONObject;
import app.GatewayInfo;
import ble.BleController;
import ble.SensorGoodsInfoUpdateHandler;
import brand.EpdInfoUpdateHandler;
import shop.ShopInfo;

public class MqttMsgReceiveHandlerThread extends Thread {
    MqttEventMsgBean eventMsg;
    private  static Logger logger = Logger.getLogger(BleController.class);  
	public MqttMsgReceiveHandlerThread(MqttEventMsgBean msg) {
		// TODO Auto-generated constructor stub
		eventMsg=msg;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
        	 logger.debug("get new topic:"+eventMsg.topic);	 
        	 JSONObject payloadData=JSONObject.parseObject(eventMsg.mqttMsg.toString());
        	 String msgEvt=payloadData.getString("method");	 

            if(msgEvt.equals("shop.replyConfig"))
        	{
            	ShopInfo.shopInfoHandler(payloadData);	 	 
        	}
            else if(msgEvt.equals("shop.shopInfoReply"))
    	 	{
            	ShopInfo.shopInfoHandler(payloadData);	 			  				  
    	 	}
        	else if(msgEvt.equals("shop.changeEpdInfo"))
        	{   
        		EpdInfoUpdateHandler.epdInfoUpdateHandler(payloadData);        	 	 
        	}
        	else if(msgEvt.equals("getway.getwayFirmwareUpdate"))
        	{
        		GatewayInfo.gatewayFirmwareUpdateHandler(payloadData);
        	}
        	else if(msgEvt.equals("shop.replyChangeEpdInfo"))
        	{
        		SensorGoodsInfoUpdateHandler.updateFinishResultHandler(payloadData);       		  
        	 }
        	else if(msgEvt.equals("device.ping"))
        	{
        		GatewayInfo.gatewayPingResponse();
        	}
        	 else
        	 {
        		  logger.debug("unknown msgEvt:"+msgEvt);
        	 }	
	}
}
