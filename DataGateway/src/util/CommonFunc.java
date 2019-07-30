package util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Base64.Decoder;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class CommonFunc {
	public static String convertBytesToHexString(byte[] data) {
	StringBuilder sBuilder=new StringBuilder();
		for(int i=0;i<data.length;i++)
		{
			sBuilder.append(String.format("%02x", data[i]));
		}
		return sBuilder.toString();
	}
	public static String convertBytesToHexString(int[] data) {
		StringBuilder sBuilder=new StringBuilder();
			for(int i=0;i<data.length;i++)
			{
				sBuilder.append(String.format("%02x", data[i]));
			}
			return sBuilder.toString();
		}
	
	public static boolean saveDataToFile(byte[] data,String path)
	{
		FileOutputStream out;
		try {
			out = new FileOutputStream(path);
			out.write(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	public static boolean saveDataToFile(String data,String path)
	{	
		   File file =new File(path);
		   if(!file.exists())
		   {
			   try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   }
		     FileWriter fwriter = null;
		     try {
		     fwriter = new FileWriter(file.getAbsolutePath(),true);
		     BufferedWriter bufferedWriter=new BufferedWriter(fwriter);
		     bufferedWriter.write(data);
		     bufferedWriter.close();
		    // fwriter.flush();
		     fwriter.close();
		     }
		     catch (IOException ex) 
		     {
		    	 ex.printStackTrace();
		     }
		  return true;
	}
	public static byte[] convertBase64ToBytes(String pic)
	{
		byte[] picData;
		Decoder decoder=Base64.getDecoder();
		picData=decoder.decode(pic);
		return picData;
	}
	 public static String getWindowsMACAddress() {  
         //获取本地IP对象  
         InetAddress ia = null;  
         try {  
             ia = InetAddress.getLocalHost();  
         } catch (UnknownHostException e) {  
             e.printStackTrace();  
             return null;
         }  
         //获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。  
         byte[] mac = null;  
         try {  
             mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();  
         } catch (SocketException e) { 
             e.printStackTrace(); 
             return null;
         }  
         //下面代码是把mac地址拼装成String  
         StringBuffer TT = new StringBuffer();   
         for(int i=0;i<mac.length;i++){  
             //mac[i] & 0xFF 是为了把byte转化为正整数  
             String s = Integer.toHexString(mac[i] & 0xFF);  
             TT.append(s.length()==1?0+s:s);  
         }  
         //把字符串所有小写字母改为大写成为正规的mac地址并返回  
         return TT.toString().toUpperCase();  
     }  
	 
	public static String getLinuxMACAddress() {     
		String mac = "";
		 try
		 {
			 Process p = new ProcessBuilder("ifconfig").start();
			 BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 String line;
			 while ((line = br.readLine()) != null) 
			 {
				 Pattern pat = Pattern.compile("\\b\\w+:\\w+:\\w+:\\w+:\\w+:\\w+\\b");
				 Matcher mat= pat.matcher(line);
				 if(mat.find())
				 {
					 mac=mat.group(0);
					 mac=mac.replaceAll(":", "");
					 System.out.print(mac);
				 }
			 }
			 br.close();
	    }
		 catch (IOException e) {return null;}
		return mac;	
	}
	public static String getIpAddr() {
		String addr="";
		Enumeration<NetworkInterface> e;
		try {
			e = NetworkInterface.getNetworkInterfaces();
			while(e.hasMoreElements())
	        {
	            addr+=e.nextElement().toString();
	        }
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
       
         return addr;
    }
	public static String getLocalIpAddr() {
		 
	    String clientIP = null;
	    Enumeration<NetworkInterface> networks = null;
	    try {
	        //获取所有网卡设备
	        networks = NetworkInterface.getNetworkInterfaces();
	        if (networks == null) {
	            //没有网卡设备 打印日志  返回null结束
	            return null;
	        }
	    } catch (SocketException e) {
	        System.out.println(e.getMessage());
	    }
	    InetAddress ip;
	    Enumeration<InetAddress> addrs;
	    // 遍历网卡设备
	    while (networks.hasMoreElements()) {
	        NetworkInterface ni = networks.nextElement();
	        try {
	            //过滤掉 loopback设备、虚拟网卡
	            if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
	                continue;
	            }
	        } catch (SocketException e) {
	        }
	        addrs = ni.getInetAddresses();
	        if (addrs == null) {
	            continue;
	        }
	        // 遍历InetAddress信息
	        while (addrs.hasMoreElements()) {
	            ip = addrs.nextElement();
	            if (!ip.isLoopbackAddress() && ip.isSiteLocalAddress() && ip.getHostAddress().indexOf(":") == -1) {
	                try {
	                    clientIP = ip.toString().split("/")[1];
	                } catch (ArrayIndexOutOfBoundsException e) {
	                    clientIP = null;
	                }
	            }
	        }
	    }
	    return clientIP;
	}
	public static String getMACAddress(){
		
		String os=System.getProperty("os.name").toLowerCase();
		String mac=null;
		if(os.startsWith("windows"))
		{     
            mac = getWindowsMACAddress();   
            System.out.println("system is windows,mac:"+mac);     
        }
		else if(os.startsWith("linux"))
        {     
            mac = getLinuxMACAddress();     
            System.out.println("system is linux,mac:"+mac);   
        }
		if(mac==null)
		return mac;
		else
		return mac.toLowerCase();
	}
	
	public static String encodeByMD5(String originString){ 
        if (originString!=null) { 
            try { 
                //创建具有指定算法名称的信息摘要 
                MessageDigest md5 = MessageDigest.getInstance("MD5"); 
                //使用指定的字节数组对摘要进行最后更新，然后完成摘要计算 
                byte[] results = md5.digest(originString.getBytes()); 
                //将得到的字节数组变成字符串返回  
                String result = convertBytesToHexString(results); 
                return result; 
            } catch (Exception e) { 
                e.printStackTrace(); 
                return null;
            } 
        } 
        return null; 
    } 
	
	public static boolean createQrCode(String  filePath, String content, int qrCodeSize, String imageFormat) throws WriterException, IOException{  
        //设置二维码纠错级别ＭＡＰ
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();  
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);  // 矫错级别  
        QRCodeWriter qrCodeWriter = new QRCodeWriter();  
        //创建比特矩阵(位矩阵)的QR码编码的字符串  
        BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);  
        // 使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
        int matrixWidth = byteMatrix.getWidth();  
        BufferedImage image = new BufferedImage(matrixWidth-200, matrixWidth-200, BufferedImage.TYPE_INT_RGB);  
        image.createGraphics();  
        Graphics2D graphics = (Graphics2D) image.getGraphics();  
        graphics.setColor(Color.WHITE);  
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);  
        // 使用比特矩阵画并保存图像
        graphics.setColor(Color.BLACK);  
        for (int i = 0; i < matrixWidth; i++){
            for (int j = 0; j < matrixWidth; j++){
                if (byteMatrix.get(i, j)){
                    graphics.fillRect(i-100, j-100, 1, 1);  
                }
            }
        }
        OutputStream outputStream=new FileOutputStream(new File(filePath));
        boolean reslut=ImageIO.write(image, imageFormat, outputStream); 
        outputStream.close();
        return reslut;
}  
}
