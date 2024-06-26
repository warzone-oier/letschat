import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.TreeMap;
import java.util.Scanner;
public class Main{
	static KeyPair keypair;
	static TreeMap<String,User> users;
	static boolean userslock;
	static final String userFolder="./users";
	static void setUsers() throws Exception{
		userslock=true;
		users=new TreeMap<String,User>();
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
		userslock=false;
	}
	static void addUser(String name){
		new Thread(){
			public void run(){
				while(userslock);
				userslock=true;
				for(String s:users.keySet())
					users.get(s).addUser(name);
				userslock=false;
			}
		}.start();
	}
	static void removeUser(String name){
		new Thread(){
			public void run(){
				while(userslock);
				userslock=true;
				for(String s:users.keySet())
					users.get(s).removeUser(name);
				userslock=false;
			}
		}.start();
	}
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
	public static void main(String args[]){
		try{
			setUsers();
			System.out.println("用户数据拉取成功");
		}catch(Exception e){
			System.out.println("用户数据拉取失败");
			return;
		}
		try{
			setPorts();
			System.out.println("启动端口监听成功");
		}catch(Exception e){
			System.out.println("启动端口监听失败");
			return;
		}
	}
}