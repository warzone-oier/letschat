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
import java.util.ArrayList;
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
	private byte[] crypt(Cipher cipher,byte[] s,int cryptLength) throws Exception{
		ArrayList<Byte> list=new ArrayList<>();
		for(int i=0;i<s.length;i+=cryptLength){
			final int blockSize=Math.min(cryptLength,s.length-i);
			byte[] block=new byte[blockSize];
			System.arraycopy(s,i,block, 0, blockSize);
			block=cipher.doFinal(block);
			for(byte b:block) list.add(b);
		}
		byte[] out=new byte[list.size()];
		for(int i=0;i<out.length;++i)
			out[i]=list.get(i);
		return out;
	}
	/**发送信息，若网络中断则抛出异常*/
	public void send(byte s[]) throws IOException{
		System.out.println("iii");
		if(s.length==1){//单字节不加密
			output.writeInt(1);
			output.write(s);
			return;
		}
		Cipher cipher;
		byte[] code;
		try{
			cipher=Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE,publickey);

			code=crypt(cipher,s,245);
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		output.writeInt(code.length);
		output.write(code);
		System.out.println("ooo");
	}
	/**接收信息，若网络中断则抛出异常*/
	public byte[] receive() throws IOException{
		final int length=input.readInt();
		byte s[]=input.readNBytes(length);
		if(length==1) return s;//单字节不加密
		Cipher cipher;
		try{//分段解密
			cipher=Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE,keypair.getPrivate());
			return crypt(cipher,s,128);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**发送字符串 */
	public void send(String s) throws IOException{
		send(s.getBytes());
	}
	/**接收字符串 */
	public String receiveString() throws IOException{
		return new String(receive());
	}
	/**发送图片 */
	public void send(BufferedImage image) throws IOException{
		ByteArrayOutputStream bout=new ByteArrayOutputStream();
		ImageIO.write(image,"png",bout);
		send(bout.toByteArray());
	}
	/**接收图片 */
	public BufferedImage receiveImage() throws IOException{
		byte[] s=receive();
		BufferedImage out;
		ByteArrayInputStream bin=new ByteArrayInputStream(s);
		out=ImageIO.read(bin);
		return out;
	}
	static public final String filter=" \"*?\\|/:";
	//以下是发送的命令表
	static public final byte changeCaptcha=0;
	static public final byte checkCaptcha=1;
	static public final byte success=2;
	static public final byte failed=3;
	static public final byte login=4;
	static public final byte register=5;
	static public final byte invaildName=6;
	static public final byte longName=7;
	static public final byte onlineUser=8;
	static public final byte offlineUser=9;
	static public final byte sendmassage=10;
}
