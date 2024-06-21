import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread{
	Socket socket;
	InputStream input;
	OutputStream output;
	Scanner scan;
	Client(Socket s){
		s=socket;
		try {
			input=s.getInputStream();
			output=s.getOutputStream();
			scan=new Scanner(input);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void run(){
	}
}
