import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
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
class ScrollPanel{
	JScrollPane scrollPane;
	JPanel panel;
	ScrollPanel(int width,int height){
		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		scrollPane=new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(width,height));
	}
	/** 刷新窗口 */
	void sendText(String name,String text){
		JLabel dialog=new JLabel(name+":"+text);
		dialog.setFont(new Font("黑体",Font.PLAIN,15));
		panel.add(dialog);
		panel.setVisible(false);
		panel.setVisible(true);
	}
}
class TextArea{
	String name;
	JPanel panel;
	JTextArea area;
	JButton send;
	TextArea(String s){
		name=s;
		panel=new JPanel();
		panel.setPreferredSize(new Dimension(800,200));
		area=new JTextArea(5, 50);
		area.setFont(new Font("黑体", Font.PLAIN, 15));
		send=new JButton("发送");
		send.setFont(new Font("黑体", Font.PLAIN, 10));
		panel.add(area);
		panel.add(send);
	}
}
class Friend implements ActionListener{
	JButton visit;//显示在主窗口的切换按钮
	JFrame frame;
	ScrollPanel dialog;
	TextArea textarea;
	JSplitPane splitPane;
	String name;
	Friend(String s){
		name=s;
		visit=new JButton(s);
		visit.setFont(new Font("黑体", Font.BOLD, 20));
		visit.setPreferredSize(new Dimension(300, 25));
		visit.addActionListener(this);

		frame=new JFrame(s);
		frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(800,500);
		frame.setLocation(400,200);
		frame.setVisible(false);

		dialog=new ScrollPanel(800, 300);
		textarea=new TextArea(s);
		textarea.send.addActionListener(this);

		splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			dialog.scrollPane,textarea.panel);
		splitPane.setDividerLocation(300);
		splitPane.setEnabled(false);
		frame.add(splitPane);
	}
	public void actionPerformed(ActionEvent e){
		if((JButton)(e.getSource())==visit)
			frame.setVisible(true);
		else try{//发送按钮
			String text=textarea.area.getText();
			textarea.area.setText("");
			dialog.sendText(MainWindow.name,text);//自己说的
			frame.setVisible(false);
			frame.setVisible(true);
			byte[] command={Network.sendmassage};
			ClientMain.server.send(command);
			ClientMain.server.send(name);//发给别人的
			ClientMain.server.send(text);
		}catch(IOException e1){
			ClientMain.mainWindow.setVisble(false,"");
		}
	}
}
class OnlineUser extends ScrollPanel{
	HashMap<String,Friend> friends;
	OnlineUser(){
		super(300,725);
		friends=new HashMap<String,Friend>();
	}
	public void addUser(String name){
		if(friends.get(name)==null){
			Friend friend=new Friend(name);
			friends.put(name,friend);
			panel.add(friend.visit);
			MainWindow.frame.setVisible(false);
			MainWindow.frame.setVisible(true);
		}
	}
	public void deleteUser(String name){
		Friend friend=friends.get(name);
		if(friend!=null){
			panel.remove(friends.get(name).visit);
			panel.repaint();
			friend.frame.setVisible(false);
			friends.remove(name);
			MainWindow.frame.setVisible(false);
			MainWindow.frame.setVisible(true);
		}
	}
	/** 别人发给自己消息*/
	public void sendText(String name,String text){
		Friend friend=friends.get(name);
		if(friend!=null)
			friend.dialog.sendText(name,text);
	}
	public void setUnvisible(){
		for(String name:friends.keySet())
			deleteUser(name);
	}
}
public class MainWindow extends Thread{
	static JFrame frame;
	static Profile profile;
	static OnlineUser onlineUser;
	static JSplitPane splitPane;
	static String name;
	MainWindow(){
		frame=new JFrame("");
		frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300,800);
		frame.setLocation(100,100);
		frame.setVisible(false);
		
		profile=new Profile();
		onlineUser=new OnlineUser();

		splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			profile.panel,onlineUser.scrollPane);
		splitPane.setDividerLocation(75);
		splitPane.setEnabled(false);
		frame.add(splitPane);
	}
	/** 切换显示，若不显示转回登录画面 */
	public void setVisble(boolean aflag,String s){
		frame.setVisible(aflag);
		splitPane.setVisible(aflag);
		if(aflag){
			name=s;
			profile.name.setText(s);
			start();
		}else{
			onlineUser.setUnvisible();
			StartWindow.setVisble(true);
		}
	}
	/** 网络读取线程 */
	public void run(){
		try{
			while(true){
				byte[] command=ClientMain.server.receive();
				setPriority(MAX_PRIORITY);
				String sender=ClientMain.server.receiveString();
				if(command[0]==Network.onlineUser)
					SwingUtilities.invokeLater(()->onlineUser.addUser(sender));
				else if(command[0]==Network.offlineUser)
					SwingUtilities.invokeLater(()->onlineUser.deleteUser(sender));
				else{
					String text=ClientMain.server.receiveString();
					SwingUtilities.invokeLater(()->onlineUser.sendText(sender,text));
				}
				setPriority(5);
			}
		}catch(IOException e){
			setVisble(false,"");
		}
	}
}