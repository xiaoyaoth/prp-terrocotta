package simulation.runtime;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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

	/* members need be maintained when the Scenario is given birth */
	private static String root = "config\\xx\\";

	private Map<Integer, DefaultBelief> agentList;
	private ArrayList<Integer> idList;
	private ArrayList<Path> pathList;
	private Integer caseID;
	private boolean hasMiged;
	private long migStart, migEnd;

	// private int agentNum, totalTicks, prior;
	// private String usr;
	private Parse p;

	/* temporary members used by server to run the simulation */
	transient private ArrayList<Func> pc = new ArrayList<Func>();
	transient private ArrayList<Tuple> caseTable;

	/* members changed when migrate happens */
	transient private Integer hostID;
	transient private boolean cfgFini = false;
	transient private boolean execFini = false;
	transient private Lock tcLock = new Lock();
	transient private ClockTick clk;

	public Scenario(Scenario s) {
		this.agentList = new HashMap<Integer, DefaultBelief>(s.agentList);
		this.idList = new ArrayList<Integer>(s.idList);
		this.pathList = new ArrayList<Path>(s.pathList);
		this.caseID = s.caseID;
		this.hasMiged = s.hasMiged;
		this.migStart = s.migStart;
		this.migEnd = s.migEnd;
		this.p = s.p;
	}

	public Scenario(Parse p) throws IOException, ParserConfigurationException,
			SAXException {
		this.p = p;
		caseTable = p.getTable();
		getFileList(root + p.getSlnPath());
		agentList = new HashMap<Integer, DefaultBelief>();
		cfgFini = false;
		clk = new ClockTick(this);
		System.out.println("clk in contruction " + clk);
		idList = new ArrayList<Integer>();
		pathList = new ArrayList<Path>();
		this.caseID = new Integer(ScenariosMgr.newSnrID());
		this.hostID = new Integer(p.getHostId());
		this.hasMiged = false;
		this.control(1, this.getTicks());
	}

	public void recover(int hostId) {
		this.cfgFini = true;
		this.execFini = false;
		this.tcLock = new Lock();
		this.hostID = hostId;
		this.migEnd = WallTime.getInstance().getTime();
	}

	public synchronized void makeNewClock(ClockTick clk) {
		this.clk = clk;
	}

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
		return this.p.getTick();
	}
	
	public int getPriority(){
		return this.p.getPrior();
	}

	public ArrayList<Func> getPC() {
		return this.pc;
	}

	public Integer getHostID() {
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

	public void run() {
		try {
			if (!this.hasMiged) {
				System.out.println("in Scenarios.java hostId:" + this.hostID);
				for (int i = 0; i < this.caseTable.size(); i++) {
					Tuple oneTuple = this.caseTable.get(i);
					oneTuple.JVM_id = this.hostID;
				}
				ScenariosMgr.add(this);
				synchronized (this.tcLock) {
					this.cfgFini = true;
					this.tcLock.wait();
				}
			}
			this.startClock();
			synchronized (this.clk.getTcLock()) {
				this.cfgFini = true;
				this.clk.getTcLock().wait();
			}
			/* added on March 21 */
			System.out.println("1. Scenario fini in Scenario.java");
			if ((this.migEnd != 0 && this.migStart != 0)
					|| (this.migEnd == 0 && this.migStart == 0))
				this.output(this.hashCode() + "", this.outputInfo());
			synchronized (this.tcLock) {
				// oneCase.caseTable.clear();
				this.clean(this.pc);
				this.clean(this.pathList);
				this.clean(this.idList);
				this.clean(this.caseTable);
				this.clean(this.agentList);
				this.clk.setMain(null);
				this.clk = null;
				if (p != null)
					this.clean(p.getTable());
				this.p = null;
				this.execFini = true;
				ScenariosMgr.remove(this);
				System.gc();
			}
			synchronized (ScenariosMgr.getSnrs()) {
				ScenariosMgr.getSnrs().notifyAll();
				System.out.println("in Scenario.java, adjustLock notified");
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
		Thread clkThread = new Thread(clk);
		clkThread.setPriority(this.p.getPrior());
		clkThread.start();
		System.out.println("clk started, in Scenario.java");
	}

	public int getTotal() {
		return this.p.getAgentTotalNum();
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
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				@SuppressWarnings("unused")
				T t = list.remove(i);
				t = null;
			}
			list.clear();
		}
		System.out.println("list" + list);
		list = null;
	}

	public <T1, T2> void clean(Map<T1, T2> list) {
		for (int i = 0; i < list.size(); i++) {
			@SuppressWarnings("unused")
			T2 t = list.remove(i);
			t = null;
		}
		list.clear();
		System.out.println("list" + list);
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
				// System.out.println(temp);
				if (!temp.equals(".svn")) {
					int pos = temp.indexOf("_");
					String cName = temp.substring(0, pos);
					temp = temp.substring(pos + 1);
					pos = temp.indexOf("(");
					String fName = temp.substring(0, pos);
					temp = temp.substring(temp.indexOf("^") + 1);
					// System.out.println(temp);
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

	public Lock getTcLock() {
		return this.tcLock;
	}

	public boolean isHasMiged() {
		return this.hasMiged;
	}

	public void print() {
		System.out.println("******print Scenario*********");
		System.out.println(this);
		System.out.println("cfgFini:\t " + this.cfgFini);
		System.out.println("hasMig:\t " + this.hasMiged);
		System.out.println("caseId:\t " + this.caseID);
		System.out.println("parse:\t " + this.p);
		System.out.println("totalTick:\t " + this.p.getTick());
		System.out.println("agentNum:\t " + this.p.getAgentTotalNum());
		System.out.println("******print Scenario end*****");
	}

	private void output(String fileId, String s) {
		// System.out.println(s);
		File fo = new File("statistics\\" + fileId + ".txt");
		try {
			FileWriter fw = new FileWriter(fo, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append(s);
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String outputInfo() {
		String execDuration = this.clk.getDuration();
		String basicInfo = "usr:" + this.p.getUsr() + " tick:"
				+ this.p.getTick() + " prior:" + this.p.getPrior();
		String migInfo = "migStart:" + this.migStart + " migEnd:"
				+ this.migEnd + " migCost:" + (this.migEnd - this.migStart);
		return execDuration + "\n" + basicInfo + "\n" + migInfo;
	}

	public void setMigrate(int hostID) {
		System.out.println(this.caseID + " calls setMigrate, in Scenario.java");
		synchronized (this.tcLock) {
			this.migStart = WallTime.getInstance().getTime();
		}
		Integer dest = ScenariosMgr.assign();
		ArrayList<DefaultBelief> migAgList = new ArrayList<DefaultBelief>();

		/* pic ag to mig */
		if (Server.serverInfo.get(dest).getRatio() > PerformanceThread
				.getThreshold()) {
			synchronized (this.agentList) {
				for (DefaultBelief ag : this.agentList.values()) {
					if (ag.getHostServerID() == hostID
							&& ag.getHostServerID() != dest) {
						ag.setMigrate(true);
						migAgList.add(ag);
					} else {
						System.out.print("d==h ");
					}
				}
			}
			for (DefaultBelief ag : migAgList)
				while (!ag.isNextTick() || ag.isMigrate()) {
					System.out.print(ag.getID() + "migloop ");
					this.output(this.clk.hashCode() + " " + ag.getID(),
							ag.debugMessage() + "\n" + clk.debugMessage()
									+ "##################################\n");
					this.clk.notifyNowLock();
				}
			if (this.clk != null) {
				this.clk.setMigrate(true);
			}
			System.out.println("in Scenario.java, setMigrate phase1 fini");
			this.writeScenarioIntoFile(migAgList, dest);
		} else
			System.out.println("in Scenario.java, no Proper Server found");
	}

	private void writeScenarioIntoFile(ArrayList<DefaultBelief> migAgList,
			Integer dest) {
		String filePath = "agentsOut\\" + this.hashCode();
		synchronized (this.tcLock) {
			this.hasMiged = true;
		}
		File f;
		ObjectOutputStream oos;
		FileOutputStream fos;

		try {
			f = new File(filePath);
			fos = new FileOutputStream(f);
			oos = new ObjectOutputStream(fos);
			this.writeScenarioCore(oos, migAgList);
			oos.close();
			fos.close();
			new SendFile(Server.serverInfo.get(dest).getIp(), f);
			/* file is deleted in SendFile.run() */
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("************in Scenario.java***************");
			System.out.println(this.clk);
			System.out.println(this.clk.getNowLock());
			System.out.println("************in Scenario.java***************");
		}
	}

	private byte[] writeSenarioIntoByteArray(ArrayList<DefaultBelief> migAgList) {
		synchronized (this.tcLock) {
			this.hasMiged = true;
		}
		ObjectOutputStream oos;
		ByteArrayOutputStream baos;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			this.writeScenarioCore(oos, migAgList);
			oos.close();
			baos.close();
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("************in Scenario.java***************");
			System.out.println(this.clk);
			System.out.println(this.clk.getNowLock());
			System.out.println("************in Scenario.java***************");
		}
		return null;
	}

	private void writeScenarioCore(ObjectOutputStream oos,
			ArrayList<DefaultBelief> migAgList) throws IOException {
		synchronized (this.tcLock) {
			Scenario inFileSnr = new Scenario(this);
			oos.writeObject(inFileSnr);/*
										 * this line has
										 * concurrencyModificationException
										 */
			oos.flush();
			inFileSnr = null;
		}
		System.out.println("write scenario in byte, phase 1 fini");
		synchronized (this.tcLock) {
			oos.writeObject(this.clk);
			oos.flush();
		}
		System.out.println("write scenario in byte, phase 2 fini");

		// this.clk.notifyNowLock();/* wake up all the agents, let them die
		// */
		for (DefaultBelief ag : migAgList) {
			try {
				oos.writeObject(ag);
				oos.flush();
				ag.notifyTcLock();
				System.out.print(ag.getID() + " ");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("****************");
				ag.printDebugMessage();
				System.out.println("****************");
			}
		}
		System.out.println("write scenario in byte, phase 3 fini");
		this.clk.print();
		/* wake up clk, let it die, clk fini */
		this.clk.notifyTickLock();
		/* wake up Scenario, let it die, scenario fini */
		this.clk.notifyTcLock();
		this.clk.print();

		oos.writeObject(null);
		System.out.println("3. mig in file now, Scenario.java");
	}
}
