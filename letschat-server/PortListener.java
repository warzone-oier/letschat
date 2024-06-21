import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.Vector;

public class PortListener extends Thread{
	Vector<Client> list;
	ServerSocket server_socket;
	KeyPair keypair;
	int top=0;
	PortListener(int port,KeyPair key){
		list=new Vector<Client>();
		keypair=key;
		try{
			server_socket=new ServerSocket(port);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public void run(){
		while(true){
			try{
				Socket socket=server_socket.accept();
				boolean exception=false;
				Client client=null;
				try{
					client=new Client(socket, keypair);
					
				}catch(Exception e){
					exception=true;
				}
				list.add(client);++top;
				if(exception==false) client.start();
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
	}
}