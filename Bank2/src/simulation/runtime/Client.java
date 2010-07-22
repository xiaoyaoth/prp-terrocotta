package simulation.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import simulation.modeling.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Client implements MainInterface {

	/**** 构建分布式系统需要的成员 ****/
	private static String root = "C:\\Users\\xiaoyaoth\\Desktop\\cnmb\\";
	private boolean finished = false;
	private ArrayList<Tuple> caseTable;
	private ClockTick clk;
	private int agentNum, totalTicks;
	public Map<Integer, DefaultBelief> agentList;
	private ArrayList<Integer> idList;
	private ArrayList<Path> pathList;
	private ArrayList<Func> pc = new ArrayList<Func>();
	
	public boolean isFinished()
	{
		return this.finished;
	}
	
	public ArrayList<Tuple> getTable()
	{
		return this.caseTable;
	}
	
	public ArrayList<Integer> getIDList()
	{
		return this.idList;
	}
	
	public ArrayList<Path> getPathList()
	{
		return this.pathList;
	}
	
	public int getTicks()
	{
		return this.totalTicks;
	}
		
	public ArrayList<Func> getPC()
	{
		return this.pc;
	}

	/****
	 * 构建分布式系统需要的成员
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 ****/

	public ClockTick getClock() {
		return this.clk;
	}

	public Client(String path, int totalTicks) throws IOException,
			ParserConfigurationException, SAXException 
	{
		this.totalTicks = totalTicks;
		Parse p = new Parse(path);
		caseTable = p.table;
		getFileList(root + p.getSlnPath());
		agentList = new HashMap<Integer, DefaultBelief>();
		finished = false;
		clk = new ClockTick(this);
		System.out.println("clk in contruction " + clk);
		agentNum = 0;
		idList = new ArrayList<Integer>();
		pathList = new ArrayList<Path>();
	}

	public static void main(String[] args) throws IOException 
	{
		try {
			Client oneCase = new Client(root + "USER\\" + 
					args[0] + "\\snr.xml", Integer.parseInt(args[1]));
			for (int i = 0; i < oneCase.caseTable.size(); i++) 
			{
				Tuple oneTuple = oneCase.caseTable.get(i);
				Integer jvm_id = (int) (Math.random() * Server.JVM_counter);
				oneTuple.JVM_id = jvm_id;
				oneCase.agentNum++;
			}
			synchronized (Server.cases) {
				Server.cases.add(oneCase);
				oneCase.finished = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void control(int order, int totalTicks) 
	{
		if (order == 1) {
			System.out.println("\nSimulation Started...");
			clk.incLeft(totalTicks);
			new Thread(clk).start();
		} else if (order == 2) {
			System.out.println("\nSimulation Paused...");
			clk.setGoOn(false);
		} else if (order == 3) {
			System.out.println("\nSimulation Continued...");
			clk.setGoOn(true);
		}
	}

	public int getTotal() {
		return agentNum;
	}

	public <T> ArrayList<T> getAgentList(Class<T> targetClass) 
	{
		ArrayList<T> ans = new ArrayList<T>();
		try {
			for (int i = 0; i < agentList.size(); i++)
				if (agentList.get(i).getClass().equals(targetClass))
					ans.add((T) agentList.get(i));
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return ans;
		}
	}

	public <T> ArrayList<T> getAgentList(Class<T> targetClass, Path path)
	{
		ArrayList<T> ans = new ArrayList<T>();
		for (int i = 0; i < idList.size(); i++)
			if (pathList.get(i).equals(path))
				ans.add((T) agentList.get(idList.get(i)));
		return ans;
	}

	public void getFileList(String slnPath)
	{
		File file = new File(slnPath + "\\flc");
		if (file.isDirectory())
		{
			String[] strList = file.list();
			for (int i = 0; i < strList.length; i++) 
			{
				String temp = strList[i];
				int pos = temp.indexOf("_");
				String cName = temp.substring(0, pos);
				temp = temp.substring(pos + 1);
				pos = temp.indexOf("(");
				String fName = temp.substring(0, pos);
				temp = temp.substring(temp.indexOf("^") + 1);
				System.out.println(temp);
				int interval = Integer.parseInt(temp.substring(0, temp.indexOf("^")));
				temp = temp.substring(temp.indexOf("^") + 1);
				int needTicks = Integer.parseInt(temp.substring(0, temp.indexOf("^")));
				pc.add(new Func(cName, fName ,interval, needTicks));
			}
		}
	}

	class Func 
	{
		String funcName, cName;
		int interval;
		int needTicks;
		
		Func(String cName, String funcName, int interval, int needTicks)
		{
			this.cName = cName;
			this.funcName = funcName;
			this.interval = interval;
			this.needTicks = needTicks;
		}
	}
}
