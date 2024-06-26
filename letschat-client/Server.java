import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
public class Server extends Network{
	Server(){//客户端只需要一个 keypair
		KeyPairGenerator keyPairGenerator;
		try{
			keyPairGenerator=KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024);
			keypair=keyPairGenerator.generateKeyPair();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
	}
}