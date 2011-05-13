

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;

import simulation.modeling.*; //仿真建模的系统类

public class Customer extends DefaultBelief implements Serializable
{
	private int cash;
	
	public Customer() //用户自定义的构造函数
	{
		this(100);
	}

	public Customer(Integer cash) //用户自定义的构造函数
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

	public synchronized void receiveMoney(Integer incr)
	{
		this.cash += incr;
		//System.out.println("" + this + "：$" + incr + " received");
	}

	public void consumeMoney()
	{
		int temp = (int)(Math.random() * 10);
		this.cash -= temp;
		//System.out.println("" + this + "：$" + temp + " consumed");
	}

	public String toString()
	{
		return "Tick:" + this.getTick() +  " Customer" + this.getID() + "(Cash：$" + this.cash + ")";
	}

	public void myPrint()
	{
		//System.out.println("" + this);
	}
}