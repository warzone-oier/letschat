import java.net.Socket;
import java.security.KeyPair;

public class Client extends Thread{
	Network network;
	Client(Socket s,KeyPair key) throws Exception{
		network.keypair=key;
		if(network.connect(s)) throw new Exception();
	}
	public void run(){
		
	}
}
