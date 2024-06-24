import java.awt.Font;
import java.io.IOException;

public class ClientMain{
	public static Font font;
	public static Server server;
	public static void main(String[] args){
		server=new Server();
		StartWindow start=new StartWindow();
	}
}
