package  serial;

public class ReadDataFromSerialPortFailure extends Exception {

	private static final long serialVersionUID = 1L;

	public ReadDataFromSerialPortFailure() {
	}

	@Override
	public String toString() {
		return "read data from serial port failure\r\n";
	}
}
