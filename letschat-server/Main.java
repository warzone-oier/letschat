import java.io.File;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
public class Main{
	static KeyPair keypair;
	static HashMap<String,User> users;
	static HashSet<String> online;
	static Thread now;
	static final String userFolder="./users";
	static void setUsers() throws Exception{
		online=new HashSet<String>();
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
	public static synchronized Thread setLock(Thread next){
		Thread bef=now;
		now=next;
		return bef;
	}
	
	static void flushOnline(){
		System.out.println("当前在线用户有：");
		for(String name:online)
			System.out.println(name);
		System.out.println("");
	}
	static void addUser(String name){
		new Thread(){
			public void run(){
				Thread bef=setLock(this);
				if(bef!=null) try{
					bef.join();
				}catch(InterruptedException e){}
				online.add(name);
				for(String s:users.keySet()) if(!s.equals(name))
					users.get(s).addUser(name);
				flushOnline();
			}
		}.start();
	}
	static void removeUser(String name){
		new Thread(){
			public void run(){
				Thread bef=setLock(this);
				if(bef!=null) try{
					bef.join();
				}catch(InterruptedException e){}
				online.remove(name);
				for(String s:users.keySet()) if(!s.equals(name))
					users.get(s).removeUser(name);
				flushOnline();
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
			listener.setPriority(1);
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