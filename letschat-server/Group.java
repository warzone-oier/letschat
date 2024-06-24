import java.awt.Image;
import java.util.Vector;

public class Group extends Chat{
	private String name;			//群名
	private Vector<User> member;	//所有成员
	@Override
	public synchronized Image getAvatar(User member){//获取的头像是群聊总头像
		throw new UnsupportedOperationException("Unimplemented method 'getAvatar'");
	}
	public synchronized void sendText(String name, String text){
		throw new UnsupportedOperationException("Unimplemented method 'sendText'");
	}
	public synchronized void init(User user){
		
	}
}
