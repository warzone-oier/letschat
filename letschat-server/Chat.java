import java.awt.Image;

public class Chat{
	public Image getAvatar(User member){return null;};
	//获取聊天的头像
	public void sendText(String name,String text){};
	//将消息转发到全体聊天用户
	public void init(User member){};
	//当某用户加入聊天/创建聊天/再次登录时，将聊天的消息拉取给该用户
}
