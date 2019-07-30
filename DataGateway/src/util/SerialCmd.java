package util;
import java.io.IOException;
import java.io.OutputStream;
import gnu.io.SerialPort;
import serial.*;

public class SerialCmd {
		public static final byte STOP_ADV_EVT = 0x01;
		public static final byte START_ADV_EVT = 0x02;
		public static final byte ENABLE_CCCD_EVT = 0x03;
		public static final byte SCAN_START_EVT = 0x04;
		public static final byte DEVICE_RESET_EVT = 0x05;
		public static final byte SHAKE_HAND_EVT = 0x06;
		public static final byte RESET_DEVICE_EVT = 0x07;
		public static final byte CONNECT_SENSOR_EVT = 0x08;
		public static final byte SEND_EPD_DATA_EVT = 0x09;
		public static final byte SEND_FINGERPRINT_DATA_EVT = 0x0A;
		public static final byte DISCONNECT_SENSOR_EVT = 0x0B;
		public static final byte RESET_SENSOR_EVT = 0x0C;
		public static final byte SEND_STOCK_DATA_EVT = 0x0D;
		public static final byte CMD_REPORT_EVT = 0x0E;
		public static final byte BLE_CMD_RECEIVE_ACK = 0x0F;
		public static final byte ETAG_CONFIG_OVER_EVT = 0x10;
		public static final byte ETAG_SEIF_TEST_EVT = 0x11;
		public static final byte DELETE_FP_USER_EVT = 0x12;
		public static final byte SENSOR_CONTROLLER_HR_EVT = 0x13;
		public static final byte SET_ETAG_GOODS_INFO_EVT=0x14;
		public static final byte UPDATE_ETAG_GOODS_INFO_EVT=0x15;
		// -----------------------------------------------------------------
		public static final String STOP_ADV_EVT_STR = "STOP_ADV_EVT";
		public static final String START_ADV_EVT_STR = "START_ADV_EVT";
		public static final String ENABLE_CCCD_EVT_STR = "ENABLE_CCCD_EVT";
		public static final String SCAN_START_EVT_STR = "SCAN_START_EVT";
		public static final String DEVICE_RESET_EVT_STR = "DEVICE_RESET_EVT";
		public static final String SHAKE_HAND_EVT_STR = "SHAKE_HAND_EVT";
		public static final String RESET_DEVICE_EVT_STR = "RESET_DEVICE_EVT";
		public static final String CONNECT_SENSOR_EVT_STR = "CONNECT_SENSOR_EVT";
		public static final String SEND_EPD_DATA_EVT_STR = "SEND_EPD_DATA_EVT";
		public static final String SEND_FINGERPRINT_DATA_EVT_STR = "SEND_FINGERPRINT_DATA_EVT";
		public static final String DISCONNECT_SENSOR_EVT_STR = "DISCONNECT_SENSOR_EVT";
		public static final String RESET_SENSOR_EVT_STR = "RESET_SENSOR_EVT";
		public static final String SEND_STOCK_DATA_EVT_STR = "SEND_STOCK_DATA_EVT";
		public static final String CMD_REPORT_EVT_STR = "CMD_REPORT_EVT";
		public static final String BLE_CMD_RECEIVE_ACK_STR = "BLE_CMD_RECEIVE_ACK";
		public static final String ETAG_CONFIG_OVER_EVT_STR = "ETAG_CONFIG_OVER_EVT";
		public static final String BLE_CMD_RECEIVE_ACK_EVT_STR = "BLE_CMD_RECEIVE_ACK_EVT";
		public static final String SENSOR_ADV_DATA_EVT_STR = "SENSOR_ADV_DATA_EVT";
		public static final String SENSOR_CONTROLLER_HR_EVT_STR = "SENSOR_CONTROLLER_HR_EVT";
		public static final String SET_ETAG_GOODS_INFO_EVT_STR="SET_ETAG_GOODS_INFO_EVT";
		public static final String UPDATE_ETAG_GOODS_INFO_EVT_STR="UPDATE_ETAG_GOODS_INFO_EVT";
		public static final String UNKOWN_CMD_STR = "UNKOWN_CMD";
		public static final String COMMAND_NULL_EVT_STR = "COMMAND_NULL_EVT";
		// -----------------------------------------------------------------
		public static final String CMD_SUCCESS = "CMD_SUCCESS";
		public static final String CMD_FAIL = "CMD_FAIL";
		public static final String CMD_NO_CONNECT_SENSOR = "CMD_NO_CONNECT_SENSOR";
		public static final String CMD_DISCONNECT = "CMD_DISCONNECT";
		public static final String CMD_CONNECT_TIME_OUT = "CMD_CONNECT_TIME_OUT";
		public static final String CMD_ALREADY_CONNECT = "CMD_ALREADY_CONNECT";
		public static final String CMD_ACK = "CMD_ACK";
		public static final String CMD_FORMAT_ERROR = "CMD_FORMAT_ERROR";
		public static final String CMD_EXEC_TIME_OUT = "CMD_EXEC_TIME_OUT";
		public static final String CMD_NOT_EXEC_OVER = "CMD_NOT_EXEC_OVER";
		public static final String CMD_RECEIVE_TIMEOUT = "CMD_RECEIVE_TIMEOUT";
		public static final String CMD_FP_REGIST_FAIL = "CMD_FP_REGIST_FAIL";
		public static final String CMD_FP_RIGIST_ERROR = "CMD_FP_RIGIST_ERROR";
		public static final String CMD_NOT_SUPPORT    =  "CMD_NOT_SUPPORT";
		public static final String CMD_BATTERY_LOW   =    "CMD_BATTERY_LOW";
		public static final String CMD_UNKOWN = "CMD_UNKOWN";
	
		public static final byte BLE_CMD_SUCCESS=0x00;
		public static final byte BLE_CMD_FAIL=0x01;
		public static final byte BLE_CMD_NO_CONNECT_SENSOR=0x02;
		public static final byte BLE_CMD_DISCONNECT=0x03;
		public static final byte BLE_CMD_CONNECT_TIME_OUT=0x04;
		public static final byte BLE_CMD_ALREADY_CONNECT=0x05;
		public static final byte BLE_CMD_ACK=0x06;
		public static final byte BLE_CMD_FORMAT_ERROR=0x07;
		public static final byte BLE_CMD_EXEC_TIME_OUT=0x08;
		public static final byte BLE_CMD_NOT_EXEC_OVER=0x09;
		public static final byte BLE_RECEIVE_TIME_OUT=0x0A;
		public static final byte BLE_CMD_FP_RIGIST_FAIL=0x0B;
		public static final byte BLE_CMD_FP_RIGIST_ERROR=0x0C;
		public static final byte BLE_CMD_NOT_SUPPORT    =   0x0D;
		public static final byte BLE_CMD_BATTERY_LOW   =    0x0E;
		
		/**
		 * 检查cmd命令
		 * 
		 * @param length
		 * @param bufferData
		 * @return
		 */
		public static boolean verfyUartCmd(int length, int[] bufferData) {
			boolean result = false;
			int chk = 0;
			for (int i = 1; i < length - 2; i++) {
				chk += (int) (bufferData[i] & 0xFF);
			}
			int chk1 = (chk >> 8) & 0xff;
			int chk2 = (chk) & 0xff;
			// ObserverGui.logoInfo(""+chk1+":"+chk2+":"+chk);
			if (chk1 == bufferData[length - 2] && chk2 == bufferData[length - 1]) {
				result = true;
			}
			return result;
		}

		/**
		 * 生成cmd命令
		 * 
		 * @param cmdType
		 * @param cmdData
		 * @param cmdDataLength
		 * @throws SendDataToSerialPortFailure
		 * @throws SerialPortOutputStreamCloseFailure
		 */
		public static void setUartCmd(SerialPort serialPort, byte cmdType, byte[] cmdData, int cmdDataLength)
				throws SendDataToSerialPortFailure, SerialPortOutputStreamCloseFailure {
			/*
			 * for(int i=0;i<cmdDataLength;i++) { System.out.println(cmdData[i]); }
			 */
			byte[] uartCmd = new byte[6000];
			uartCmd[0] = (byte) 0xA5;
			uartCmd[3] = cmdType;
			for (int i = 0; i < cmdDataLength; i++) {
				uartCmd[4 + i] = cmdData[i];
			}
			int cmdLen = cmdDataLength + 2 + 1;
			uartCmd[1] = (byte) ((cmdLen >> 8) & 0xff);
			uartCmd[2] = (byte) ((cmdLen) & 0xff);
			int chk32 = 0;
			for (int i = 1; i <= cmdLen; i++) {
				chk32 += (int) (uartCmd[i] & 0xff);
			}
			uartCmd[cmdLen + 1] = (byte) ((chk32 >> 8) & 0xff);
			// System.out.println(chk32+":"+uartCmd[cmdLen+1]);
			uartCmd[cmdLen + 2] = (byte) (chk32 & 0xff);
			
			// for(int i=0;i<cmdLen+3;i++) { System.out.println(uartCmd[i]); }
			byte[] dataBytes=new byte[cmdLen+3];
			for(int i=0;i<cmdLen+3;i++) {dataBytes[i]=uartCmd[i];};
			 String dataString=CommonFunc.convertBytesToHexString(dataBytes);
			 System.out.println(dataString);
			sendToPort(serialPort, uartCmd, cmdLen + 3);
		}

		/**
		 * 获取cmd数据
		 * 
		 * @param uartData
		 * @param length
		 * @return
		 */
		public static int[] getCmdData(int[] uartData, int length) {
			int[] cmdData = new int[length];
			for (int i = 0; i < length; i++) {
				cmdData[i] = uartData[4 + i];
			}
			return cmdData;
		}

		/**
		 * 生成命令后顺序调用直接发送
		 * 
		 * @param data
		 * @param length
		 * @throws SendDataToSerialPortFailure
		 * @throws SerialPortOutputStreamCloseFailure
		 */
		synchronized private static void sendToPort(SerialPort serialPort, byte[] data, int length)
				throws SendDataToSerialPortFailure, SerialPortOutputStreamCloseFailure {
			OutputStream out = null;
			try {
				out = serialPort.getOutputStream();
				out.write(data, 0, length);
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

		public static String getCmdFromIndex(int index) {
			String cmd = "";
			switch (index) {
			case 0:
				cmd = COMMAND_NULL_EVT_STR;
				break;
			case 1:
				cmd = STOP_ADV_EVT_STR;
				break;
			case 2:
				cmd = START_ADV_EVT_STR;
				break;
			case 3:
				cmd = ENABLE_CCCD_EVT_STR;
				break;
			case 4:
				cmd = SCAN_START_EVT_STR;
				break;
			case 5:
				cmd = DEVICE_RESET_EVT_STR;
				break;
			case 6:
				cmd = SHAKE_HAND_EVT_STR;
				break;
			case 7:
				cmd = RESET_DEVICE_EVT_STR;
				break;
			case 8:
				cmd = CONNECT_SENSOR_EVT_STR;
				break;
			case 9:
				cmd = SEND_EPD_DATA_EVT_STR;
				break;
			case 0x0a:
				cmd = SEND_FINGERPRINT_DATA_EVT_STR;
				break;
			case 0x0b:
				cmd = DISCONNECT_SENSOR_EVT_STR;
				break;
			case 0x0c:
				cmd = RESET_SENSOR_EVT_STR;
				break;
			case 0x0d:
				cmd = SEND_STOCK_DATA_EVT_STR;
				break;
			case 0x0e:
				cmd = CMD_REPORT_EVT_STR;
				break;
			case 0x0f:
				cmd = BLE_CMD_RECEIVE_ACK_EVT_STR;
				break;
			case 0x10:
				cmd = ETAG_CONFIG_OVER_EVT_STR;
				break;
			case 0x14:
			    cmd=SET_ETAG_GOODS_INFO_EVT_STR;
			    break;
			case 0x15:
			    cmd=UPDATE_ETAG_GOODS_INFO_EVT_STR;
			    break;
			case 0xED:
				cmd = SENSOR_ADV_DATA_EVT_STR;
				break;
			default:
				cmd = UNKOWN_CMD_STR+":"+index;
				break;
			}
			return cmd;
		}

		public static String getCmdReturnValueFromIndex(int index) {
			String cmd = "";
			switch (index) {
			case 0:
				cmd = CMD_SUCCESS;
				break;
			case 1:
				cmd = CMD_FAIL;
				break;
			case 2:
				cmd = CMD_NO_CONNECT_SENSOR;
				break;
			case 3:
				cmd = CMD_DISCONNECT;
				break;
			case 4:
				cmd = CMD_CONNECT_TIME_OUT;
				break;
			case 5:
				cmd = CMD_ALREADY_CONNECT;
				break;
			case 6:
				cmd = CMD_ACK;
				break;
			case 7:
				cmd = CMD_FORMAT_ERROR;
				break;
			case 8:
				cmd = CMD_EXEC_TIME_OUT;
				break;
			case 9:
				cmd=CMD_NOT_EXEC_OVER;
				break;
			case 0x0A:
				cmd = CMD_RECEIVE_TIMEOUT;
				break;
			case 0x0B:
				cmd = CMD_FP_REGIST_FAIL;
				break;
			case 0x0C:
				cmd = CMD_FP_RIGIST_ERROR;
				break;
			default:
				cmd = "CMD_UNKOWN:"+index;
				break;
			}
			return cmd;
		}
		
}
