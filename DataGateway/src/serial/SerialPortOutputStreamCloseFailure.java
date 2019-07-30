package  serial;

public class SerialPortOutputStreamCloseFailure extends Exception {

	private static final long serialVersionUID = 1L;

	public SerialPortOutputStreamCloseFailure() {
	}

	@Override
	public String toString() {
		return "serialPort outputStream close failure\r\n";
	}
}
