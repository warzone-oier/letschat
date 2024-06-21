import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;

public class Client extends Thread{
	Network network;
	Client(Socket s,KeyPair key) throws IOException{
		network=new Network();
		try{
			network.keypair=key;
		}catch(Exception e){
			e.printStackTrace();
		}
		if(network.connect(s)) throw new IOException();
	}
	public void run(){
		try{
			network.send("F");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
