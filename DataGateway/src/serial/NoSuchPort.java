package  serial;

public class NoSuchPort extends Exception {

	private static final long serialVersionUID = 1L;

	public NoSuchPort() {
	}

	@Override
	public String toString() {
		return "no such port\r\n";
	}
}
