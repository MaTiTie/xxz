package brand;

public class GoodsEatgInfoBean {
	public final int STOCK_MAX_LEN=30;
	public String number;
	public String goodsSize="";
	public String suggRetailPrice;
	public String retailPrice;
	public String qrcode="";
	public int version;	
	public int stockSizeCount=1;//at least one size
	public int[] stock=new int[STOCK_MAX_LEN];

}
