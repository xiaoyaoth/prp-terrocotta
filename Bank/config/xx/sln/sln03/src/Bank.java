

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import simulation.modeling.*; //仿真建模的系统类

public class Bank extends DefaultBelief
{
	private int cash;

	public Bank() //用户自定义的构造函数
	{
		this(1000);
	}
	
	public Bank(Integer cash) //用户自定义的构造函数
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

	public void sendMoney()
	{
		ArrayList<Customer> custList = this.main.getAgentList(Customer.class);
		int temp = (int)(Math.random() * 20), index = (int)(Math.random() * custList.size());
		Customer cust = custList.get(index);
		this.addMess(true, new MessageInfo(this, cust, "receiveMoney(" + temp + ")"));
		this.cash -= temp;
		System.out.println("" + this + " Sent $" + temp + " to " + cust);
	}

	public String toString()
	{
		return "Bank" + this.getID() + "（Cash：$" + this.cash + "）";
	}

	public void myPrint()
	{
		System.out.println("" + this);
	}
}