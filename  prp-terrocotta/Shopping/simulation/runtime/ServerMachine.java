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

public class ServerMachine implements MainInterface {

	/**** 构建分布式系统需要的成员 ****/
	public boolean conv_fini = false;
	//public Map<Integer, Agent> map;
	//public Map<Integer, Casher> shopper_map;
	public ArrayList<Tuple> caseTable;
	private ClockTick clk;
	public int agentNum;
	public Map<Integer,DefaultBelief> agentlist;
	public ArrayList<Integer> idlist;
	public ArrayList<Path> pathlist;

	// public GUI gui;
	/****
	 * 构建分布式系统需要的成员
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 ****/
	
	public ClockTick getClock()
	{
		return this.clk;
	}
	
	ServerMachine() throws IOException, ParserConfigurationException,
			SAXException {
		caseTable = (new Parse()).table;
//		map = new HashMap<Integer, Agent>();
//		shopper_map = new HashMap<Integer, Casher>();
		agentlist = new HashMap<Integer, DefaultBelief>();
		conv_fini = false;
		clk = new ClockTick(this);
		System.out.println("clk in contruction " + clk);
		agentNum = 0;
		idlist = new ArrayList<Integer>();
		pathlist = new ArrayList<Path>();
	}

	public static void main(String[] args) throws IOException {
		try {
			ServerMachine oneCase = new ServerMachine();
			for(int i = 0; i < oneCase.caseTable.size(); i++){
				Tuple oneTuple = oneCase.caseTable.get(i);
				Integer jvm_id = (int) (Math.random() * ClientMachine.JVM_counter);
				oneTuple.JVM_id = jvm_id;
				if(oneTuple.agTy.equals("Agent"))
					oneCase.agentNum++;
				// should be deleted later...
				
				//oneCase.agentNum++;
				//should be added later...
			}
			System.out.println(oneCase);
			System.out.println(oneCase.getAgentList(Agent.class));
			synchronized (ClientMachine.cases) {
				ClientMachine.cases.add(oneCase);
				oneCase.conv_fini = true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void control(int order) {
		if (order == 1) {
			System.out.println("\nSimulation Started...");
			clk.incLeft(1000);
			new Thread(clk).start();
		} else if (order == 2) {
			System.out.println("\nSimulation Paused...");
			clk.goOn = false;
		} else if (order == 3) {
			System.out.println("\nSimulation Continued...");
			clk.goOn = true;
		}
	}

	public int getTotal() {
		return agentNum;
	}
	
	public <T> ArrayList<T> getAgentList(Class<T> targetClass) {
		ArrayList<T> ans = new ArrayList<T>();
		try {
			for (int i=0; i<agentlist.size(); i++)
				if (agentlist.get(i).getClass().equals(targetClass)) ans.add((T)agentlist.get(i));
		}
		catch (Exception ex) { ex.printStackTrace(); }
		finally { return ans; }
	}

	public <T> ArrayList<T> getAgentList(Class<T> targetClass, Path path) {
		ArrayList<T> ans = new ArrayList<T>();
		for (int i=0; i<idlist.size(); i++)
			if (pathlist.get(i).equals(path)) ans.add((T)agentlist.get(idlist.get(i)));
		return ans;
	}
}
