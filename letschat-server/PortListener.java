import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class PortListener extends Thread{
	ServerSocket server_socket;
	PortListener(int port){
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
					client=new Client(socket,Main.keypair);
				}catch(Exception e){
					exception=true;
				}
				if(exception==false){
					client.setPriority(1);
					client.start();
				}
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
	}
}