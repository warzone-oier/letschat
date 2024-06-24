import java.awt.Font;
import java.io.IOException;

public class ClientMain{
	public static Font font;
	public static Server server;
	public static void main(String[] args){
		server=new Server();
		try{
			server.send("I'm Client");
			System.out.println(server.receiveString());
		}catch(IOException e){
			e.printStackTrace();
		}
		StartWindow start=new StartWindow();
	}
}
