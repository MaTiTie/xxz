package serial;

public class SerialPortInputStreamCloseFailure extends Exception {

	private static final long serialVersionUID = 1L;

	public SerialPortInputStreamCloseFailure() {
	}

	@Override
	public String toString() {
		return "serialPort inputStream close failure\r\n";
	}
}
