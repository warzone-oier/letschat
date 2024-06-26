import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
public class User{//某个用户
	String name;
	private LinkedList<Client> clients;//用户所在的所有线程
	private Thread now;
	int onlineclient;
	User(String id){
		name=id;
		clients=new LinkedList<Client>();
		onlineclient=0;
	}
	public synchronized Thread setLock(Thread next){
		Thread bef=now;
		now=next;
		return bef;
	}
	/** 将客户端加入该用户， */
	public void addClient(Client client){
		new Thread(){
			public void run(){
				Thread bef=setLock(this);
				if(bef!=null) try{
					bef.join();
				}catch(InterruptedException e){}
				clients.add(client);
				if(onlineclient++==0) Main.addUser(name);
				new Thread(){
					public void run(){
						Thread bef=Main.setLock(this);
						if(bef!=null) try{
							bef.join();
						}catch(InterruptedException e){}
						for(String s:Main.users.keySet()) if(!s.equals(name)){
							byte[] command={Network.onlineUser};
							if(Main.users.get(s).onlineclient>0) try{
								client.network.send(command);
								client.network.send(s);
							}catch(IOException e){
								return;
							}
						}
					}
				}.start();
			}
		}.start();
	}
	/** 删除离线的客户端 */
	public void deleteClient(Client client){
		new Thread(){
			public void run(){
				Thread bef=setLock(this);
				if(bef!=null) try{
					bef.join();
				}catch(InterruptedException e){}
				for(Iterator<Client> ite=clients.iterator();ite.hasNext();){
					Client get=ite.next();
					if(get==client) ite.remove();
				}
				if(--onlineclient==0) Main.removeUser(name);
			}
		}.start();
	}
	/** 加入新在线用户 */
	public void addUser(String s){
		new Thread(){
			public void run(){
				Thread bef=setLock(this);
				if(bef!=null) try{
					bef.join();
				}catch(InterruptedException e){}
				
				System.out.println(s+" addUser*"+name);
				for(Client client:clients){
					byte[] command={Network.onlineUser};
					try{
						System.out.println("***");client.network.send(command);
						System.out.println("***");client.network.send(s);
					}catch(Exception e){}
				}
				System.out.println(s+" addUser "+name);
			}
		}.start();
	}
	/** 删除新在线用户 */
	public void removeUser(String name){
		new Thread(){
			public void run(){
				Thread bef=setLock(this);
				if(bef!=null) try{
					bef.join();
				}catch(InterruptedException e){}
				for(Client client:clients){
					byte[] command={Network.offlineUser};
					try{
						client.network.send(command);
						client.network.send(name);
					}catch(Exception e){}
				}
			}
		}.start();
	}
	public void send(String receiver,String text){
		Main.users.get(receiver).receive(name, text);
	}
	public void receive(String sender,String text){
		System.out.println("send from "+sender+" to "+name);
		new Thread(){
			public void run(){
				Thread bef=setLock(this);
				if(bef!=null) try{
					bef.join();
				}catch(InterruptedException e){}
				for(Client client:clients){
					byte[] command={Network.sendmassage};
					try{
						client.network.send(command);
						client.network.send(sender);						
						client.network.send(text);
					}catch(Exception e){}
				}
				System.out.println("*send from"+sender+" to "+name);
			}
		}.start();
	}
}
