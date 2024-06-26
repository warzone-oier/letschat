import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.jar.Attributes.Name;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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
		label.setFont(ClientMain.font);
		label.setForeground(Color.red);
		frame.add(label);
	}
}
class Profile implements ActionListener{
	JLabel avatar,name;
	JButton settings;
	JPanel panel;
	Profile(){
		panel=new JPanel();
		panel.setPreferredSize(new Dimension(300, 100));
		panel.setLayout(new FlowLayout());
		avatar=new JLabel();
		name=new JLabel();
		name.setFont(ClientMain.font);
		settings=new JButton("更改头像");
		panel.add(avatar);
		panel.add(name);
		panel.add(settings);
		settings.addActionListener(this);
	}
	/** 设置头像 */
	public void setAvatar(BufferedImage image){
		Image scaledimage=image.getScaledInstance(50,50,Image.SCALE_SMOOTH);
		avatar.setIcon(new ImageIcon(scaledimage));
	}
	public void actionPerformed(ActionEvent e){
		FileDialog fileDialog=new FileDialog(MainWindow.frame, "选择图片", FileDialog.LOAD);
		fileDialog.setVisible(true);
		String filename=fileDialog.getFile();
		if(filename!=null){
			String directory=fileDialog.getDirectory();
			File file=new File(directory+filename);
			BufferedImage image;
			try{
				image=ImageIO.read(file);
			}catch(IOException e1){
				ErrorWindow error=new ErrorWindow("错误：不支持的文件格式");
				return;
			}
			try{
				byte[] command={Network.changeAvatar};
				ClientMain.server.send(command);
				ClientMain.server.send(image);
				//发送改头像信息，由服务器的反馈改服务端头像
			}catch(IOException e1){
				ClientMain.mainWindow.setVisble(false, "");
			}
		}
	}
}
class Bottom{
	JScrollPane scrollPane;
	JPanel panel;
	Bottom(){
		scrollPane=new JScrollPane();
		panel=new JPanel();
		scrollPane.setPreferredSize(new Dimension(300,700));
		scrollPane.add(panel);
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
		frame.setLayout(null);
		frame.setVisible(false);
		
		profile=new Profile();
		bottom=new Bottom();

		splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			profile.panel,bottom.scrollPane);
		splitPane.setDividerLocation(100);
		splitPane.setEnabled(false);
		frame.add(splitPane);
	}
	/** 切换显示，若显示则拉取头像，若不显示转回登录画面 */
	public void setVisble(boolean aflag,String name){
		frame.setVisible(aflag);
		if(aflag){
			try{
				BufferedImage image=ClientMain.server.receiveImage();
				profile.setAvatar(image);
				profile.name.setText(name);
			}catch(IOException e){
				frame.setVisible(false);
				StartWindow.setVisble(true);
				return;
			}
			start();
		}else StartWindow.setVisble(true);
	}
	/** 网络读取线程 */
	public void run(){
		try{
			while(true){
				byte[] command=ClientMain.server.receive();
				if(command[0]==Network.changeAvatar)
					profile.setAvatar(ClientMain.server.receiveImage());
			}
		}catch(IOException e){
			setVisble(false,"");
		}
	}
}