import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;


public class Client extends Thread{
	private Network network;
	private User user;
	Client(Socket s,KeyPair key) throws IOException{
		network=new Network();
		try{
			network.keypair=key;
		}catch(Exception e){
			e.printStackTrace();
		}
		if(network.connect(s)) throw new IOException();
	}
	private boolean Login(String name,String password){//返回值：是否登录成功
		return true;
	}
	private boolean Register(String name,String password){//返回值：是否注册成功
		return true;
	}
	
	public void run(){
		while(true){
			try{
				String command=network.receive();
				if(command=="text"){
					String chat=network.receive();
					String text=network.receive();
					user.sendText(chat,text);
				}
			}catch(IOException e){
				return;
			}
		}
	}
}
