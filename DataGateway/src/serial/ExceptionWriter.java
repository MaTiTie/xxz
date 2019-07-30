package serial;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 璐熻矗灏嗕紶鍏ョ殑Exception涓殑閿欒淇℃伅鎻愬彇鍑烘潵骞惰浆鎹㈡垚瀛楃涓诧紱
 * 
 * @author yangle
 */
public class ExceptionWriter {

	/**
	 * 灏咵xception涓殑閿欒淇℃伅灏佽鍒板瓧绗︿覆涓苟杩斿洖璇ュ瓧绗︿覆
	 * 
	 * @param e
	 *            鍖呭惈閿欒鐨凟xception 
	 * @return 閿欒淇℃伅瀛楃涓�
	 */
	public static String getErrorInfoFromException(Exception e) {

		StringWriter sw = null;
		PrintWriter pw =null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return "\r\n" + sw.toString() + "\r\n";

		} catch (Exception e2) {
			return "鍑洪敊鍟︼紒鏈幏鍙栧埌閿欒淇℃伅锛岃妫�鏌ュ悗閲嶈瘯!";
		} finally {
			try {
				if (pw != null) {
					pw.close();
				}
				if (sw != null) {
					sw.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
