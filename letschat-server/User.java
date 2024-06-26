import java.util.Hashtable;
import java.util.Vector;

public class User{//某个用户
	String name;
	Vector<Client> clients;//用户所在的所有线程
	Hashtable<Long,Chat> chatMap;  //用户所在的所有聊天
	User(String id){name=id;}
	public synchronized void sendText(String id,String text){
		
	}
}
