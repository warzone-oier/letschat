import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
/**单个界面 */
abstract class Panel{
	protected JPanel frame;
	JPanel line[];
	JLabel error;
	Panel(JFrame f,int size){
		frame=new JPanel();
		frame.setBounds(0, 0, 400, 300);
		frame.setLayout(new GridLayout(size,1));
		f.add(frame);
		line=new JPanel[size];
		for(int i=0;i<size;++i){
			line[i]=new JPanel(new FlowLayout());
			frame.add(line[i]);
		}
		error=new JLabel();
		error.setForeground(Color.red);
		setVisible(false);
	}
	protected void sendError(String s){
		error.setText(s);
		error.setVisible(true);
	}
	abstract public boolean setVisible(boolean aflag);
};
/**带有标签的输入框 */
class Text{
	private JLabel label;
	protected JTextField textfield;
	Text(JPanel panel,Font font,String title,JTextField field,int column){
		label=new JLabel(title);
		textfield=field;
		textfield.setColumns(column);
		label.setFont(font);
		textfield.setFont(font);
		panel.add(label);
		panel.add(textfield);
	}
	public String get(){
		return textfield.getText();
	}
	public void set(String s){
		textfield.setText(s);
	}
}
class HelloWindow extends Panel implements ActionListener{
	static Text ip,port;
	static JButton login,register;
	HelloWindow(JFrame f,Font font){
		super(f,6);
		MaskFormatter ipformatter,portformatter;
		try{
			ipformatter=new MaskFormatter("###.###.###.###");//设置格式
			portformatter=new MaskFormatter("#####");
			ipformatter.setPlaceholderCharacter('0'); //设置占位符
			portformatter.setPlaceholderCharacter('0');
			ip=new Text(line[1], font,"服务器IP地址",
				new JFormattedTextField(ipformatter),15);
			port=new Text(line[2], font, "端口号",
				new JFormattedTextField(portformatter),5);
			File file=new File("connect.last");
			if(file.exists()){
				try {
					FileInputStream fin=new FileInputStream(file);
					DataInputStream in=new DataInputStream(fin);
					ip.set(in.readUTF());
					port.set(in.readUTF());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}catch (ParseException e) {
			e.printStackTrace();
		}
		error.setFont(font);
		login=new JButton("登录");
		register=new JButton("注册");
		login.setFont(font);
		register.setFont(font);
		line[3].add(error);
		line[4].add(login);
		line[4].add(register);
		login.addActionListener(this);
		register.addActionListener(this);
	}
	static private String getip(){
		String s=ip.get();
		String out="";
		for(int i=0;i<4;++i){
			if(i!=0) out=out+".";
			int num=0;
			for(int j=0;j<3;++j)
				num=num*10+(s.charAt(i*4+j)-'0');
			if(num>=256) return "";
			out=out+num;
		}
		return out;
	}
	static private int getport(){
		String s=port.get();
		int out=0;
		for(int i=0;i<5;++i)
			out=out*10+(s.charAt(i)-'0');
		return out;
	}
	public boolean setVisible(boolean aflag){
		frame.setVisible(aflag);
		error.setVisible(!aflag);
		return false;
	}
	public void actionPerformed(ActionEvent e){
		Socket s;
		try{
			s=new Socket(getip(),getport());
			if(ClientMain.server.connect(s)){
				sendError("网络连接失败");
				return;
			}
		}catch(Exception exc){
			sendError("网络连接失败");
			return;
		}
		File file=new File("connect.last");
		if(file.exists()) file.delete();
		try{
			file.createNewFile();
		}catch(IOException e1){
			e1.printStackTrace();
		}
		try{
			FileOutputStream fout=new FileOutputStream(file);
			DataOutputStream out=new DataOutputStream(fout);
			out.writeUTF(ip.get());
			out.writeUTF(port.get());
		}catch(Exception e1){
			e1.printStackTrace();
		}
		setVisible(false);
		if((JButton)(e.getSource())==login){//登录事件
			if(StartWindow.login.setVisible(true)){
				setVisible(true);
				sendError("网络异常，请稍后再试");
				ClientMain.server.disconnect();
			}
		}else if(StartWindow.register.setVisible(true)){
			setVisible(true);
			sendError("网络异常，请稍后再试");
			ClientMain.server.disconnect();
		}
	}
}
/**验证码的专门输入框 */
class CaptchaText extends Text implements ActionListener{
	JLabel label;
	JButton change;
	CaptchaText(JPanel panel,Font font,String title,JTextField field,int column){
		super(panel,font,title,field,column);
		label=new JLabel();
		change=new JButton("看不清？换一张");
		change.setFont(font);
		change.addActionListener(this);
		panel.add(label);
		panel.add(change);
	}
	/**更换验证码图片 */
	public void actionPerformed(ActionEvent e){
		byte[] command={Network.changeCaptcha};
		try{
			ClientMain.server.send(command);
			label.setIcon(new ImageIcon(ClientMain.server.receiveImage()));
		}catch(IOException e1){
			StartWindow.setVisble(true);
		}
	}
	/** 发送验证码，检查是否正确*/
	public boolean check(){
		byte[] command={Network.checkCaptcha};
		try{
			ClientMain.server.send(command);
			ClientMain.server.send(get());
			command=ClientMain.server.receive();
			if(command[0]==Network.success) return true;
			label.setIcon(new ImageIcon(ClientMain.server.receiveImage()));
			return false;
		}catch(IOException e1){
			StartWindow.setVisble(true);
			return false;
		}
	}
}
abstract class AccountWindow extends Panel implements ActionListener{
	static Text name,password;
	static CaptchaText captcha;
	JButton confirm,cancel;
	AccountWindow(JFrame f,Font font,int length,String title){
		super(f, length);
		name=new Text(line[1], font, "用户名",new JTextField(),32);
		JPasswordField field=new JPasswordField();
		field.setEchoChar('*');
		password=new Text(line[2], font, "密码",field,32);
		captcha=new CaptchaText(line[length-4],font,"验证码",new JTextField(),6);
		confirm=new JButton(title);
		cancel=new JButton("取消");
		error.setFont(font);
		confirm.setFont(font);
		cancel.setFont(font);
		line[length-3].add(error);
		line[length-2].add(confirm);
		line[length-2].add(cancel);
		confirm.addActionListener(this);
		cancel.addActionListener(this);
	}
	/** 切换显示，若显示则拉取验证码，验证码拉取失败返回 true */
	public boolean setVisible(boolean aflag){
		if(aflag){
			try{
				captcha.label.setIcon(new ImageIcon(
					ClientMain.server.receiveImage()));
			}catch(IOException e){
				return true;
			}
		}
		frame.setVisible(aflag);
		error.setVisible(!aflag);
		return false;
	}
	abstract protected void Confirm();
	public void actionPerformed(ActionEvent e){
		if((JButton)(e.getSource())==cancel){//取消
			setVisible(false);
			StartWindow.hello.setVisible(true);
			ClientMain.server.disconnect();
			return;
		}
		if(name.get().equals(""))
			sendError("请输入用户名");
		else if(password.get().equals(""))
			sendError("请输入密码");
		else if(captcha.get().equals(""))
			sendError("请输入验证码");
		else Confirm();
	}
}
class LoginWindow extends AccountWindow{
	LoginWindow(JFrame f,Font font){
		super(f,font,7,"登录");
	}
	protected void Confirm(){
		if(!captcha.check()) sendError("验证码错误");
		else{
			try{
				byte[] out={Network.login};
				ClientMain.server.send(out);
				ClientMain.server.send(name.get());
				ClientMain.server.send(password.get());
				byte command=ClientMain.server.receive()[0];
				if(command==Network.success){
					sendError("登录成功");
				}else sendError("用户名或密码错误");
			}catch(IOException e){
				StartWindow.setVisble(true);
			}
		}
	}
}
class RegisterWindow extends AccountWindow{
	static Text repeat;
	RegisterWindow(JFrame f,Font font){
		super(f,font,8,"注册");
		JPasswordField field=new JPasswordField();
		field.setEchoChar('*');
		repeat=new Text(line[3],font,"确认密码",field,32);
	}
	protected void Confirm(){
		if(!password.get().equals(repeat.get()))
			sendError("两次输入密码不相同");
		else if(!captcha.check()) sendError("验证码错误");
		else{
			try{
				byte[] out={Network.login};
				ClientMain.server.send(out);
				ClientMain.server.send(name.get());
				ClientMain.server.send(password.get());
				byte command=ClientMain.server.receive()[0];
				if(command==Network.success){
					sendError("注册成功");
				}else sendError("用户名已存在");
			}catch(IOException e){
				StartWindow.setVisble(true);
			}
			
		}
	}
}
public class StartWindow{
	static JFrame frame;
	static Font font;
	static HelloWindow hello;
	static LoginWindow login;
	static RegisterWindow register;
	StartWindow(){
		frame=new JFrame("Hello LetsChat!");
		frame.setResizable(false);
		frame.setSize(400,300);
		frame.setLocation(200,200);
		frame.setLayout(null);
		font=new Font("黑体", Font.BOLD, 10);
		hello=new HelloWindow(frame, font);
		login=new LoginWindow(frame, font);
		register=new RegisterWindow(frame, font);
		frame.setVisible(true);
		hello.setVisible(true);
	}
	/** 如果显示，则转到开始画面，显示网络异常 */
	public static void setVisble(boolean aflag){
		frame.setVisible(aflag);
		if(aflag){
			StartWindow.login.setVisible(false);
			StartWindow.register.setVisible(false);
			StartWindow.hello.setVisible(true);
			StartWindow.hello.sendError("网络异常，请稍后再试");
		}
	}
}
