import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
public class Server extends Network{
	Server(){//客户端只需要一个 keypair
		KeyPairGenerator keyPairGenerator;
		try{
			keyPairGenerator=KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keypair=keyPairGenerator.generateKeyPair();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
	}
	public boolean login(String name,String password) throws IOException{
		String fail;
		sendString(name);
		sendString(password);
		fail=receiveString();
		return fail.equals("T");
	}
}