import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.MaskFormatter;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
class HelloWindow implements ActionListener{
	JPanel frame,panel[];
	MaskFormatter ipformatter,portformatter;
	JLabel ipLabel,portLabel;
	JFormattedTextField ipField, portField;
	JButton login,register;
	HelloWindow(JFrame f,Font font){
		frame=new JPanel();
		frame.setBounds(0, 0, 400, 300);
		frame.setLayout(new GridLayout(5,1));
		f.add(frame);
		panel=new JPanel[4];
		for(int i=0;i<4;++i){
			panel[i]=new JPanel(new FlowLayout());
			frame.add(panel[i]);
		}
		try{
			ipformatter=new MaskFormatter("###.###.###.###");
			portformatter=new MaskFormatter("#####");
			ipformatter.setPlaceholderCharacter('0'); // 设置占位符
		}catch (ParseException e) {
			e.printStackTrace();
		}
		ipField = new JFormattedTextField(ipformatter);
		portField= new JFormattedTextField(portformatter);
		ipField.setColumns(9);
		portField.setColumns(4);
		ipLabel=new JLabel("服务器IP地址");
		portLabel=new JLabel("端口号");
		ipLabel.setFont(font);
		portLabel.setFont(font);
		panel[1].add(ipLabel);
		panel[1].add(ipField);
		panel[2].add(portLabel);
		panel[2].add(portField);
		login=new JButton("登录");
		register=new JButton("注册");
		login.setFont(font);
		register.setFont(font);
		panel[3].add(login);
		panel[3].add(register);
		login.addActionListener(this);
		register.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e){
		if((JButton)(e.getSource())==login){//登录事件
		
		}else{

		}
	}
	void setVisible(boolean aFlag){
		frame.setVisible(aFlag);
	}
}
public class StartWindow{
	JFrame frame;
	Font font;
	HelloWindow hello;
	StartWindow(){
		frame=new JFrame("Hello LetsChat!");
		frame.setResizable(false);
		frame.setSize(400,300);
		frame.setLocation(200,200);
		frame.setLayout(null);
		font=new Font("黑体", Font.BOLD, 10);
		hello=new HelloWindow(frame, font);
		frame.setVisible(true);
	}
}
