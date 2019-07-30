package  serial;

public class SerialPortParameterFailure extends Exception {

	private static final long serialVersionUID = 1L;

	public SerialPortParameterFailure() {
	}
	@Override
	public String toString() {
		return "serialPort parameter faiure\r\n";
	}
}
