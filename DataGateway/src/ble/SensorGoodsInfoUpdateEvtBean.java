package ble;

import java.util.TimerTask;

public class SensorGoodsInfoUpdateEvtBean {
	public boolean execCmd=false;
	public byte currentCmdType;
	public byte[] SensorMacAddr;
	public String  SensorMacAddrString;
	public byte[] goodsInfoData;
	public int goodsInfoDataLength;
	public int timeoutTime=6000;
	public volatile int execErrorCount;
   // public volatile boolean connectSensorExecResult;
  //  public volatile boolean sendDataExecResult;
  //  public volatile boolean disconnectSensorExecResult;
    public volatile boolean sensorGoodsUpdateExecResult;
    public TimerTask cmdExecTimerTask;
}
