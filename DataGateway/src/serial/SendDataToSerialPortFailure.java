package  serial;

public class SendDataToSerialPortFailure extends Exception {

	private static final long serialVersionUID = 1L;

	public SendDataToSerialPortFailure() {
	}

	@Override
	public String toString() {
		return "send data to serial port failure\r\n";
	}
}
