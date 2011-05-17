package simulation.runtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import simulation.modeling.ClockTick;
import simulation.modeling.DefaultBelief;
import simulation.modeling.Lock;
import simulation.modeling.MainInterface;
import simulation.modeling.Path;

public class Scenario implements Runnable, MainInterface, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**** 构建分布式系统需要的成员 ****/
	private static String root = "config\\xx\\";
	private boolean cfgFini = false;
	private boolean execFini = false;
	private ArrayList<Tuple> caseTable;
	private ClockTick clk;
	private int agentNum, totalTicks;
	private Map<Integer, DefaultBelief> agentList;
	private ArrayList<Integer> idList;
	private ArrayList<Path> pathList;
	private ArrayList<Func> pc = new ArrayList<Func>();
	private Integer caseID;
	private Parse p;	
	private Integer hostID;
	
	private int unmigRemains;
	private Integer migHost;

	private Lock tcLock = new Lock();

	public boolean isCfgFinished() {
		return this.cfgFini;
	}

	public boolean isExecFinished() {
		return this.execFini;
	}

	public ArrayList<Tuple> getTable() {
		return this.caseTable;
	}

	public void setTable(ArrayList<Tuple> table) {
		synchronized (this.caseTable) {
			this.caseTable = table;
		}
	}

	public ArrayList<Integer> getIDList() {
		return this.idList;
	}

	public void setIDList(ArrayList<Integer> idList) {
		synchronized (this.idList) {
			this.idList = idList;
		}
	}

	public ArrayList<Path> getPathList() {
		return this.pathList;
	}

	public int getTicks() {
		return this.totalTicks;
	}

	public ArrayList<Func> getPC() {
		return this.pc;
	}
	
	public Integer getHostID(){
		return this.hostID;
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

	public Scenario(String usr, String tick) throws IOException,
			ParserConfigurationException, SAXException {
		this.totalTicks = Integer.parseInt(tick);
		String configPath = root + "USER\\" + usr + "\\snr.xml";
		p = new Parse(configPath);
		caseTable = p.getTable();
		getFileList(root + p.getSlnPath());
		agentList = new HashMap<Integer, DefaultBelief>();
		cfgFini = false;
		clk = new ClockTick(this);
		System.out.println("clk in contruction " + clk);
		agentNum = 0;
		idList = new ArrayList<Integer>();
		pathList = new ArrayList<Path>();
		this.caseID = this.hashCode();
		this.hostID = ScenariosMgr.assign();
		this.control(1, this.getTicks());
	}

	public void run() {
		try {
			System.out.println("client half fini2");
			System.out.println(this.hostID);
			for (int i = 0; i < this.caseTable.size(); i++) {
				Tuple oneTuple = this.caseTable.get(i);
				oneTuple.JVM_id = this.hostID;
				this.agentNum++;
			}
			System.out.println("client half fini3");

			ScenariosMgr.add(this);
			synchronized (this.tcLock) {
				this.cfgFini = true;
			}
			System.out.println("client half fini4 " + this.getCaseID());
			// Scanner input = new Scanner(System.in);
			// while (true) {
			// input.next();
			// Server.servers.get(0).migrate();
			// }
			while (!this.getClock().isFini()) {
				Thread.sleep(1000);
			}
			/* added on March 21 */
			System.out.println("fini");
			this.output(this.getClock().getDuration() + " " + " "
					+ this.totalTicks);
			synchronized (this.tcLock) {
				// oneCase.caseTable.clear();
				this.clean(this.pc);
				this.clean(this.pathList);
				this.clean(this.idList);
				this.clean(this.caseTable);
				this.clean(this.agentList);
				this.clk.setMain(null);
				this.clk = null;
				this.p.getTable().clear();
				this.p = null;
				this.execFini = true;
				this.tcLock = null;
				ScenariosMgr.remove(this);
				ScenariosMgr.finiCaseNum++;
				System.gc();
			}
			/* fini */
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void control(int order, int totalTicks) {
		if (order == 1) {
			System.out.println("\nSimulation Started...");
			clk.incLeft(totalTicks);
			// new Thread(clk).start();
		} else if (order == 2) {
			System.out.println("\nSimulation Paused...");
			clk.setGoOn(false);
		} else if (order == 3) {
			System.out.println("\nSimulation Continued...");
			clk.setGoOn(true);
		}
	}

	public void startClock() {
		new Thread(clk).start();
	}

	public int getTotal() {
		return agentNum;
	}

	@SuppressWarnings({ "finally", "unchecked" })
	public <T> ArrayList<T> getAgentList(Class<T> targetClass) {
		ArrayList<T> ans = new ArrayList<T>();
		try {
			for (int i = 0; i < idList.size(); i++)
				if (agentList.get(idList.get(i)).getClass().equals(targetClass))
					ans.add((T) agentList.get(idList.get(i)));
		} catch (Exception ex) {
			System.out.println(this.agentList);
			System.out.println(this.idList);
			ex.printStackTrace();
		} finally {
			return ans;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> ArrayList<T> getAgentList(Class<T> targetClass, Path path) {
		ArrayList<T> ans = new ArrayList<T>();
		for (int i = 0; i < idList.size(); i++)
			if (pathList.get(i).equals(path)
					&& agentList.get(i).getClass().equals(targetClass))
				ans.add((T) agentList.get(idList.get(i)));
		return ans;
	}

	public <T> void clean(ArrayList<T> list) {
		for (int i = 0; i < list.size(); i++) {
			@SuppressWarnings("unused")
			T t = list.remove(i);
			t = null;
		}
		list.clear();
		list = null;
		System.out.println(list);
	}

	public <T1, T2> void clean(Map<T1, T2> list) {
		for (int i = 0; i < list.size(); i++) {
			@SuppressWarnings("unused")
			T2 t = list.remove(i);
			t = null;
		}
		list.clear();
		list = null;
	}

	public Map<Integer, DefaultBelief> getAgentList() {
		return this.agentList;
	}

	public synchronized void putAgent(DefaultBelief ag) {
		synchronized (this.agentList) {
			this.agentList.put(ag.getID(), ag);
		}
	}

	@Override
	public DefaultBelief getAgent(int id) {
		// TODO Auto-generated method stub
		return this.agentList.get(id);
	}

	public Integer getCaseID() {
		return this.caseID;
	}

	public void getFileList(String slnPath) {
		File file = new File(slnPath + "\\flc");
		if (file.isDirectory()) {
			String[] strList = file.list();
			for (int i = 0; i < strList.length; i++) {
				String temp = strList[i];
				System.out.println(temp);
				if (!temp.equals(".svn")) {
					int pos = temp.indexOf("_");
					String cName = temp.substring(0, pos);
					temp = temp.substring(pos + 1);
					pos = temp.indexOf("(");
					String fName = temp.substring(0, pos);
					temp = temp.substring(temp.indexOf("^") + 1);
					System.out.println(temp);
					int interval = Integer.parseInt(temp.substring(0,
							temp.indexOf("^")));
					temp = temp.substring(temp.indexOf("^") + 1);
					int needTicks = Integer.parseInt(temp.substring(0,
							temp.indexOf("^")));
					pc.add(new Func(cName, fName, interval, needTicks));
				}
			}
		}
	}

	class Func implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String funcName, cName;
		int interval;
		int needTicks;

		Func(String cName, String funcName, int interval, int needTicks) {
			this.cName = cName;
			this.funcName = funcName;
			this.interval = interval;
			this.needTicks = needTicks;
		}
	}

	public void output(String s) throws IOException {
		File fo = new File("statistics\\" + this.hashCode() + ".txt");
		FileWriter fw = new FileWriter(fo);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(s);
		bw.flush();
		bw.close();
		fw.close();
	}

	public void setMigrate(int hostID) {
		System.out.println("setMigrate in Client is called");
		this.migHost = hostID;
		int dest = ScenariosMgr.assign();
		for (DefaultBelief ag : this.agentList.values())
			if (ag.getHostServerID() == hostID && ag.getHostServerID() != dest){
				ag.setMigrate(true, dest);
				this.incRemainedAfterMig();
			}
			else
				System.out.print("d==h ");
	}
	
	public synchronized void decRemainedNumAfterMig(){
		this.unmigRemains--;
		if(this.unmigRemains == 0)
			Server.serverInfo.get(this.migHost).getPerfThread().notifyTcLock();
	}
	
	public synchronized void incRemainedAfterMig(){
		this.unmigRemains++;
	}
}
