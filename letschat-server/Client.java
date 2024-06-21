import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;

public class Client extends Thread{
	Network network;
	Client(Socket s,KeyPair key) throws Exception{
		network.keypair=key;
		System.out.println("0");
		if(network.connect(s)) throw new Exception();
	}
	public void run(){
		try{
			network.send("F");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
