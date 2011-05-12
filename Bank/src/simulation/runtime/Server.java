/*
 * ���������µ�eclipse��terracotta��װterracotta��ʱ������һ������
 * ˵ʲôpde.core��pde.uiû���ҵ�
 * ��help��install new software������pde��
 * ��һ��buckminister pde support, pde resources he pde tools description���������ͺ���
 */
/* 
 * Feb 12 : ����һ��config file�� ��config fileд��ip��ַ
 * ������θ���������ȡip��ַ��
 * ���������м�¼��һ��ip��ַ����tc-config�ļ����ڼ������ip��ַ��
 */
/*
 * March 21:ɾ����Server�µ�agent��Arraylist,��Ȼ�ڴ�һֱ�ͷŲ���
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

	private final static String AGENTS_OUT_FILE_FOLDER = "agentsOut//";
	private final static String AGENTS_IN_FILE_FOLDER = "agentsIn//";
	private final static File dir = new File(AGENTS_IN_FILE_FOLDER);

	// public static int finiCaseNumber;
	// public static Map<Integer, AgentsMgr> cases = new HashMap<Integer,
	// AgentsMgr>();
	// public static LinkedList<AgentsMgr> casesID = new
	// LinkedList<AgentsMgr>();
	public static Map<Integer, ServerInformation> serverInfo = new HashMap<Integer, ServerInformation>();
	// public static ArrayList<Server> servers = new ArrayList<Server>();
	// private List<DefaultBelief> agents = new ArrayList<DefaultBelief>();
	private ServerInformation sInfo;
	private PerformanceThread perfthread;

	private class PerformanceThread implements Runnable {
		// private int cpuUsage;
		// private int memAvail;
		private int machineAbility;
		private int loopCount;
		private int weakPoint;
		private int JVM_id;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				synchronized (tcLock) {
					if (sInfo.agentTotal > 0)
						sInfo.ratio = sInfo.agentCount / sInfo.agentTotal;
					else
						sInfo.ratio = 0;
					System.out.println(" LoopCount:" + this.loopCount
							+ sInfo.eventCount + " AgentCount:"
							+ sInfo.agentCount + " AgentTotal:"
							+ sInfo.agentTotal + " ratio:" + sInfo.ratio);
					if (sInfo.agentTotal > 0) {
						if (sInfo.ratio < 100)
							// weakPoint++;
							weakPoint = 0;
						else
							weakPoint = 0;
					} else
						weakPoint = 0;
					int size = ScenariosMgr.getSnrs().size();
					if (this.weakPoint == 3 && size > 0) {
						Scenario c = ScenariosMgr.getSnrs().get(size - 1);
						if (c != null)
							c.setMigrate();
						else
							System.out.println("Client is null");
						this.weakPoint = 4;
					}
					sInfo.eventCount = 0;
					sInfo.agentCount = 0;
				}
				synchronized (serverInfo) {
					sInfo.perf = this.machineAbility;
					serverInfo.put(sInfo.JVM_id, sInfo);
				}
				try {
					Thread.sleep(1000);
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
		private int agentCount;
		private int agentTotal;
		private double ratio;
		private Lock tcLock = new Lock();

		public String getIp() {
			return this.ip;
		}

		public synchronized void addEventCount() {
			this.eventCount++;
		}

		public synchronized void addAgentCount() {
			this.agentCount++;
		}

		public synchronized void decAgentTotal() {
			// System.out.println("decAgentTotal Called");
			this.agentTotal--;
		}

		public synchronized void incAgentTotal() {
			this.agentTotal++;
		}

		public int getPerf() {
			return this.perf;
		}

		public double getRatio() {
			return this.ratio;
		}
	}

	/* debug */
	// private Scanner in = new Scanner(System.in);

	Server() {
		Server.deleteAllAgentsFile();
		this.sInfo = new ServerInformation();
		this.perfthread = new PerformanceThread();
		// new GetFile(PORT).start();
		tcLock = new Lock();
		this.sInfo.JVM_id = this.hashCode();
		this.perfthread.JVM_id = this.sInfo.JVM_id;
		try {
			this.sInfo.ip = InetAddress.getLocalHost().getHostAddress()
					.toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		new ScenariosMgr();
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
		c.setIp("localhost");
		Thread serverThread = new Thread(c);
		serverThread.setName("ServerThread");
		serverThread.start();
		GetFile getFile = null;
		try {
			getFile = new GetFile(10000);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("�޷������ļ�!");
			System.exit(1);
		}
		getFile.setName("getFileThread");
		getFile.start();
	}

	public void run() {
		synchronized (this.tcLock) {
			this.pointer = ScenariosMgr.finiCaseNum;
		}
		Thread perfThread = new Thread(this.perfthread);
		perfThread.setName("PerformanceThread");
		perfThread.start();
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
			Scenario c = ScenariosMgr.getSnrs().get(ag.getCaseID());
			c.putAgent(ag);
			/* newly modified */
			// synchronized (this.agents) {
			// this.agents.add(ag);
			// }
			ag.setMain(ScenariosMgr.getSnrs().get(ag.getCaseID()));
			ag.setMigrate(false);
			synchronized (this.tcLock) {
				this.sInfo.incAgentTotal();
			}
			System.out.print(ag.getID() + " nextTick:" + ag.isNextTick() + " ");
			new Thread(ag).start();

			try {
				objin.close();
				fin.close();
				f.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("recover " + ag.getID());
			//
			// System.out.print("&&" + ag.getOwnTick() + " ");
			// System.out.print(ag);
			// System.out.println(" Clock now is "
			// + ag.getMain().getClock().getNow());
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
		System.out.println("case: "+ l.size() +" pointer: "+this.pointer+" finiNum: "+ScenariosMgr.finiCaseNum);
		if (m.size() == l.size()
				&& ScenariosMgr.getSnrIDs().size() > realPointer) {
			Scenario oneCase = m.get(l.get(realPointer));
			if (oneCase.isCfgFinished()) {
				ArrayList<Tuple> table = oneCase.getTable();
				System.out.println(oneCase.getCaseID() + " " + table);
				for (int i = 0; i < table.size(); i++) {
					// System.out.println("i<table.size()");
					DefaultBelief ag = null;
					Tuple one = table.get(i);
					if (one.JVM_id == this.sInfo.JVM_id) {
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
								ag.setIp(this.sInfo.ip);
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

	public void setIp(String ip) {
		this.sInfo.ip = ip;
	}
}
