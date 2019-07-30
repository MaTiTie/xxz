package gls;

import brand.GoodsEatgInfoBean;

public class GlsGoodsEtagInfoBean extends GoodsEatgInfoBean{
	//public final int STOCK_MAX_LEN=30;
	public String number;
	public String color;
    public String toString()
	{
	   StringBuilder sBuilder=new StringBuilder();
	   sBuilder.append("style number:"+number+" ");
	   sBuilder.append("color:"+color+" ");
	   sBuilder.append("Size:"+goodsSize+" ");
	   sBuilder.append("retaiPrice:"+retailPrice+" ");
	   sBuilder.append("suggRetailPrice:"+suggRetailPrice+" ");
	   sBuilder.append("version:"+Integer.toString(version)+" ");
	   return sBuilder.toString();
	}
}
