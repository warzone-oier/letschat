class A{
	int a;
}
public class Main{
	public static void main(String args[]){
		A a=new A();
		A b=a;
		a.a=1;
		System.out.println(a.a);
		a.a=2;
		System.out.println(a.a);
	}
}