package brand;

import java.util.Date;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import ble.SensorGoodsInfoUpdateBean;
import ble.SensorGoodsInfoUpdateHandler;
import gls.GlsEpdUpdate;
import gls.GlsGoodsEtagInfoBean;
import kc.KcEpdUpdate;
import kc.KcGoodsEtagInfoBean;
import shop.ShopInfo;
import util.StringUtil;

public class EpdInfoUpdateHandler {
	 private static  Logger logger = Logger.getLogger(EpdInfoUpdateHandler.class); // 1. create log  
	public static void epdInfoUpdateHandler(JSONObject updateData)
	{
		//System.out.println(updateData.toJSONString());
		long cmdTime=updateData.getLongValue("time");
		long current_time=new Date().getTime();
		System.out.print("epd update msg:"+cmdTime+"cu:"+current_time);
		if((current_time-cmdTime)>1000*60*60*3) //if cmd comes 3 hours later,then ignore the cmd
		{
			logger.error("epd update msg comes time out");
			return;
		}
		if(ShopInfo.shopType.equals("KC"))//brand kc
		   {	 		 
	 		  JSONObject data=( updateData.getJSONObject("params")).getJSONArray("data").getJSONObject(0);
	 		  int epdInfoLogId=data.getInteger("epdInfoLogId");
	 		  JSONArray goodsUpdateList=data.getJSONArray("detail");
	 		  for(int i=0;i<goodsUpdateList.size();i++)
	 		  {
	 			 JSONObject updateInfo=goodsUpdateList.getJSONObject(i);
	 			 int epdInfoLogDetailId=updateInfo.getInteger("epdInfoLogDetailId");
	 			 String sensorMacAddr=updateInfo.getString("hardwareMac");
	 			 String deviceType=updateInfo.getString("deviceType");
	 			 JSONObject goodsInfo=updateInfo.getJSONObject("stock");
	 			 KcGoodsEtagInfoBean goodsEtagInfoBean=new KcGoodsEtagInfoBean();
	 			 Integer retailPrice=goodsInfo.getInteger("retailPrice");
	 			 if(retailPrice!=null)
	 			 {
	 				 goodsEtagInfoBean.retailPrice=Integer.toString(retailPrice);
	 			 }
	 			 else {
	 				goodsEtagInfoBean.retailPrice="";
				}
	 			 Integer suggRetailPrice=goodsInfo.getInteger("suggRetailPrice");
	 			 if(suggRetailPrice==null)
	 			 {
	 				goodsEtagInfoBean.suggRetailPrice="N";
	 			 }
	 			 else
	 			 {
		 			 goodsEtagInfoBean.suggRetailPrice=Integer.toString(suggRetailPrice);
		 			 if(goodsEtagInfoBean.suggRetailPrice.length()==0||suggRetailPrice.intValue()==0)
		 			 {
		 				goodsEtagInfoBean.suggRetailPrice="N";
		 			 }
	 			 }
	 			 String lengthName=goodsInfo.getString("lengthName");
	 			 if(lengthName.length()==0)
	 			 {
	 				 lengthName="NN";
	 			 }
	 			 else
	 			 {
	 				 lengthName=lengthName.toLowerCase();
	 				 if(lengthName.contains("cm"))
	 				 {
	 					lengthName=lengthName.substring(0, lengthName.length()-2);
	 					 System.out.println("cut length name");
	 				 }
	 			 }
	 			 String sizeName=goodsInfo.getString("sizeName");
	 			 if(sizeName.length()==0)
	 			 {
	 				 sizeName="N";
	 			 }
	 			 goodsEtagInfoBean.goodsSize=sizeName+"-"+lengthName;
	 			 goodsEtagInfoBean.goodsSize="("+goodsEtagInfoBean.goodsSize+")";
	 			 if(goodsEtagInfoBean.goodsSize.length()<4)
	 			 {
	 				goodsEtagInfoBean.goodsSize="N";
	 			 }
	 			 goodsEtagInfoBean.number=goodsInfo.getString("barCode");
	 			 if(goodsEtagInfoBean.number==null)
	 			 {
	 				goodsEtagInfoBean.number="";
	 			 }
	 			 int gender=goodsInfo.getInteger("menWomenWareId");
	 			 if(gender==2)
	 			 {
	 				 goodsEtagInfoBean.goodsGender=0; 
	 			 }
	 			 else
	 			 {
	 				 goodsEtagInfoBean.goodsGender=1; 
	 			 }
	 			 goodsEtagInfoBean.clerk=goodsInfo.getString("staffName");
	 			 if(goodsEtagInfoBean.clerk.length()==0)
	 			 {
	 				goodsEtagInfoBean.clerk="NNN";
	 			 }
	 			 String shelves=goodsInfo.getString("shelves");
	 			 if(shelves.length()==0)
	 			 {
	 				 shelves="N";
	 			 }
	 			 String locationRegion=goodsInfo.getString("locationRegion");
	 			 if(locationRegion.length()==0)
	 			 {
	 				 locationRegion="N";
	 			 }
	 			 String floor=goodsInfo.getString("floor");
	 			 if(floor.length()==0)
	 			 {
	 				 floor="N";
	 			 }
	 			 goodsEtagInfoBean.location=floor+"-"+locationRegion+"-"+shelves;
	 			 goodsEtagInfoBean.location=StringUtil.filterChinese(goodsEtagInfoBean.location);
	 			 JSONArray stockInfo=updateInfo.getJSONArray("relatedQuantity");
	 			 goodsEtagInfoBean.stockSizeCount=stockInfo.size();
	 			 for(int j=0;j<stockInfo.size();j++)
	 			 {
	 				 goodsEtagInfoBean.stock[j]=stockInfo.getJSONObject(j).getInteger("value");
	 			 }
	 			 goodsEtagInfoBean.version=goodsInfo.getInteger("version");
	 			 if(ShopInfo.websiteAddr!=null)
	 			 {
	 				 goodsEtagInfoBean.qrcode=ShopInfo.websiteAddr+sensorMacAddr;
	 			 }
	 			 else
	 			 {
	 				goodsEtagInfoBean.qrcode="http://mttsmart.com/device/"+sensorMacAddr;
	 			 }
	 			 //System.out.println(goodsEtagInfoBean.toString());
	 			 SensorGoodsInfoUpdateBean goodsInfoUpdateBean=new SensorGoodsInfoUpdateBean();
	 			 goodsInfoUpdateBean.sensorMacAddr=sensorMacAddr;
	 			 goodsInfoUpdateBean.deviceType=deviceType;
	 			 goodsInfoUpdateBean.epdInfoLogDetailId=epdInfoLogDetailId;
	 			 goodsInfoUpdateBean.epdInfoLogId=epdInfoLogId;
	 			 goodsInfoUpdateBean.goodsEatgInfoBean=goodsEtagInfoBean;
	 			 SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.put(sensorMacAddr, goodsInfoUpdateBean);
	 		  } 		  
		   }
		else {
			JSONObject data=( updateData.getJSONObject("params")).getJSONArray("data").getJSONObject(0);
	        String uid=updateData.getString("uid");
	 		  JSONArray goodsUpdateList=data.getJSONArray("detail");
	 		  for(int i=0;i<goodsUpdateList.size();i++)
	 		  {
	 			 JSONObject updateInfo=goodsUpdateList.getJSONObject(i);
	 			// int epdInfoLogDetailId=updateInfo.getInteger("epdInfoLogDetailId");
	 			 String sensorMacAddr=updateInfo.getString("sensorId");
	 			 String deviceType=updateInfo.getString("sensorType");
	 			 JSONObject goodsInfo=updateInfo.getJSONObject("stock");
	 			 GlsGoodsEtagInfoBean goodsEtagInfoBean=new GlsGoodsEtagInfoBean();
	 			 Integer retailPrice=goodsInfo.getInteger("price");
	 			 if(retailPrice!=null)
	 			 {
	 				 goodsEtagInfoBean.retailPrice=Integer.toString(retailPrice);
	 			 }
	 			 else {
	 				goodsEtagInfoBean.retailPrice="";
				}
	 			 
	 			 goodsEtagInfoBean.number=goodsInfo.getString("styleNumber");
	 			 if(goodsEtagInfoBean.number==null)
	 			 {
	 				goodsEtagInfoBean.number="";
	 			 }
	 			
	 			 goodsEtagInfoBean.version=goodsInfo.getInteger("version");
	 			 
	 			 JSONArray stockInfo=updateInfo.getJSONArray("relatedQuantity");
	 			 goodsEtagInfoBean.stockSizeCount=stockInfo.size();
	 			 for(int j=0;j<stockInfo.size();j++)
	 			 {
	 				 goodsEtagInfoBean.stock[j]=stockInfo.getJSONObject(j).getInteger("value");
	 			 }
	 			 
	 			 if(ShopInfo.websiteAddr!=null)
	 			 {
	 				 goodsEtagInfoBean.qrcode=ShopInfo.websiteAddr+sensorMacAddr;
	 			 }
	 			 else
	 			 {
	 				goodsEtagInfoBean.qrcode="http://mttsmart.com/device/"+sensorMacAddr;
	 			 }
	 			 System.out.println(goodsEtagInfoBean.toString());
	 			 SensorGoodsInfoUpdateBean goodsInfoUpdateBean=new SensorGoodsInfoUpdateBean();
	 			 goodsInfoUpdateBean.sensorMacAddr=sensorMacAddr;
	 			 goodsInfoUpdateBean.deviceType=deviceType;
                 goodsInfoUpdateBean.uid=uid;
	 			 goodsInfoUpdateBean.goodsEatgInfoBean=goodsEtagInfoBean;
	 			 SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.put(sensorMacAddr, goodsInfoUpdateBean);			
		    }
		}
	}
    public static byte[] creatEpdData(GoodsEatgInfoBean goodsEtagInfo)
    {
    	if(ShopInfo.shopType.equals("KC"))//brand kc
		   {
		       return KcEpdUpdate.createEpdData((KcGoodsEtagInfoBean)goodsEtagInfo);
		   }
    	else 
    	  {
    		   return GlsEpdUpdate.createEpdData((GlsGoodsEtagInfoBean)goodsEtagInfo);
    	  }
    	
    }
}
