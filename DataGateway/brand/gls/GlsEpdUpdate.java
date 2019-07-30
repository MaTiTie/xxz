package gls;

public class GlsEpdUpdate {

	 public static  byte[] createEpdData(GlsGoodsEtagInfoBean goodsEtagInfo) {
			if(goodsEtagInfo==null)
				return null;
			int goodsNumberLen=goodsEtagInfo.number.length();
			byte[] goodsNumberData=goodsEtagInfo.number.getBytes();
			int goodsPriceLen=goodsEtagInfo.retailPrice.length();
			byte[] goodsPriceData=goodsEtagInfo.retailPrice.getBytes();
			int goodsQrcodeLen=goodsEtagInfo.qrcode.length();
			byte[] goodsQrcodeData=goodsEtagInfo.qrcode.getBytes();
			int goodsStockLen=1+1+goodsEtagInfo.stockSizeCount;
			byte[] goodsStockData=new byte[goodsStockLen];
			for(int i=0;i<goodsStockLen;i++)
			{
				goodsStockData[i]=0;
			}
			goodsStockData[0]=0;
			goodsStockData[1]=(byte) goodsEtagInfo.stockSizeCount;
			for(int i=0;i<goodsEtagInfo.stockSizeCount;i++)
			{
				goodsStockData[2+i]=(byte) goodsEtagInfo.stock[i];
			}
			int totalLen=1+goodsNumberLen+1+goodsPriceLen+1+goodsQrcodeLen+1+goodsStockLen+1;
			byte[] data=new byte[totalLen];
			data[0]=(byte) goodsEtagInfo.version;
			int index=1;
			
			data[index]=(byte) goodsNumberLen;
			index++;
			for(int i=0;i<goodsNumberLen;i++)
			{
				data[index+i]=goodsNumberData[i];
			}
			index+=goodsNumberLen;
			
			
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
			
			data[index]=(byte) goodsStockLen;
			index++;
			for(int i=0;i<goodsStockLen;i++)
			{
				data[index+i]=goodsStockData[i];
			}
			index+=goodsStockLen;	
			return data;		
		}
}
