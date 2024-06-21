import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
class Network{
	KeyPair keypair;
	PublicKey publickey;
	Socket socket;
	DataInputStream input;
	DataOutputStream output;
	Network(){
		KeyPairGenerator keyPairGenerator;
		try{
			keyPairGenerator=KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keypair=keyPairGenerator.generateKeyPair();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
	}
	public boolean connect(String address,int port){
		try{
			socket=new Socket(address,port);
			input=new DataInputStream(socket.getInputStream());
			output=new DataOutputStream(socket.getOutputStream());
			output.writeUTF(Base64.getEncoder().encodeToString(keypair.getPublic().getEncoded()));
			output.writeChar('\n');
			byte[] publicKeyBytes=Base64.getDecoder().decode(input.readUTF());
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory;
			try{
				keyFactory = KeyFactory.getInstance("RSA");
				publickey=keyFactory.generatePublic(keySpec);
			}catch(Exception e){
				e.printStackTrace();
			}
		}catch(IOException e){
			return true;
		}
		return false;
	}
	public void send(String s) throws IOException{//发送信息，若网络中断则抛出异常
		Cipher cipher;
		String code;
		try{
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE,publickey);
			code=Base64.getEncoder().encodeToString(cipher.doFinal(s.getBytes()));
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		output.writeUTF(code);
		output.writeChar('\n');
	}
	public String receive() throws IOException{//接收信息，若网络中断则抛出异常
		Cipher cipher;
		try{
			cipher=Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE,keypair.getPrivate());
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
		String code=input.readUTF();
		try{
			code=new String(cipher.doFinal(Base64.getDecoder().decode(code)));
		}catch(Exception e){
			e.printStackTrace();
		}
		return code;
	}
}
public class Server extends Network{
	public boolean login(String name,String password) throws IOException{
		String fail;
		send(name);
		send(password);
		fail=receive();
		return fail.equals("T");
	}
}
