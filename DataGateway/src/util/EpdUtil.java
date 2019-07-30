package util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;


public class EpdUtil {

	public static byte[] getsensorEpdImage(String styleNumber,String price,int size,byte[]backgroundImg) throws IOException  {
		ByteArrayInputStream in = new ByteArrayInputStream(backgroundImg);    //将b作为输入流；
		BufferedImage bufImg = ImageIO.read(in);
		//bufimage.s
		int srcImgWidth = bufImg.getWidth();
		int srcImgHeight = bufImg.getHeight();
		//BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bufImg.getGraphics();
		//g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
		g.setFont(new Font("微软雅黑", Font.BOLD, 18));
		g.setColor(Color.black);
			g.drawString(styleNumber, 53, 30);
			g.drawString("￥" + price, 50, 60);
			if (size == 50) {
				g.drawRect(14, 115, 23, 23);
			} else if (size== 55) {
				g.drawRect(43, 115, 23, 23);
			} else if (size == 60) {
				g.drawRect(73, 115, 23, 23);
			} else if (size == 65) {
				g.drawRect(103, 115, 23, 23);
			} else if (size == 70) {
				g.drawRect(133, 115, 23, 23);
			} else if (size == 75) {
				g.drawRect(164, 115, 23, 23);
			}
			g.setColor(Color.white);
		    g.dispose();
		    System.out.println(bufImg.getWidth() + "/" + bufImg.getHeight());
		    return getMatrix(bufImg, srcImgWidth, srcImgHeight);
	}

	private static byte[] getMatrix(BufferedImage bufImg, int srcImgWidth, int srcImgHeight) {
		ArrayList<Integer> argbList = new ArrayList<Integer>();
		int minx = bufImg.getMinX();
		int miny = bufImg.getMinY();
		for (int y = miny; y < srcImgHeight; y++) {
			for (int x = minx; x < srcImgWidth; x++) {
				int argbi = bufImg.getRGB(x, y); // 下面三行代码将一个数字转换为RGB数字
				Color c = new Color(argbi);
				int r = c.getRed();
				int g = c.getGreen();
				int b = c.getBlue();
//				System.out.println(r + " " + g + " " + b);
				if (r == 255 & g == 255 & b == 255) {
					argbList.add(1);
				} else if (r == 0 & g == 0 & b == 0) {
					argbList.add(0);
				}
			}
		}
		ArrayList<Byte> byteList = new ArrayList<>();
		ArrayList<String> newArgbList = new ArrayList<String>();
		int i = 0;
		String bits = "";
		for (int x = 0; x < argbList.size(); x++) {
			bits = bits + argbList.get(x);
			if (i == 8) {
				byte[] bs = new byte[1];
				bs[0] = decodeBinaryString(bits);
				byteList.add(bs[0]);
				newArgbList.add("0X" + bytesToHexString(bs));
				bits = "";
				i = 0;
			}
			i++;
		}
//		System.out.println(argbList.size() + "/" + newArgbList.size() + "/" + byteList.size() + "\n"
//				+ newArgbList.toString() + "\n" + byteList.toString());
		byte[] epdData = new byte[5000];
		for (int x = 0; x < byteList.size(); x++) {
			epdData[x] = byteList.get(x);
		}
		return epdData;
	}

	/**
	 * 二进制字符串转byte
	 */
	private static byte decodeBinaryString(String byteStr) {
		int re, len;
		if (null == byteStr) {
			return 0;
		}
		len = byteStr.length();
		if (len != 4 && len != 8) {
			return 0;
		}
		if (len == 8) {// 8 bit处理
			if (byteStr.charAt(0) == '0') {// 正数
				re = Integer.parseInt(byteStr, 2);
			} else {// 负数
				re = Integer.parseInt(byteStr, 2) - 256;
			}
		} else {// 4 bit处理
			re = Integer.parseInt(byteStr, 2);
		}
		return (byte) re;
	}

	/**
	 * * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。 * * @param
	 * src byte[] data * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
}
