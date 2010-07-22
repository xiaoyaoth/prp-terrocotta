

import java.io.File;
import java.io.FileWriter;

import simulation.modeling.*; //仿真建模的系统类

public class Customer2 extends DefaultBelief
{
	private int cash;
	
	public Customer2() //用户自定义的构造函数
	{
		this(100);
	}

	public Customer2(Integer cash) //用户自定义的构造函数
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
		System.out.println("" + this + "：$" + incr + " received");
	}

	public void consumeMoney()
	{
		int temp = (int)(Math.random() * 10);
		this.cash -= temp;
		System.out.println("" + this + "：$" + temp + " consumed");
	}

	public String toString()
	{
		return "Customer" + this.getID() + "（Cash：$" + this.cash + "）";
	}

	public void myPrint()
	{
		System.out.println("" + this);
	}
}