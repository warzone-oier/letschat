import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ShellClient{
	static Scanner scan;
	static Server server;
	static String getToken(){//读取一行，提取出唯一的 Token
		String get=scan.nextLine();
		int begin=0;
		while(begin<get.length()&&get.charAt(begin)==' ') ++begin;
		if(begin==get.length()) return " ";//这一行没有 Token 输出 " "
		int end=begin;
		while(end<get.length()&&get.charAt(end)!=' ') ++end;
		String s=get.substring(begin,end);
		while(end<get.length()&&get.charAt(end)==' ') ++end;
		if(end!=get.length()) return null;//这一行有多个 Token 输出 null
		return s;
	}
	static String setAddress(){
		System.out.println("请输入服务器IP地址:");
		String s=getToken();
		if(s==null) return null;
		if(s.equals(" ")) return " ";
		int cnt=0;
		for(int i=0;i<s.length();){
			for(int get=0;i<s.length()&&'0'<=s.charAt(i)&&s.charAt(i)<='9';++i){
				get=get*10+(s.charAt(i)-'0');
				if(get>=256) return null;
			}
			if(i<s.length()){
				if(s.charAt(i)!='.') return null;
				++i;++cnt;
				if(cnt==4) return null;
			}
		}
		if(cnt!=3) return null;
		return s;
	}
	static int setPort(){
		System.out.println("请输入服务器端口:");
		String s=getToken();
		if(s==null) return -1;
		if(s.equals(" ")) return -2;
		int get=0;
		for(int i=0;i<s.length();++i)
			if('0'<=s.charAt(i)&&s.charAt(i)<='9'){
				get=get*10+(s.charAt(i)-'0');
				if(get>=65536) return -1;
			}else return -1;
		return get;
	}
	static String address;
	static int port;
	static void connect(){
		boolean fail=true;
		do{
			for(address=null;address==null;){
				address=setAddress();
				if(address==null) System.out.println("无效地址！");
				if(address.equals(" ")) address=null;
			}
			for(port=-1;port==-1;){
				port=setPort();
				if(port==-1) System.out.println("无效端口！");
				if(port==-2) port=-1;
			}
			try{
				fail=server.connect(new Socket("http://"+address, port));
			}catch(Exception e){
				fail=true;
			}
			if(fail) System.out.println("网络有问题");
		}while(fail);
	}
	public static void main(String args[]){
		scan=new Scanner(System.in);
		server=new Server();
		connect();
		String name=null;
		String password=null;
		boolean fail=true;
		do{
			while(name==null){
				System.out.println("请输入用户名:");
				name=getToken();
				if(name==null) System.out.println("无效用户名！");
				if(name.equals(" ")) name=null;
			}
			while(password==null){
				System.out.println("请输入密码:");
				password=getToken();
				if(password==null) System.out.println("无效密码！");
				if(password.equals(" ")) password=null;
			}
			try{
				fail=server.login(name, password);
				if(fail) System.out.println("用户名或密码错误");
			}catch(IOException e){
				fail=true;
				System.out.println("网络中断，请检查网络");
				connect();
			}
		}while(fail);
	}
}
