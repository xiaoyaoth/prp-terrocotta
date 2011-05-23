/*
 * 今天用了新的eclipse和terracotta，装terracotta的时候遇到一个问题
 * 说什么pde.core和pde.ui没有找到
 * 在help的install new software里搜索pde，
 * 把一个buckminister pde support, pde resources he pde tools description下载下来就好了
 */
/* 
 * Feb 12 : 设置一个config file， 在config file写上ip地址
 * 或者如何根据网卡获取ip地址？
 * 环境变量中记录了一个ip地址，是tc-config文件所在计算机的ip地址；
 */
/*
 * March 21:删除了Server下的agent的Arraylist,不然内存一直释放不掉
 */
package simulation.runtime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import simulation.modeling.DefaultBelief;
import simulation.modeling.InvokeMethod;
import simulation.modeling.Lock;
import simulation.modeling.PlanCondition;

//import java.util.Scanner;

public class Server implements Runnable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Lock tcLock;
	private int pointer;

	private final static String AGENTS_OUT_FILE_FOLDER = "agentsOut\\";
	private final static String AGENTS_IN_FILE_FOLDER = "agentsIn\\";
	private final static String SNR_FOLDER = "snrQueue\\";
	private final static File dir = new File(AGENTS_IN_FILE_FOLDER);

	public static Map<Integer, ServerInformation> serverInfo = new HashMap<Integer, ServerInformation>();
	private ServerInformation sInfo;

	// private int jVM_id;

	Server() {
		Server.deleteAllAgentsFile();
		this.sInfo = new ServerInformation(this.hashCode());
		tcLock = new Lock();
		new ScenariosMgr();
		System.out.println("JVM " + this.sInfo.getJVM_id() + " starts");
	}

	public int getJVMId() {
		return this.sInfo.getJVM_id();
	}

	public String getIp() {
		return this.sInfo.getIp();
	}

	public static void main(String[] args) throws IOException {
		File f = new File("config\\ip.txt");
		java.io.FileReader fr = new java.io.FileReader(f);
		java.io.BufferedReader br = new java.io.BufferedReader(fr);
		Server c = new Server();
		c.setIp(br.readLine());
		Thread serverThread = new Thread(c);
		serverThread.setName("ServerThread");
		serverThread.start();
	}

	public void run() {
		synchronized (this.tcLock) {
			this.pointer = ScenariosMgr.finiCaseNum;
		}
		// Thread perfThread = new Thread(this.perfthread);
		// perfThread.setName("PerformanceThread");
		// perfThread.start();
		while (true) {
			this.initLoop();
			this.mainLoop();
		}
	}

	public void addPc(Scenario oneCase, DefaultBelief ag) {
		for (int i = 0; i < oneCase.getPC().size(); i++) {
			String cName = oneCase.getPC().get(i).cName;
			if (ag.getClass().getName().equals(cName)) {
				PlanCondition pc = new PlanCondition(
						oneCase.getPC().get(i).interval,
						oneCase.getPC().get(i).needTicks, oneCase.getPC()
								.get(i).funcName);
				ag.addPC(pc);
			}
		}
	}

	public void recover() throws IOException, ClassNotFoundException {
		// File[] flist = Server.dir.listFiles();
		// for (File f : flist) {
		// if (!f.isDirectory()) {
		byte[] migbytes = this.sInfo.getMigAgents();
		if (migbytes != null) {
			ByteArrayInputStream bais = new ByteArrayInputStream(migbytes);
			ObjectInputStream objin = new ObjectInputStream(bais);
			Object obj = null;
			while ((obj = objin.readObject()) != null) {
				if (obj instanceof DefaultBelief) {
					DefaultBelief ag = (DefaultBelief) obj;
					ag.makeNewTcLock();
					Scenario c = ScenariosMgr.getSnrs().get(ag.getCaseID());
					ag.setMain(ScenariosMgr.getSnrs().get(ag.getCaseID()));
					ag.setMigrate(false);
					ag.setHostServerID(this.sInfo.getJVM_id());
					ag.setNextTick();
					c.putAgent(ag);
					synchronized (this.tcLock) {
						this.sInfo.incAgentTotal();
					}
					System.out.print(ag.getID() + " nextTick:"
							+ ag.isNextTick() + " " + ag.getTick());
					new Thread(ag).start();
					System.out.println("recover " + ag.getID());
				}
			}

			objin.close();
			bais.close();
			// }
			// }
		}
	}

	public static void deleteAllAgentsFile() {
		File dir = new File(Server.AGENTS_IN_FILE_FOLDER);
		File[] flist = dir.listFiles();
		for (File f : flist) {
			f.delete();
		}
		dir = new File(AGENTS_OUT_FILE_FOLDER);
		flist = dir.listFiles();
		for (File f : flist) {
			f.delete();
		}
	}

	public void initLoop() {
		try {
			this.recover();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void mainLoop() {
		int realPointer = this.pointer - ScenariosMgr.finiCaseNum;
		Map<Integer, Scenario> m = ScenariosMgr.getSnrs();
		ArrayList<Integer> l = ScenariosMgr.getSnrIDs();
		// System.out.print("case: "+ l.size()
		// +" pointer: "+this.pointer+" finiNum: "+ScenariosMgr.finiCaseNum+" ");
		if (m.size() == l.size()
				&& ScenariosMgr.getSnrIDs().size() > realPointer) {
			Scenario oneCase = m.get(l.get(realPointer));
			if (oneCase.isCfgFinished()) {
				ArrayList<Tuple> table = oneCase.getTable();
				// System.out.println(oneCase.getCaseID() + " " + table);
				for (int i = 0; i < table.size(); i++) {
					// System.out.println("i<table.size()");
					DefaultBelief ag = null;
					Tuple one = table.get(i);
					if (one.JVM_id == this.sInfo.getJVM_id()) {
						Object[] args = new Object[one.args.size()];
						for (int j = 0; j < one.args.size(); j++)
							args[j] = Integer.parseInt(one.args.get(j));

						Object tempObj;
						try {
							// System.out.println("agent_type " + one.agTy);
							tempObj = InvokeMethod.newInstance(one.agTy, args);
							if (tempObj instanceof DefaultBelief) {
								ag = (DefaultBelief) tempObj;
								ag.setMain(oneCase);
								ag.setID(one.id);
								ag.setPath(one.path);
								ag.setLifeCycle(oneCase.getTicks());
								ag.setHostServerID(this.getJVMId());
								this.addPc(oneCase, ag);
								// synchronized (tcLock) {
								// this.agents.add(ag);
								// }
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						oneCase.putAgent(ag);
						synchronized (oneCase.getPathList()) {
							oneCase.getPathList().add(one.path);
						}
						synchronized (oneCase.getIDList()) {
							oneCase.getIDList().add(ag.getID());
						}
						new Thread(ag).start();
						/* added on May 2nd */
						args = null;
						synchronized (tcLock) {
							this.sInfo.incAgentTotal();
						}
						// one = null;
						/**/
					}
				}
				/*
				 * added on May 2nd table = null; synchronized (oneCase) {
				 * oneCase.setTable(null); } /*
				 */
				System.out.println("[This scenario is completely arranged!]");
				System.out.println();

				// oneCase.control(1, oneCase.getTicks());
//				if (!oneCase.getClock().isGoOn())
//					oneCase.startClock();
				synchronized (oneCase.getTcLock()){
					oneCase.getTcLock().notify();
				}
				synchronized (this.tcLock) {
					this.pointer++;
				}
			}
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setIp(String ip) {
		this.sInfo.setIp(ip);
	}
}
