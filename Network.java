import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import javax.imageio.ImageIO;
/** 通信类，用于 socket 的加密通信*/
public class Network{
	KeyPair keypair;//自身密钥对来自外部
	private PublicKey publickey;
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private final int cryptLength=240;
	Network(){}
	/**传入对应的 socket，初始化输入输出流，发生异常返回 true*/
	public boolean connect(Socket s){
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
	public boolean disconnect(){
		try{input.close();}
		catch(IOException e){
			return true;}
		try{output.close();
		}catch(IOException e){
			return true;}
		try{socket.close();}
		catch(IOException e){
			return true;}
		input=null;
		output=null;
		socket=null;
		publickey=null;
		return false;
	}
	/**发送信息，若网络中断则抛出异常*/
	public synchronized void send(byte s[]) throws IOException{
		Cipher cipher;
		String code;
		try{
			cipher=Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE,publickey);
			final int block=(s.length-1)/cryptLength+1;
			byte out[]=new byte[block*256];
			for(int i=0;i<block;++i){//分段加密
				byte now[]=new byte[i==block-1? s.length-i*cryptLength:cryptLength];
				for(int j=0;j<cryptLength&&i*cryptLength+j<s.length;++j)
					now[j]=s[i*cryptLength+j];
				now=cipher.doFinal(now);
				for(int j=0;j<256;++j)
					out[i*256+j]=now[j];
			}
			code=Base64.getEncoder().encodeToString(out);
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		output.writeUTF(code);
	}
	/**接收信息，若网络中断则抛出异常*/
	public synchronized byte[] receive() throws IOException{
		Cipher cipher;
		String code=input.readUTF();
		try{//分段解密
			cipher=Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE,keypair.getPrivate());
			byte s[]=Base64.getDecoder().decode(code);
			final int block=s.length>>8;
			byte now[]=new byte[256];
			for(int i=0;i<256;++i)
				now[i]=s[(block-1)*256+i];
			now=cipher.doFinal(now);
			byte out[]=new byte[(block-1)*256+now.length];
			for(int i=0;i<now.length;++i)
				out[(block-1)*256+i]=now[i];
			for(int i=0;i<block-1;++i){
				now=new byte[256];
				for(int j=0;j<256;++j)
					now[j]=s[i*256+j];
				now=cipher.doFinal(now);
				for(int j=0;j<cryptLength;++j)
					out[i*cryptLength+j]=now[j];
			}
			return out;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**发送字符串 */
	public synchronized void send(String s) throws IOException{
		send(s.getBytes());
	}
	/**接收字符串 */
	public synchronized String receiveString() throws IOException{
		return new String(receive());
	}
	/**发送图片 */
	public synchronized void send(BufferedImage image) throws IOException{
		ByteArrayOutputStream bout=new ByteArrayOutputStream();
		ImageIO.write(image,"png",bout);
		send(bout.toByteArray());
	}
	/**接收图片 */
	public synchronized BufferedImage receiveImage() throws IOException{
		byte[] s=receive();
		ByteArrayInputStream bin=new ByteArrayInputStream(s);
		BufferedImage out=ImageIO.read(bin);
		return out;
	}

	//以下是发送的命令表
	static public final byte changeCaptcha=0;
	static public final byte checkCaptcha=1;
	static public final byte success=2;
	static public final byte failed=3;
	static public final byte login=4;
	static public final byte register=5;
}
