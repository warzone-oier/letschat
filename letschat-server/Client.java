import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
public class Client extends Thread{
	public Network network;
	private User user;
	private Captcha captcha;
	Client(Socket s,KeyPair key) throws IOException{
		network=new Network();
		try{
			network.keypair=key;
		}catch(Exception e){
			e.printStackTrace();
		}
		if(network.connect(s)) throw new IOException();
		captcha=new Captcha();
		network.send(captcha.image);
		setPriority(1);
	}
	/** 检查用户名是否合法 */
	private boolean namecheck(String name){
		char get[]=name.toCharArray();
		char filt[]=Network.filter.toCharArray();
		for(char c:get)
			for(char p:filt)
				if(c==p) return false;
		return true;
	}
	/** 登录 */
	private boolean login(String name,String password) throws IOException{
		//System.out.println("login");
		user=Main.users.get(name);
		if(user==null) return false;
		File file=new File(Main.userFolder+"/"+name+"/password");
		FileInputStream fin=new FileInputStream(file);
		DataInputStream bin=new DataInputStream(fin);
		if(!password.equals(bin.readUTF())) return false;///检查密码是否正确
		return true;
	}
	/** 注册 */
	private boolean register(String name,String password) throws IOException{
		//System.out.println("register");
		user=Main.users.get(name);
		if(user!=null) return false;
		File file=new File(Main.userFolder+"/"+name);
		file.mkdir();
		//保存密码
		file=new File(Main.userFolder+"/"+name+"/password");
		file.createNewFile();
		FileOutputStream fout=new FileOutputStream(file);
		DataOutputStream bout=new DataOutputStream(fout);
		bout.writeUTF(password);
		user=new User(name);
		new Thread(){
			public void run(){
				Thread bef=Main.setLock(this);
				if(bef!=null) try{
					bef.join();
				}catch(InterruptedException e){}
				Main.users.put(name,user);
			}
		}.start();
		return true;
	}
	/** 检查验证码 */
	private void checkCaptcha() throws IOException{
		while(true){
			byte command=network.receive()[0];
			if(command==Network.checkCaptcha){
				String s=network.receiveString();
				byte[] out={Network.success};
				if(captcha.check(s)){
					network.send(out);
					command=network.receive()[0];
					String name=network.receiveString();
					String password=network.receiveString();
					if(name.length()>=32)
						out[0]=Network.longName;
					else if(!namecheck(name))
						out[0]=Network.invaildName;
					else if(command==Network.login){
						if(login(name,password)){
							network.send(out);
							return;
						}else out[0]=Network.failed; 
					}else if(register(name,password)){
						network.send(out);
						return;
					}else out[0]=network.failed;
				}else out[0]=Network.failed;
				network.send(out);
			}
			captcha=new Captcha();
			network.send(captcha.image);
		}
	}
	public void run(){
		try{
			checkCaptcha();
			user.addClient(this);
			while(true){//命令接收
				byte[] command=network.receive();
				if(command[0]==Network.sendmassage){
					String receiver=network.receiveString();
					String text=network.receiveString();
					user.send(receiver,text);
				}
			}
		}catch(IOException e){
			user.deleteClient(this);
		}
	}
}
