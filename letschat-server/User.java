import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.ImageIO;
public class User{//某个用户
	String name;
	private boolean lock;//用户锁（整个类的所有函数的读写同步）
	private LinkedList<Client> clients;//用户所在的所有线程
	private HashMap<Long,Chat> chatMap;  //用户所在的所有聊天
	User(String id){
		name=id;
		clients=new LinkedList<Client>();
		chatMap=new HashMap<Long,Chat>();
		lock=false;
	}
	/** 将客户端加入该用户， */
	public synchronized void addClient(Client client){
		while(lock);
		lock=true;
		clients.add(client);
		try{
			File file=new File(Main.userFolder+"/"+name+"/avatar");
			BufferedImage image=ImageIO.read(file);
			client.network.send(image);
		}catch(IOException e){
			e.printStackTrace();
			//不可能有异常
		}
		lock=false;
	}
	/** 改头像 */
	public synchronized void changeAvatar(BufferedImage image){
		while(lock);
		lock=true;
		File file=new File(Main.userFolder+"/"+name+"/avatar");
		file.delete();
		file=new File(Main.userFolder+"/"+name+"/avatar");
		try{
			file.createNewFile();
			ImageIO.write(image,"png", file);
		}catch(IOException e){
			e.printStackTrace();
			lock=false;
			return;//不可能有异常
		}
		for(Iterator<Client> ite=clients.iterator();ite.hasNext();){
			Client client=ite.next();
			if(client.isAlive()){
				byte[] command={Network.changeAvatar};
				try{
					client.network.send(command);
					client.network.send(image);
					System.out.println("...");
				}catch(IOException e){//该方法由其他 client 发布
					ite.remove();//连接超时，移除
				}
			}else ite.remove();
		}
		lock=false;
	}
}
