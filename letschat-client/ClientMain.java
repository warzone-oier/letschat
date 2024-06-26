import java.awt.Font;
public class ClientMain{
	public static Font font;
	public static Server server;
	public static StartWindow start;
	public static MainWindow mainWindow;
	public static void main(String[] args){
		font=new Font("黑体", Font.BOLD, 10);
		server=new Server();
		start=new StartWindow();
		mainWindow=new MainWindow();
	}
}
