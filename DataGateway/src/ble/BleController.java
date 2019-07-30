package ble;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import org.apache.log4j.Logger;

import app.GatewayInfo;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import serial.NoSuchPort;
import serial.NotASerialPort;
import serial.PortInUse;
import serial.SendDataToSerialPortFailure;
import serial.Serial;
import serial.SerialPortOutputStreamCloseFailure;
import serial.SerialPortParameterFailure;
import util.CommonFunc;
import util.SerialCmd;

public class BleController extends Thread{
	private SerialPort serialPort=null;
	private String baurateArg=null;
	private static volatile boolean serialShakehandsOk=false;
	public static volatile int checkDataIndex=0;
	private static volatile int checkDataLength=0;
    //private int[] bufferData=new int[10000];
    private byte[] readBuffer=new byte[10000];
    private int[] serialDataBuffer=new int[10000];
    public static volatile boolean bleControllerStateCheck=false;
    InputStream in = null;
    boolean receiveStart=false;
	public String bleAdapterMacAddr=null;
    private  static Logger logger = Logger.getLogger(BleController.class); // 1. create log  
	@Override
	public void run() {
		if(openSerialPort()) 
		{			
             SensorGoodsInfoUpdateHandler.setSerialPort(serialPort);
		}
	}
	public void close() {
		serialShakehandsOk=false;
		checkDataIndex=0;
		checkDataLength=0;
		bleControllerStateCheck=false;
        receiveStart=false;
		if(serialPort!=null)
		{
			serialPort.removeEventListener();
			serialPort.close();
			serialPort=null;
		}	
	}
	
	public boolean openSerialPort()
    {	
		Enumeration<?>portList = CommPortIdentifier.getPortIdentifiers();// 閫氳繃涓插彛閫氫俊绠＄悊绫昏幏寰楀綋鍓嶈繛鎺ヤ笂鐨勪覆鍙ｅ垪琛�
		if(GatewayInfo.systemOs.startsWith("Win"))
		{
			while (portList.hasMoreElements()&&portList!=null)
			{
				CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();// 鑾峰彇鐩稿簲涓插彛瀵硅薄
				Serial  serial=new Serial();
				if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {// 鍒ゆ柇绔彛绫诲瀷鏄惁涓轰覆鍙�
					  logger.debug("璁惧绫诲瀷锛�" + portId.getPortType() + " <----> " + "璁惧鍚嶇О锛�" + portId.getName());		  				  
					  try {
					    int baudrate=115200;
						if(baurateArg!=null)
						{
							baudrate=Integer.parseInt(baurateArg);
						}
						
						serialPort=serial.openPort(portId.getName(), baudrate);
						serial.setListenerToSerialPort(serialPort, new SerialPortEventListener() {
				    		@Override
				    		public void serialEvent(SerialPortEvent arg0) {
				    			if(arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {//鏁版嵁閫氱煡
				    				try
				    				{
				    					readPort();	
				    				}catch (Exception e) {
										// TODO: handle exception
				    					e.printStackTrace();
				    					serialShakehandsOk=false;
				    					checkDataIndex=0;
				    					checkDataLength=0;
				    					bleControllerStateCheck=false;
				    			        receiveStart=false;
				    					if(serialPort!=null)
				    					{
				    						serialPort.close();
				    						serialPort.removeEventListener();
				    						serialPort=null;
				    					}	
									}
				    			}
				    		}
				    	});
						
					} catch (SerialPortParameterFailure | NotASerialPort | NoSuchPort | PortInUse e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						serial.closePort(serialPort);
						continue;
					}
					
					if(serialPort!=null)
					{
						try {
							 SerialCmd.setUartCmd(serialPort,SerialCmd.SHAKE_HAND_EVT , null, 0);
						} catch (SendDataToSerialPortFailure | SerialPortOutputStreamCloseFailure e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							serial.closePort(serialPort);
							continue;
						}
						try {
							Thread.sleep(4000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							logger.error(e.toString());
						}
						if(serialShakehandsOk==true)
						{
							//bleControllerStateCheck=true;
							return true;
						}
						else
						{
							serialPort.removeEventListener();
							serial=null;
							this.close();
						}
					}
				}
			}
		}
		else
		{
			
		  Serial  serial=new Serial();  	
		  try 
			{
		  serialPort=serial.openPort("/dev/ttyS1", 115200);
		  logger.debug("ttyS1");
			serial.setListenerToSerialPort(serialPort, new SerialPortEventListener() {
	    		@Override
	    		public void serialEvent(SerialPortEvent arg0) {
	    			if(arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {//鏁版嵁閫氱煡
	    				try
	    				{
	    					readPort();	
	    				}catch (Exception e) {
							// TODO: handle exception
	    					e.printStackTrace();
	    					serialShakehandsOk=false;
	    					checkDataIndex=0;
	    					checkDataLength=0;
	    					bleControllerStateCheck=false;
	    			        receiveStart=false;
	    					if(serialPort!=null)
	    					{
	    						serialPort.close();
	    						serialPort.removeEventListener();
	    						serialPort=null;
	    					}	
						}
	    			}
	    		}
	    	});
			}
		   catch (Exception e) {
					// TODO: handle exception
					  e.printStackTrace();
					  serial.closePort(serialPort);
					 // break;
				}
		  
		  if(serialPort!=null)
			{
				try {
					 SerialCmd.setUartCmd(serialPort,SerialCmd.SHAKE_HAND_EVT , null, 0);
				} catch (SendDataToSerialPortFailure | SerialPortOutputStreamCloseFailure e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					serial.closePort(serialPort);
				}
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e.toString());
				}
				if(serialShakehandsOk==true)
				{
					//bleControllerStateCheck=true;
					return true;
				}
				else
				{
					serialPort.removeEventListener();
					serial=null;
					this.close();
				}
			}
				  
		}
		logger.error("can not find data collector!");
		return false;
    }
	public void handle_cmd(int[] cmd,int length) {
		if(SerialCmd.verfyUartCmd(length,cmd))
    	{
    		if(!SerialCmd.getCmdFromIndex(cmd[3]).equals("SENSOR_ADV_DATA_EVT"))
    		{
    			if(SerialCmd.getCmdFromIndex(cmd[3]).equals("SHAKE_HAND_EVT"))
    			{
    				if(cmd[5]==0x01)
    				{
    					logger.debug("this device is sensor manager");
    				}
    				else if(cmd[5]==0x02||cmd[5]==0x03)
    				{
    					byte[] mac=new byte[6];
    					mac[0]=(byte) cmd[6];
    					mac[1]=(byte) cmd[7];
    					mac[2]=(byte) cmd[8];
    					mac[3]=(byte) cmd[9];
    					mac[4]=(byte) cmd[10];
    					mac[5]=(byte) cmd[11];
    					bleAdapterMacAddr=CommonFunc.convertBytesToHexString(mac);
    					logger.debug("this device is ble controller");
    					GatewayInfo.setClientId(bleAdapterMacAddr);
    					serialShakehandsOk=true;
    					bleControllerStateCheck=true;
    					if(length==15)
    					{
	    					//System.out.println("len:"+length);
	    					byte fmHigh= (byte) ((byte)cmd[12]&0xf0);
	    					byte fmLow=(byte) ((byte)cmd[12]&0x0f);
	    					GatewayInfo.bleAdapterFirmwareVersion=Integer.toString(fmHigh)+"."+Integer.toString(fmLow);
	    					//System.out.println("ble fm:"+GatewayInfo.bleAdapterFirewareVersion);
    					}   					
    				}
    				else {
    					logger.debug("wrong device");
					}
    			}
    			else
    			{
    				UartBleDataBean uartBleDataBean=new UartBleDataBean();
        			uartBleDataBean.data=cmd;
        			uartBleDataBean.len=length;
        			BleDataHandlerThread.appendUartBleDataList(uartBleDataBean);
    			}
    		}
    		else
    		{
    			//logger.debug("add uart cmd");
    			UartBleDataBean uartBleDataBean=new UartBleDataBean();
    			uartBleDataBean.data=cmd;
    			uartBleDataBean.len=length;
    			BleDataHandlerThread.appendUartBleDataList(uartBleDataBean);
    		}
    	}
    	else
    	{
    		logger.error("verfy cmd error:");     		
    		checkDataLength=0;
    		checkDataIndex=0;
    		receiveStart=false;
    	}	
	}
	public void handle_data(byte[] data,int length) {
		for(int i=0;i<length;i++)
		{
			serialDataBuffer[checkDataIndex]=((int)data[i]&0x000000ff);
			checkDataIndex++;
		}	
		if(checkDataIndex>=3)
		{
			if(serialDataBuffer[0]==0xA5)
			{
				checkDataLength=serialDataBuffer[1]*256+serialDataBuffer[2];
				if(checkDataLength>255) //cmd length error ,this is not a vailid 0xa5
				{
					checkDataIndex-=1;
					for(int i=0;i<checkDataIndex;i++)
					{
						serialDataBuffer[i]=serialDataBuffer[i+1];
					}
					logger.error("cmd length error");
					return ;
				}
				if(checkDataIndex>=checkDataLength+3)
				{
					int[] TempData =new int[checkDataLength+3];
					//System.out.print("get cmd:");
					for(int i=0;i<checkDataLength+3;i++)
					{
						TempData[i]=serialDataBuffer[i];
						//System.out.print(String.format("%02x", TempData[i]));
					}
					//System.out.print("\r\n");
					//System.out.println("index:"+checkDataIndex+"len:"+checkDataLength);
					//remove cmd from the buffer
					checkDataIndex-=(checkDataLength+3);
					for(int i=0;i<checkDataIndex;i++)
					{
						serialDataBuffer[i]=serialDataBuffer[i+checkDataLength+3];
					}
					handle_cmd(TempData,checkDataLength+3);	
					if(checkDataIndex>3)
					{
						if(serialDataBuffer[0]==0xA5)
						{
							int len=serialDataBuffer[1]*256+serialDataBuffer[2];
							if(checkDataIndex>=len+3)
							{
								handle_data(null,0);
							}
						}
					}
				}
			}
			else
			{
				int count=0;// find 0xa5
				for(int i=0;i<checkDataIndex;i++)
				{
					if(serialDataBuffer[i]==0xA5)
					{
						break;
					}
					else
					{
						count++;
					}
				}
				checkDataIndex-=count;
				for(int i=0;i<checkDataIndex;i++)
				{
					serialDataBuffer[i]=serialDataBuffer[i+count];
				}
			}
		}
	}

	public void readPort()
		{		
		     try {	
		    	    in = serialPort.getInputStream();
		            //int bufflenth = in.available();	
		            int numBytes=0;
		            if(in.available()>0)
		            {
		            	numBytes = in.read(readBuffer);
		            }
//		            for(int i=0;i<numBytes;i++)
//	        		{
//	        			System.out.print(String.format("%02x", readBuffer[i]));
//	        			
//	        		}
		            handle_data(readBuffer,numBytes);		
		        }
		        catch (IOException e) {
					// TODO: handle exception
		        	logger.error("data collector read uart error");
		        	this.close();
					e.printStackTrace();
					logger.error(e.toString());
				}
		}
	public static void main(String[] args) {
		BleController bleController=new BleController();
		bleController.start();
	}
	
	
}
