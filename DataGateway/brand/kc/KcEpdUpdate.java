package kc;

import serial.SendDataToSerialPortFailure;
import serial.SerialPortOutputStreamCloseFailure;
import util.SerialCmd;

public class KcEpdUpdate {
	
	
	public static  byte[] createEpdData(KcGoodsEtagInfoBean goodsEtagInfo) {
		if(goodsEtagInfo==null)
			return null;
		int goodsNumberLen=goodsEtagInfo.number.length();
		byte[] goodsNumberData=goodsEtagInfo.number.getBytes();
		
		int goodsBasePriceLen=goodsEtagInfo.suggRetailPrice.length();
		byte[] goodsBasePriceData=goodsEtagInfo.suggRetailPrice.getBytes();
		
		int goodsPriceLen=goodsEtagInfo.retailPrice.length();
		byte[] goodsPriceData=goodsEtagInfo.retailPrice.getBytes();
		
		int goodsQrcodeLen=goodsEtagInfo.qrcode.length();
		byte[] goodsQrcodeData=goodsEtagInfo.qrcode.getBytes();
		
		int goodsSizeLen=goodsEtagInfo.goodsSize.length();
		byte[] goodsSizerData=goodsEtagInfo.goodsSize.getBytes();
		
		int locationLen=goodsEtagInfo.location.length();
		byte[] locationData=goodsEtagInfo.location.getBytes();
		
		int clerkLen=goodsEtagInfo.clerk.length();
		byte[] clerkData=goodsEtagInfo.clerk.getBytes();
		
		int goodsStockLen=goodsEtagInfo.stockSizeCount;

		byte[] goodsStockData=new byte[goodsStockLen];
		for(int i=0;i<goodsStockLen;i++)
		{
			goodsStockData[i]=(byte)goodsEtagInfo.stock[i];
		}
		
		int totalLen=2+goodsNumberLen+1+goodsBasePriceLen+1+goodsPriceLen+1+goodsQrcodeLen+1+goodsSizeLen+1+locationLen+1+clerkLen+1+goodsStockLen+1;
		byte[] data=new byte[totalLen];
		data[0]=(byte) goodsEtagInfo.version;
		data[1]=goodsEtagInfo.goodsGender;
		int index=2;	
		data[index]=(byte) goodsNumberLen;
		index++;
		for(int i=0;i<goodsNumberLen;i++)
		{
			data[index+i]=goodsNumberData[i];
		}
		index+=goodsNumberLen;
		
		data[index]=(byte) goodsBasePriceLen;
		index++;
		for(int i=0;i<goodsBasePriceLen;i++)
		{
			data[index+i]=goodsBasePriceData[i];
		}
		index+=goodsBasePriceLen;
		
		data[index]=(byte) goodsPriceLen;
		index++;
		for(int i=0;i<goodsPriceLen;i++)
		{
			data[index+i]=goodsPriceData[i];
		}
		index+=goodsPriceLen;
		
		data[index]=(byte) goodsQrcodeLen;
		index++;
		for(int i=0;i<goodsQrcodeLen;i++)
		{
			data[index+i]=goodsQrcodeData[i];
		}
		index+=goodsQrcodeLen;
		
		data[index]=(byte) goodsSizeLen;
		index++;
		for(int i=0;i<goodsSizeLen;i++)
		{
			data[index+i]=goodsSizerData[i];
		}
		index+=goodsSizeLen;
		
		data[index]=(byte) locationLen;
		index++;
		for(int i=0;i<locationLen;i++)
		{
			data[index+i]=locationData[i];
		}
		index+=locationLen;
		
		data[index]=(byte) clerkLen;
		index++;
		for(int i=0;i<clerkLen;i++)
		{
			data[index+i]=clerkData[i];
		}
		index+=clerkLen;
		
		data[index]=(byte) goodsStockLen;
		index++;
		for(int i=0;i<goodsStockLen;i++)
		{
			data[index+i]=goodsStockData[i];
		}
		index+=goodsStockLen;	
		return data;		
	}
	
	public static void main(String[] args) throws SendDataToSerialPortFailure, SerialPortOutputStreamCloseFailure {
		
		KcGoodsEtagInfoBean goodsEtagInfoBean=new KcGoodsEtagInfoBean();
		goodsEtagInfoBean.version=10;
		goodsEtagInfoBean.goodsGender=0;
		goodsEtagInfoBean.number="7565664663636";
		goodsEtagInfoBean.retailPrice="12345";
		goodsEtagInfoBean.suggRetailPrice="333";
		goodsEtagInfoBean.qrcode="http://mttsmart.com/123456789012";
		goodsEtagInfoBean.goodsSize="3XL-889";
		goodsEtagInfoBean.location="N-NNN-N";
		goodsEtagInfoBean.clerk="NNN";
		goodsEtagInfoBean.stockSizeCount=8;
		for(int i=0;i<goodsEtagInfoBean.stockSizeCount;i++)
		{
			goodsEtagInfoBean.stock[i]=i;
		}
		byte[] data=createEpdData(goodsEtagInfoBean);
		SerialCmd.setUartCmd(null,SerialCmd.UPDATE_ETAG_GOODS_INFO_EVT , data, data.length);
	}
}
