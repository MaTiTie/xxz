package  serial;

public class NotASerialPort extends Exception {

	private static final long serialVersionUID = 1L;

	public NotASerialPort() {
	}

	@Override
	public String toString() {
		return "not a serial port\r\n";
	}
}
