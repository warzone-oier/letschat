import java.awt.Font;
public class ClientMain{
	public static Font font;
	public static Network server;
	public static StartWindow start;
	public static MainWindow mainWindow;
	public static void main(String[] args){
		font=new Font("黑体", Font.BOLD, 10);
		server=new Network();
		server.keypair=Network.generateKeyPair();
		start=new StartWindow();
	}
}
