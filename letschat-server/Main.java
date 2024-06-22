import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Scanner;
import java.util.Vector;
public class Main{
	static KeyPair keypair;
	static Vector<PortListener> listeners;
	static void setPorts() throws Exception{
		KeyPairGenerator keyPairGenerator;
		keyPairGenerator=KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		keypair=keyPairGenerator.generateKeyPair();

		FileInputStream fin=new FileInputStream("ports.data");
		Scanner scan=new Scanner(fin);
		listeners=new Vector<PortListener>();
		while(scan.hasNext()){
			int port=scan.nextInt();
			if(port<0||port>=65536) throw new Exception();
			PortListener listener=new PortListener(port, keypair);
			listeners.add(listener);
			listener.start();
		}
		scan.close();
	}
	public static void main(String args[]){
		try{
			setPorts();
		}catch(Exception e){
			System.out.println("启动端口监听失败");
			return;
		}
	}
}