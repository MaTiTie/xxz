package ble;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import brand.GoodsEatgInfoBean;
import util.CommonCode;

public class SensorGoodsInfoUpdateBean {
	
 public String sensorMacAddr;
 public String uid;
 public String deviceType;
 public int epdInfoLogDetailId;
 public int epdInfoLogId;
 private static final int updateGoodsInfoTimeoutTime=1000*60*60*3;//3hour
 private static Timer updateGoodsInfoUseTimer;
 public ArrayList<String> errorRecord=new ArrayList<>();
 public TimerTask  updateGoodsInfoTimeout;
 public GoodsEatgInfoBean goodsEatgInfoBean;
 //public SensorGoodsInfoUpdateBean sensorGoodsInfoUpdateBean;
 public SensorGoodsInfoUpdateBean() {
		// TODO Auto-generated constructor stub
	    //sensorGoodsInfoUpdateBean=this;
		if(updateGoodsInfoUseTimer==null) {
			updateGoodsInfoUseTimer=new Timer();
		}
		this.updateGoodsInfoTimeout=new TimerTask() {		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				errorRecord.add("goods update timer timeOut");
				SensorGoodsInfoUpdateHandler.reportSensorGoodsInfoUpdate(SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.get(sensorMacAddr),CommonCode.UPDATE_ETAG_TIMEOUT);
				SensorGoodsInfoUpdateHandler.neededUpdateGoodsInfoSensorList.remove(sensorMacAddr);
			}
		};
		updateGoodsInfoUseTimer.schedule(this.updateGoodsInfoTimeout, updateGoodsInfoTimeoutTime);
	}

}
