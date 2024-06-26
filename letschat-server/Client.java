import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
	private boolean login(String name,String password) throws IOException{
		System.out.println("login");
		user=Main.users.get(name);
		if(user==null) return false;
		File file=new File(Main.userFolder+name+"/password");
		FileInputStream fin=new FileInputStream(file);
		DataInputStream bin=new DataInputStream(fin);
		if(!password.equals(bin.readUTF())) return false;///检查密码是否正确
		user.addClient(this);
		return true;
	}
	private boolean register(String name,String password) throws IOException{
		System.out.println("register");
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
		//创建默认头像
		Path source=Paths.get(Main.userFolder+"/defaultAvatar");
		Path dest=Paths.get(Main.userFolder+"/"+name+"/avatar");
		Files.copy(source,dest,StandardCopyOption.REPLACE_EXISTING);

		user=new User(name);
		Main.users.put(name,user);
		user.addClient(this);
		return true;
	}
	private void checkCaptcha() throws IOException{//验证码
		while(true){
			byte command=network.receive()[0];
			if(command==Network.changeCaptcha){
				captcha=new Captcha();
				network.send(captcha.image);
			}else if(command==Network.checkCaptcha){
				String s=network.receiveString();
				if(captcha.check(s)){
					byte[] out={Network.success};
					network.send(out);
					command=network.receive()[0];
					String name=network.receiveString();
					String password=network.receiveString();
					if(name.length()>=128){
						out[0]=Network.longName;
						network.send(out);
					}else if(!namecheck(name)){
						out[0]=Network.invaildName;
						network.send(out);
					}else if(command==Network.login){
						if(login(name,password)){
							network.send(out);
							return;
						}
					}else if(register(name,password)){
						network.send(out);
						return;
					}
				}
				byte[] out={Network.failed};
				network.send(out);
				captcha=new Captcha();
				network.send(captcha.image);
			}
		}
	}
	public void run(){
		try{
			checkCaptcha();
		}catch(IOException e){
			return;
		}
	}
}
