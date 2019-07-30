package kc;

import brand.GoodsEatgInfoBean;

public class KcGoodsEtagInfoBean extends GoodsEatgInfoBean{
	public String location="";
	public String clerk="";
	public byte goodsGender=0;
	public KcGoodsEtagInfoBean() {
		for(int i=0;i<STOCK_MAX_LEN;i++) {
			stock[i]=0;
		}
	}
	public String toString()
	{
		StringBuilder sBuilder=new StringBuilder();
		sBuilder.append("number:"+number+"\r\n");
		sBuilder.append("retailPrice:"+retailPrice+"\r\n");
		sBuilder.append("suggRetailPrice:"+suggRetailPrice+"\r\n");
		sBuilder.append("qrcode:"+qrcode+"\r\n");
		sBuilder.append("goods_size:"+goodsSize+"\r\n");
		sBuilder.append("location:"+location+"\r\n");
		sBuilder.append("clerk:"+clerk+"\r\n");
		sBuilder.append("goodsGender:"+goodsGender+"\r\n");
		sBuilder.append("version:"+version+"\r\n");
		sBuilder.append("stock: ");
		for(int i=0;i<stockSizeCount;i++)
		{
			sBuilder.append(stock[i]+" ");
		}
		sBuilder.append("\r\n");
		return sBuilder.toString();
	}

}
