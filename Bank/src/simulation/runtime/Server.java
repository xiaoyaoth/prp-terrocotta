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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import simulation.modeling.DefaultBelief;
import simulation.modeling.InvokeMethod;
import simulation.modeling.PlanCondition;

//import java.util.Scanner;

public class Server implements Runnable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object tcLock;
	private int pointer;

	private final static String AGENTS_OUT_FILE_FOLDER = "agentsOut//";
	private final static String AGENTS_IN_FILE_FOLDER = "agentsIn//";
	private final static File dir = new File(AGENTS_IN_FILE_FOLDER);

	public static int finiCaseNumber;
	public static Map<Integer, Client> cases = new HashMap<Integer, Client>();
	public static LinkedList<Client> casesID = new LinkedList<Client>();
	public static Map<Integer, ServerInformation> serverInfo = new HashMap<Integer, ServerInformation>();
	// public static ArrayList<Server> servers = new ArrayList<Server>();
	// private List<DefaultBelief> agents = new ArrayList<DefaultBelief>();
	private ServerInformation sInfo;
	private PerformanceThread perfthread;

	private class PerformanceThread implements Runnable {
		private int cpuUsage;
		private int memAvail;
		private int machineAbility;
		private int loopCount;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				synchronized (tcLock) {
					int cpuTemp = CPU.INSTANCE.getCpuUsage();
					int cpuAve = 0;
					if (cpuTemp <= 100) {
						this.loopCount++;
						this.cpuUsage += cpuTemp;
						cpuAve = this.cpuUsage / this.loopCount;
					}
					this.memAvail = MEM.INSTANCE.getMEMUsage();
					this.machineAbility = this.memAvail * (100 - cpuAve);
					System.out.println(" LoopCount:" + this.loopCount
							+ " CpuTemp:" + cpuTemp + " MEM:" + this.memAvail
							+ " CPU:" + cpuAve + " EventCount:"
							+ sInfo.eventCount);
					sInfo.eventCount = 0;
				}
				synchronized (serverInfo) {
					sInfo.perf = this.machineAbility;
					serverInfo.put(sInfo.JVM_id, sInfo);
				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public class ServerInformation {
		private final static int PORT = 10000;
		private String ip = "192.168.131.1";
		private int JVM_id;
		private int perf;
		private int eventCount;

		public String getIp() {
			return this.ip;
		}

		public void addEventCount() {
			this.eventCount++;
		}
	}

	/* debug */
	// private Scanner in = new Scanner(System.in);

	Server() {
		Server.deleteAllAgentsFile();
		this.sInfo = new ServerInformation();
		this.perfthread = new PerformanceThread();
		// new GetFile(PORT).start();
		tcLock = new Object();
		this.sInfo.JVM_id = this.hashCode();
		try {
			this.sInfo.ip = InetAddress.getLocalHost().getHostAddress()
					.toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		System.out.println("JVM " + this.sInfo.JVM_id + " starts");
	}

	public int getJVMId() {
		return this.sInfo.JVM_id;
	}

	public String getIp() {
		return this.sInfo.ip;
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		Server c = new Server();
		c.setIp(in.next());
		new Thread(c).start();
	}

	public void run() {
		synchronized (this.tcLock) {
			this.pointer = Server.finiCaseNumber;
		}
		new Thread(this.perfthread).start();
		while (true) {
			this.initLoop();
			this.mainLoop();
		}
	}

	public void addPc(Client oneCase, DefaultBelief ag) {
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

	public static Integer assign() {
		int mode = 0;
		Iterator<Integer> iter = Server.serverInfo.keySet().iterator();

		switch (mode) {
		case 0:// random choose
			int res = 0;
			for (int i = 0; i < Server.serverInfo.size() * Math.random(); i++)
				res = iter.next();
			return res;
			// case 1:// based on agent_list size and wait_list size
			// se = Server.servers.get(0);
			// for (Server s : Server.servers)
			// if (s.agents.size() < se.agents.size())
			// se = s;
			// return se;
		case 2:// based on machine performanc
			int bestPerf = 0;
			int bestId = -1;
			while (iter.hasNext()) {
				int tempId = iter.next();
				int tempPerf = Server.serverInfo.get(tempId).perf;
				if (tempPerf > bestPerf) {
					bestPerf = tempPerf;
					bestId = tempId;
				}
			}
			return bestId;
		case 3:// based on agents' relationship with each other
			/*
			 * actually it should be the logic of the Client instead, the Server
			 * just responsible for return proper JVM_id from the perspective of
			 * Hardware thus should not burden the work to assign a agent a
			 * specific JVM_id
			 */
		case 4:

		default:
			return null;
		}
	}

	public void migrate() throws IOException, ClassNotFoundException {
		// System.out.println(Server.cases);
		// DefaultBelief a = this.agents.get(0);
		// System.out.println(a);
		// a.setMigrate(true);
		// synchronized (this.agents) {
		// this.agents.remove(a);
		// }
	}

	public void recover() throws IOException, ClassNotFoundException {
		File[] flist = Server.dir.listFiles();
		for (File f : flist) {
			File mig = f;
			FileInputStream fin = new FileInputStream(mig);
			ObjectInputStream objin = new ObjectInputStream(fin);
			DefaultBelief ag = (DefaultBelief) objin.readObject();
			Client c = Server.cases.get(ag.getCaseID());
			synchronized (c.agentList) {
				c.agentList.put(ag.getID(), ag);
			}
			/* newly modified */
			// synchronized (this.agents) {
			// this.agents.add(ag);
			// }
			ag.setMain(Server.cases.get(ag.getCaseID()));
			ag.setMigrate(false);
			new Thread(ag).start();

			try {
				objin.close();
				fin.close();
				f.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.print("&&" + ag.getOwnTick() + " ");
			System.out.print(ag);
			System.out.println(" Clock now is "
					+ ag.getMain().getClock().getNow());
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
		int realPointer = this.pointer - Server.finiCaseNumber;
		// System.out.println("casesID's size is " + casesID.size(
		// + ", realPointer is " + realPointer);
		if (Server.casesID.size() > realPointer) {
			Client oneCase = casesID.get(realPointer);
			if (oneCase.isFinished()) {
				ArrayList<Tuple> table = oneCase.getTable();
				System.out.println(table);
				for (int i = 0; i < table.size(); i++) {
					System.out.println("i<table.size()");
					DefaultBelief ag = null;
					Tuple one = table.get(i);
					if (one.JVM_id == this.sInfo.JVM_id) {
						Object[] args = new Object[one.args.size()];
						for (int j = 0; j < one.args.size(); j++)
							args[j] = Integer.parseInt(one.args.get(j));

						Object tempObj;
						try {
							System.out.println("agent_type " + one.agTy);
							tempObj = InvokeMethod.newInstance(one.agTy, args);
							args = null;
							if (tempObj instanceof DefaultBelief) {
								ag = (DefaultBelief) tempObj;
								ag.setMain(oneCase);
								ag.setID(one.id);
								ag.setPath(one.path);
								ag.setIp(this.sInfo.ip);
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

						synchronized (oneCase.agentList) {
							oneCase.agentList.put(ag.getID(), ag);
						}
						synchronized (oneCase.getPathList()) {
							oneCase.getPathList().add(one.path);
						}
						synchronized (oneCase.getIDList()) {
							oneCase.getIDList().add(ag.getID());
						}
						new Thread(ag).start();
					}
				}
				System.out.println("[This scenario is completely arranged!]");
				System.out.println();

				// oneCase.control(1, oneCase.getTicks());
				if (!oneCase.getClock().isGoOn())
					oneCase.startClock();
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

	public boolean isStillAvailable() {
		return true;
	}

	public void setIp(String ip) {
		this.sInfo.ip = ip;
	}
}
