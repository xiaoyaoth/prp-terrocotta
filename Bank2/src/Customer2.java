

import java.io.File;
import java.io.FileWriter;

import simulation.modeling.*; //���潨ģ��ϵͳ��

public class Customer2 extends DefaultBelief
{
	private int cash;
	
	public Customer2() //�û��Զ���Ĺ��캯��
	{
		this(100);
	}

	public Customer2(Integer cash) //�û��Զ���Ĺ��캯��
	{
		this.cash = cash;
	}
	
	
	public synchronized void write(String s)
	{
		try {
			File file = new File("outC.txt");
			FileWriter fw = new FileWriter(file, true);
			fw.write(s);
			fw.close();
		}
		catch (Exception ex){}
	}

	public void receiveMoney(Integer incr)
	{
		this.cash += incr;
		System.out.println("" + this + "��$" + incr + " received");
	}

	public void consumeMoney()
	{
		int temp = (int)(Math.random() * 10);
		this.cash -= temp;
		System.out.println("" + this + "��$" + temp + " consumed");
	}

	public String toString()
	{
		return "Customer" + this.getID() + "��Cash��$" + this.cash + "��";
	}

	public void myPrint()
	{
		System.out.println("" + this);
	}
}