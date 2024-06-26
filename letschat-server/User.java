import java.util.HashMap;
import java.util.Vector;

public class User{//某个用户
	String name;
	Vector<Client> clients;//用户所在的所有线程
	HashMap<Long,Chat> chatMap;  //用户所在的所有聊天
	User(String id){
		name=id;
		clients=new Vector<Client>();
		chatMap=new HashMap<Long,Chat>();
	}
	public synchronized void sendText(String id,String text){
		
	}
}
