package util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream; 
import java.util.Iterator;
import java.util.Properties; 

public class Config {
	public static String getAppInfo(String info,String path) {	
		if(info==null||path==null)
		{
			return null;
		}
		Properties prop = new Properties(); 
		String infoData=null;
        try{
            //读取属性文件app.properties
            InputStream in = new BufferedInputStream (new FileInputStream(path+"//"+"app.properties"));
            prop.load(in);     ///加载属性列表
            Iterator<String> it=prop.stringPropertyNames().iterator();
            while(it.hasNext()){
                String key=it.next();
                if(key.equals(info))
                {
                	infoData=prop.getProperty(key);
                	break;
                }                
            }
            in.close();          
          
        }      
         catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
        	 return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        return infoData;
	}
	public static void saveInfo(String key,String value,String path) {	
		  ///保存属性到b.properties文件
		if(key==null||value==null||path==null) 
		{
			return ;
		}
		Properties prop = new Properties(); 
        FileOutputStream oFile=null;
        InputStream in;
		try {
			in = new BufferedInputStream (new FileInputStream(path+"//"+"app.properties"));
			try {
				prop.load(in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     ///加载属性列表
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		try {
			oFile = new FileOutputStream(path+"//"+"app.properties", false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//true表示追加打开
		prop.setProperty(key, value);   
        try {
        	 prop.store(oFile, "The New properties file");
			oFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public static void main(String[] args) { 
        String version=Config.getAppInfo("appVersion", ".");
        System.out.println(version);
        Config.saveInfo("app", "2.0", ".");
        Config.saveInfo("appVersion", "2.0", ".");
    } 
}