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
abstract class Panel{//单个面板
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
	abstract public void setVisible(boolean aflag);
};
class Text{//带有标签的输入框
	private JLabel label;
	private JTextField textfield;
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
			ipformatter=new MaskFormatter("###.###.###.###");
			portformatter=new MaskFormatter("#####");
			ipformatter.setPlaceholderCharacter('0'); // 设置占位符
			portformatter.setPlaceholderCharacter('0'); // 设置占位符
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
	public void setVisible(boolean aflag){
		frame.setVisible(aflag);
		error.setVisible(!aflag);
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
			StartWindow.login.setVisible(true);
		}else{//注册事件
			StartWindow.register.setVisible(true);
		}
	}
}

class CaptchaText extends Text{//验证码的专门输入框
	JLabel label;
	JButton change;
	CaptchaText(JPanel panel,Font font,String title,JTextField field,int column) {
		super(panel,font,title,field,column);

	}
	
}
class LoginWindow extends Panel implements ActionListener{
	static Text name,password;
	static CaptchaText captcha;
	JButton login,cancel;
	LoginWindow(JFrame f,Font font){
		super(f, 7);
		name=new Text(line[1], font, "用户名",new JTextField(),32);
		JPasswordField field=new JPasswordField();
		field.setEchoChar('*');
		password=new Text(line[2], font, "密码",field,32);
		captcha=new CaptchaText(line[3],font,"验证码",new JTextField(),32);
		login=new JButton("登录");
		cancel=new JButton("取消");
		error.setFont(font);
		login.setFont(font);
		cancel.setFont(font);
		line[4].add(error);
		line[5].add(login);
		line[5].add(cancel);
		login.addActionListener(this);
		cancel.addActionListener(this);
	}
	public void setVisible(boolean aflag){
		frame.setVisible(aflag);
		error.setVisible(!aflag);
	}
	public void actionPerformed(ActionEvent e){
		if((JButton)(e.getSource())==cancel){//取消
			setVisible(false);
			StartWindow.hello.setVisible(true);
			ClientMain.server.endconnect();
			return;
		}
		String id=name.get();
		String pass=password.get();
		String capt=captcha.get();
		if(id.equals(""))
			sendError("请输入用户名");
		else if(pass.equals(""))
			sendError("请输入密码");
		else if(capt.equals(""))
			sendError("请输入验证码");
		else{
		}
	}
}
class RegisterWindow extends Panel implements ActionListener{
	static Text name,password,repeat;
	static CaptchaText captcha;
	JButton register,cancel;
	RegisterWindow(JFrame f,Font font){
		super(f, 8);
		name=new Text(line[1], font, "用户名",new JTextField(),32);
		JPasswordField field1=new JPasswordField(),field2=new JPasswordField();
		field1.setEchoChar('*');
		field2.setEchoChar('*');
		password=new Text(line[2], font, "密码",field1,32);
		repeat=new Text(line[3], font, "确认密码",field2,32);
		captcha=new CaptchaText(line[4],font,"验证码",new JTextField(),10);
		register=new JButton("注册");
		cancel=new JButton("取消");
		error.setFont(font);
		register.setFont(font);
		cancel.setFont(font);
		line[5].add(error);
		line[6].add(register);
		line[6].add(cancel);
		register.addActionListener(this);
		cancel.addActionListener(this);
	}
	public void setVisible(boolean aflag){
		frame.setVisible(aflag);
		error.setVisible(!aflag);
	}
	public void actionPerformed(ActionEvent e){
		if((JButton)(e.getSource())==cancel){//取消
			setVisible(false);
			StartWindow.hello.setVisible(true);
			ClientMain.server.endconnect();
			return;
		}
		String id=name.get();
		String pass1=password.get();
		String pass2=repeat.get();
		String capt=captcha.get();
		if(id.equals(""))
			sendError("请输入用户名");
		else if(pass1.equals(""))
			sendError("请输入密码");
		else if(!pass1.equals(pass2))
			sendError("两次密码输入不相同");
		else if(capt.equals(""))
			sendError("请输入验证码");
		else{
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
}
