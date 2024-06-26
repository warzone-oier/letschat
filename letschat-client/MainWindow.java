import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
class ErrorWindow{
	JFrame frame;
	JLabel label;
	ErrorWindow(String text){
		frame=new JFrame("错误");
		frame.setResizable(false);
		frame.setSize(200,100);
		frame.setLocation(300,300);
		frame.setLayout(new GridLayout(1, 1));
		frame.setVisible(true);
		label=new JLabel(text);
		label.setFont(new Font("黑体", Font.BOLD, 20));

		label.setForeground(Color.red);
		frame.add(label);
	}
}
class Profile{
	JLabel name;
	JPanel panel;
	Profile(){
		panel=new JPanel();
		panel.setPreferredSize(new Dimension(300, 75));
		panel.setLayout(new FlowLayout());
		name=new JLabel();
		name.setFont(new Font("黑体", Font.BOLD, 20));
		panel.add(name);
	}
}
class Friend{
	JButton button;
	Friend(String name){
		button=new JButton(name);
		button.setFont(new Font("黑体", Font.BOLD, 20));
	}
}
class Bottom{
	JScrollPane scrollPane;
	JPanel panel;
	HashMap<String,Friend> friends;
	Bottom(){
		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		scrollPane=new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(300,725));
	}
	public void addUser(String name){
		Friend friend=new Friend(name);
		friends.put(name,friend);
		panel.add(friend.button);
	}
	public void deleteUser(String name){
		panel.remove(friends.get(name).button);
		friends.remove(name);
	}
}
public class MainWindow extends Thread{
	static JFrame frame;
	static Profile profile;
	static Bottom bottom;
	static JSplitPane splitPane;
	MainWindow(){
		frame=new JFrame("");
		frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300,800);
		frame.setLocation(100,100);
		frame.setVisible(false);
		
		profile=new Profile();
		bottom=new Bottom();

		splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			profile.panel,bottom.scrollPane);
		splitPane.setDividerLocation(75);
		splitPane.setEnabled(false);
		frame.add(splitPane);
	}
	/** 切换显示，若显示则拉取头像，若不显示转回登录画面 */
	public void setVisble(boolean aflag,String name){
		frame.setVisible(aflag);
		splitPane.setVisible(aflag);
		if(aflag){
			profile.name.setText(name);
			start();
		}else StartWindow.setVisble(true);
	}
	/** 网络读取线程 */
	public void run(){
		try{
			while(true){
				byte[] command=ClientMain.server.receive();
				if(command[0]==Network.onlineUser)
					bottom.addUser(ClientMain.server.receiveString());
				else if(command[0]==Network.offlineUser)
					bottom.deleteUser(ClientMain.server.receiveString());
			}
		}catch(IOException e){
			setVisble(false,"");
		}
	}
}