package ble;
import java.util.Date;

public class SensorAdvDataBean {
	
	public static final int SENSOR_STATE_NOT_INIT_NOT_USE=0;
	public static final int SENSOR_STATE_CHARGING=1;
	public static final int SENSOR_STATE_CHARGED=2;
	public static final int SENSOR_STATE_NORMAL_WORK=3;
	public static final int SENSOR_STATE_INIT_NOT_USE=4;
	public static final int SENSOR_DATA_TYPE_HEARTRATE=3;
	public static final int SENSOR_DATA_TYPE_GSENSOR=1;
	public static final int SENSOR_DATA_TYPE_CHECK_STOCK=2;
	public static final int SENSOR_DATA_TYPE_CHARGE=4;
    int sensorState;
    int sensorType;
    int firmwareVersion;
	int advCount;
	int sensorDataType;
	String sensorMacAddr="";
	int[] sensorData=new int[27];
	int batteryLevle=0xff;
	int goodsInfoVersion=0;
	int signalIntensity=255;
	int[] remarks=new int[4];
	//int[] gSensorData=new int[27];
    long timestamp=new Date().getTime();
	public String toString()
	{
		String toStr="";
		toStr+="mac addr:"+this.sensorMacAddr+"\r\n";
		if(sensorState==0x00)
		{
			toStr+="sensorState:"+"not init not use"+"\r\n";
		}
		else if(sensorState==0x01)
		{
			toStr+="sensorState:"+"charging"+"\r\n";
		}
		else if(sensorState==0x02)
		{
			toStr+="sensorState:"+"charged"+"\r\n";
		}
		else if(sensorState==0x03)
		{
			toStr+="sensorState:"+"normal work"+"\r\n";
		}
		else if(sensorState==0x04)
		{
			toStr+="sensorState:"+"init not use"+"\r\n";
		}
		toStr+="sensorType:"+this.sensorType+"\r\n";
		toStr+="fmVer:"+this.firmwareVersion+"\r\n";
		toStr+="advCount:"+this.advCount+"\r\n";
		
		if(sensorDataType==0x03)
		{
		  toStr+="heartrate data:"+"battery_levle:"+this.sensorData[0]+"\r\n";
		}
		else if(sensorDataType==0x02)
		{
			toStr+="check stock data:";
					for(int i=0;i<10;i++)
					{
						toStr+=sensorData[i]+" ";
					}
					toStr+="\r\n";
		}
		else if(sensorDataType==0x04)
		{
			toStr+="charge data:"+"charge state:"+this.sensorData[0]+"\r\n";
		}
		else {
			toStr+="g-sensor data\r\n";
		}
		toStr+="batteryLevle:"+this.batteryLevle+"\r\n";
		toStr+="epdVer:"+this.goodsInfoVersion+"\r\n";
		toStr+="signalIntensity:"+this.signalIntensity+"\r\n";
		toStr+="timestamp:"+this.timestamp;
		return toStr;
	}
	
	public SensorAdvDataBean()
	{
		
	}
	
	public static void main(String[] args) {
		//SensorAdvData data=new SensorAdvData();
		//data.time=new Date();
		//System.out.println(data.time.getTime());
		for(int i=0x20;i<0x7f;i++)
		{
			System.out.print((char)i);
		}
	}
}
