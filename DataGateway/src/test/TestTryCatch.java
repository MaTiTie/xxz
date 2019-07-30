package test;

public class TestTryCatch {

	public static void main(String[] args) {
			try {
				testfuc();
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("hello world");
				e.printStackTrace();
			}
		
	}
	
	public static void testfuc() {
		 byte[] test=new byte[100];
		 System.out.println(test[99]);
		 testFunc2();
	}
	public static void testFunc2() {
		 byte[] test=new byte[100];
		 System.out.println(test[101]);
	}
	
}
