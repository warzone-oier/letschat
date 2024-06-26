import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
public class User{//某个用户
	String name;

	private boolean lock;//用户锁（整个类的所有函数的读写同步）
	private LinkedList<Client> clients;//用户所在的所有线程
	int onlineclient;
	User(String id){
		name=id;
		clients=new LinkedList<Client>();
		onlineclient=0;
		lock=false;
	}
	/** 将客户端加入该用户， */
	public void addClient(Client client){
		new Thread(){
			public void run(){
				while(lock);
				lock=true;
				clients.add(client);
				if(onlineclient++==0) Main.addUser(name);
				lock=false;
				while(Main.userslock);
				Main.userslock=true;
				for(String name:Main.users.keySet()){
					byte[] command={Network.onlineUser};
					if(Main.users.get(name).onlineclient>0) try{
						client.network.send(command);
						client.network.send(name);
					}catch(IOException e){
						Main.userslock=false;
						return;
					}
				}
				Main.userslock=false;
			}
		}.start();
	}
	/** 删除离线的客户端 */
	public void deleteClient(Client client){
		new Thread(){
			public void run(){
				while(lock);
				lock=true;
				for(Iterator<Client> ite=clients.iterator();ite.hasNext();){
					Client get=ite.next();
					if(get==client) ite.remove();
				}
				if(--onlineclient==0) Main.removeUser(name);
				lock=false;
			}
		}.start();
	}
	/** 加入新在线用户 */
	public void addUser(String name){
		new Thread(){
			public void run(){
				while(lock);
				lock=true;
				for(Client client:clients){
					byte[] command={Network.onlineUser};
					try{
						client.network.send(command);
						client.network.send(name);
					}catch(Exception e){
						lock=false;
						return;
					}
				}
				lock=false;
			}
		}.start();
	}
	/** 删除新在线用户 */
	public void removeUser(String name){
		new Thread(){
			public void run(){
				while(lock);
				lock=true;
				for(Client client:clients){
					byte[] command={Network.offlineUser};
					try{
						client.network.send(command);
						client.network.send(name);
					}catch(Exception e){
						lock=false;
						return;
					}
				}
				lock=false;
			}
		}.start();
	}
}
