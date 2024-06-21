import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class PortListener extends Thread{
	ArrayList<Client> list;
	ServerSocket server_socket;
	int top=0;
	PortListener(int port){
		list=new ArrayList<Client>();
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
				list.add(top,new Client(socket));
				list.get(top++).start();
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
	}
}