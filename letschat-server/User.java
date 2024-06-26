import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;

public class User{//某个用户
	String name;
	private Vector<Client> clients;//用户所在的所有线程
	private HashMap<Long,Chat> chatMap;  //用户所在的所有聊天
	User(String id){
		name=id;
		clients=new Vector<Client>();
		chatMap=new HashMap<Long,Chat>();
	}
	public void addClient(Client client) throws IOException{
		clients.add(client);
		File file=new File(Main.userFolder+"/"+name+"/avatar");
		client.network.send(ImageIO.read(file));
	}
	public synchronized void sendText(String id,String text){
		
	}
}
