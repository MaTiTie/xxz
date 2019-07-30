package serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TooManyListenersException;

public class Serial {
	
	//SerialPortEventListener portListener;
	public Serial() 
	{
		
	}
    public final SerialPort openPort(String portName, int baudrate)
            throws SerialPortParameterFailure, NotASerialPort, NoSuchPort,
            PortInUse {
        try {
            // ͨ���˿���ʶ��˿�
            CommPortIdentifier portIdentifier = CommPortIdentifier
                    .getPortIdentifier(portName);
            // �򿪶˿ڣ����ö˿�����timeout���򿪲����ĳ�ʱʱ�䣩
            CommPort commPort = portIdentifier.open(portName, 2000);
            // �ж��ǲ��Ǵ���
            if (commPort instanceof SerialPort) {
            	SerialPort serialPort = (SerialPort) commPort;
                try {
                    // ���ô��ڵĲ����ʵȲ���
                    serialPort.setSerialPortParams(baudrate,
                    		SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    		SerialPort.PARITY_NONE);  
                    serialPort.setInputBufferSize(1000000);
                   // SerialListener serialListener=new SerialListener();
                	//this.addListener(serialPort, serialListener);
                } catch (UnsupportedCommOperationException e) {
                    throw new SerialPortParameterFailure();
                }         
                return serialPort;
            } else {
                // ���Ǵ���
                throw new NotASerialPort();
            }
        } catch (NoSuchPortException e1) {
            throw new NoSuchPort();
        } catch (PortInUseException e2) {
            throw new PortInUse();
        }
        
    }

    public void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }
    
    public  void sendToPort(SerialPort serialPort,byte[] data,int length)
            throws SendDataToSerialPortFailure,
            SerialPortOutputStreamCloseFailure {
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(data,0,length);
            out.flush();
        } catch (IOException e) {
            throw new SendDataToSerialPortFailure();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                throw new SerialPortOutputStreamCloseFailure();
            }
        }
    }

    public void sendToPort(SerialPort serialPort, String order)
            throws SendDataToSerialPortFailure,
            SerialPortOutputStreamCloseFailure {
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(order.getBytes());
            out.flush();
        } catch (IOException e) {
            throw new SendDataToSerialPortFailure();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                throw new SerialPortOutputStreamCloseFailure();
            }
        }
    }
    public void setListenerToSerialPort(SerialPort serialPort, SerialPortEventListener listener) {
		try {
			//给串口添加事件监听
			serialPort.addEventListener(listener);
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		}
		serialPort.notifyOnDataAvailable(true);//串口有数据监听
		serialPort.notifyOnBreakInterrupt(true);//中断事件监听
	}

        

 public void readFromPort(SerialPort port) {
	 
 }
}

