package shop;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import app.GatewayInfo;
import mqtt.MqttEventMsgBean;
import mqtt.MqttHandler;
import mqtt.MqttMsgSendHandlerThread;
import util.CommonCode;
import util.CommonFunc;
import util.Config;

public class ShopInfo {
	public static String shopId=null;
	public static String shopLocation=null;
	public static int brandId=0;
	public static String websiteAddr=null;
	public  static  String shopType="KC";
	private static Logger logger= Logger.getLogger(ShopInfo.class); // 1. create log 
	
    public static void shopInfoHandler(JSONObject payloadData)
    {
    	if(shopType.equals("GLS"))
    	{
    		glsShopInfoHandler(payloadData);
    	}
    	else
    	{
    		kcShopInfoHandler(payloadData);
    	}
    }
    
    public static void glsShopInfoHandler(JSONObject payloadData)
    {
	 	  int code= payloadData.getJSONObject("params").getIntValue("code");
	 	  String message=payloadData.getJSONObject("params").getString("message");
	 	  if(code==200)
	 	  {
  	 	  JSONObject data=( payloadData.getJSONObject("params")).getJSONArray("data").getJSONObject(0);
  	 	  JSONArray  goodsInfosJArry=data.getJSONArray("goodsInfo");
  	 	  //JSONArray  bindInfosJArry=data.getJSONArray("bindData");
  	 	  String shopId=data.getString("shopId");
  	 	  if(shopId!=null)
  	 	  {
      	 	  GatewayInfo.setShopId(shopId);
      	 	  Config.saveInfo("shopId", shopId, GatewayInfo.runtimePath);
  	 	  }
  	 	  logger.debug("shop id is:"+ GatewayInfo.shopId+"goodsCount:"+goodsInfosJArry.size());
  	 	  MqttHandler.getInstance().subscribeTopic("/mtt/shoptalk/"+shopId+"/change_epd_info");
  	 	  MqttHandler.getInstance().subscribeTopic("/mtt/shoptalk/"+shopId+"/reply/change_epd_info");
  	 	  /*
  	 	  ArrayList<BindInfoBean> bindInfosList=new ArrayList<>();
  	 	  for(int i=0;i<bindInfosJArry.size();i++)
			  {
				  String jString=bindInfosJArry.getJSONObject(i).toJSONString();
				 // System.out.println(jString);
				 BindInfoBean bindInfo= JSON.parseObject(jString, new TypeReference<BindInfoBean>() {});
				  //System.out.println(bindInfo);
				 if(bindInfo!=null)
				 {
					 bindInfosList.add(bindInfo);
				 }
				 else
				 {
					logger.error("get bind info error");
				 }
			  }
  	 	  ShopInfo.updateBindInfo(bindInfosList);
			  ArrayList<GoodsInfoBean> goodsInfosList=new ArrayList<>();
			  for(int i=0;i<goodsInfosJArry.size();i++)
			  {
				  String jString=goodsInfosJArry.getJSONObject(i).toJSONString();
				 // System.out.println(jString);
				  GoodsInfoBean goodsInfo= JSON.parseObject(jString, new TypeReference<GoodsInfoBean>() {});
				  //System.out.println(goodsInfo.styleNumber);
				  goodsInfosList.add(goodsInfo);
			  }
			  ShopInfo.setGoodsInfo(goodsInfosList);
			  */
	 	  }
	 	  else
	 	  {
	 		 logger.error(message);
	 	  }
			  //CommonResourceManager.updateGoodsInfoData(goodsInfosList);	
    }
    public static void kcShopInfoHandler(JSONObject payloadData)
    {
    	int code= payloadData.getJSONObject("params").getIntValue("code");
	 	  String message=payloadData.getJSONObject("params").getString("message");
	 	  System.out.println(payloadData.toJSONString());
	 	  if(code==CommonCode.SUCCESS)
	 	  {
    	 	  JSONObject data=( payloadData.getJSONObject("params")).getJSONArray("data").getJSONObject(0);
    	 	  JSONObject basicInfo=data.getJSONObject("basic");
    	 	  String shopId=basicInfo.getString("shopId");
    	 	  ShopInfo.brandId=basicInfo.getInteger("brandId");
    	 	  ShopInfo.websiteAddr=basicInfo.getString("preQrcode");
    	 	  if(shopId!=null)
    	 	  {
    	 		 ShopInfo.shopId=shopId;
    	 		 GatewayInfo.setShopId(shopId);
    	 		 String storedShopId=Config.getAppInfo("shopId",GatewayInfo.runtimePath);
    	 		 if(storedShopId!=null)
    	 		 {
    	 			 if(!storedShopId.equals(shopId))
    	 			 {
    	 				 MqttHandler.getInstance().unSubscribeTopic("/mtt/shoptalk/"+storedShopId+"/#");
    	 			 }
    	 		 }
    	 		 Config.saveInfo("shopId", shopId, GatewayInfo.runtimePath);
    	 		 logger.debug("shop id is:"+ GatewayInfo.shopId);
      	 	 MqttHandler.getInstance().subscribeTopic("/mtt/shoptalk/"+shopId+"/change_epd_info");
      	 	 MqttHandler.getInstance().subscribeTopic("/mtt/shoptalk/"+shopId+"/reply/change_epd_info");
    	 	  }  
	 	 }
	 	  else
	 	  {
	 		 logger.error(message);
	 	  }
    }
    public static void shopConfigInfoRequest() {
    	if(shopType.equals("GLS"))
    	{
    		glsShopInfoRequest();
    	}
    	else
    	{
    		kcShopInfoRequest();
    	}
    }
    
   public static void glsShopInfoRequest()
   {
	   if(GatewayInfo.clientId==null)
			return ;
   	String goodsInfoRequestTopic="/mtt/shoptalk/"+GatewayInfo.clientId+"/shop_info_request";
   	JSONObject payloadJson = new JSONObject();
		payloadJson.put("deviceId", GatewayInfo.clientId);
		payloadJson.put("version", "1.0");
		payloadJson.put("time", Long.toString(System.currentTimeMillis()));
		payloadJson.put("method", "shop.shopInfoRequest");
		String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
		String uid=CommonFunc.encodeByMD5(originString);
		if(uid==null)
		{
			uid=Integer.toString((int)(1+Math.random()*100000000));
		}
		payloadJson.put("uid", uid);
		JSONObject paramsJson=new JSONObject();
		JSONArray  dataJsonArray=new JSONArray();
		paramsJson.put("data", dataJsonArray);
		payloadJson.put("params", paramsJson);
		logger.debug("shop info request");
       MqttMessage message = new MqttMessage(payloadJson.toJSONString().getBytes());
       int qos=2;
       message.setQos(qos);	        
       MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
       mqttEventMsg.mqttMsg=message;
       mqttEventMsg.topic=goodsInfoRequestTopic;	
       MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg);
   }
   
   public static void kcShopInfoRequest()
   {
	   if(GatewayInfo.clientId==null)
			return ;
   	String goodsInfoRequestTopic="/mtt/shoptalk/"+GatewayInfo.clientId+"/request/config";
   	JSONObject payloadJson = new JSONObject();
		payloadJson.put("deviceId", GatewayInfo.clientId);
		payloadJson.put("version", "1.0");
		payloadJson.put("time", Long.toString(System.currentTimeMillis()));
		payloadJson.put("method", "shop.requestConfig");
		String originString=GatewayInfo.clientId+Long.toString(System.currentTimeMillis())+Integer.toString((int)(1+Math.random()*1000));
		String uid=CommonFunc.encodeByMD5(originString);
		if(uid==null)
		{
			uid=Integer.toString((int)(1+Math.random()*100000000));
		}
		payloadJson.put("uid", uid.toUpperCase());
		JSONObject paramsJson=new JSONObject();
		JSONArray  dataJsonArray=new JSONArray();
		JSONObject itemsJson=new JSONObject();
		JSONArray itemJsonArrray =new JSONArray();
		itemJsonArrray.add("basic");
		itemsJson.put("item",itemJsonArrray);
		dataJsonArray.add(itemsJson);
		paramsJson.put("data", dataJsonArray);
		payloadJson.put("params", paramsJson);
		logger.debug("shop info request");
		System.out.println(payloadJson.toJSONString());
       MqttMessage message = new MqttMessage(payloadJson.toJSONString().getBytes());
       int qos=2;
       message.setQos(qos);	        
       MqttEventMsgBean mqttEventMsg=new MqttEventMsgBean();
       mqttEventMsg.mqttMsg=message;
       mqttEventMsg.topic=goodsInfoRequestTopic;	
       MqttMsgSendHandlerThread.appendMqttmsgQueue(mqttEventMsg);
   }

}
