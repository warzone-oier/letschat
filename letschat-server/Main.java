import java.io.File;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Scanner;
public class Main{
	static KeyPair keypair;
	static HashMap<String,User> users;
	static HashMap<Long,Group> groups;
	static void setPorts() throws Exception{
		KeyPairGenerator keyPairGenerator;
		keyPairGenerator=KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		keypair=keyPairGenerator.generateKeyPair();

		FileInputStream fin=new FileInputStream("ports.data");
		Scanner scan=new Scanner(fin);
		while(scan.hasNext()){
			int port=scan.nextInt();
			if(port<0||port>=65536) throw new Exception();
			PortListener listener=new PortListener(port);
			listener.start();
		}
		scan.close();
	}
	static final String userFolder="./users";
	static void setUsers() throws Exception{
		users=new HashMap<String,User>();
		File file=new File(userFolder);
		if(!file.isDirectory()){
			if(file.isFile()) file.delete();
			file.mkdir();
		}
		String[] userlist=file.list();
		for(String name:userlist){
			User user=new User(name);
			users.put(name, user);
		}
	}
	public static void main(String args[]){
		try{
			setPorts();
			System.out.println("启动端口监听成功");
		}catch(Exception e){
			System.out.println("启动端口监听失败");
			return;
		}
		try{
			setUsers();
			System.out.println("用户数据拉取成功");
		}catch(Exception e){
			System.out.println("用户数据拉取失败");
			return;
		}
	}
}