import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
public class Network{//通信类，用于 socket 的加密通信
	KeyPair keypair;//自身密钥对来自外部
	private PublicKey publickey;
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	Network(){}
	public boolean connect(Socket s){//传入对应的 socket，初始化输入输出流，发生异常返回 true
		try{
			socket=s;
			input=new DataInputStream(socket.getInputStream());
			output=new DataOutputStream(socket.getOutputStream());
			byte[] code=keypair.getPublic().getEncoded();
			output.writeUTF(Base64.getEncoder().encodeToString(code));
			//writeUTF() 和 readUTF():
			//前两个字节表示输入输出长度 (unsigned short)，之后为 UTF-8 格式的字符串
			//适合通信，一次 writeUTF() 唯一对应一次 readUTF()
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
	public synchronized void send(String s) throws IOException{//发送信息，若网络中断则抛出异常
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
	}
	public synchronized String receive() throws IOException{//接收信息，若网络中断则抛出异常
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
