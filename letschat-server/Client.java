import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;


public class Client extends Thread{
	private Network network;
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
	private void checkCaptcha() throws IOException{//验证码
		while(true){
			byte command=network.receive()[0];
			if(command==Network.changeCaptcha){
				captcha=new Captcha();
				network.send(captcha.image);
			}else if(command==Network.checkCaptcha){
				String s=network.receiveString();
				if(s.equals(captcha.s)){
					byte[] out={Network.success};
					network.send(out);
					command=network.receive()[0];
					String name=network.receiveString();
					String password=network.receiveString();
					if(command==Network.login){//登录信息


						network.send(out);
						return;
					}else{//注册信息

						
						network.send(out);
						return;
					}
				}
				byte[] out={Network.captchaFailed};
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
