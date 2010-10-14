package simulation.runtime;

import simulation.modeling.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import java.util.Scanner;

public class Server implements Runnable, Serializable {
	/**
	 * 
	 */
	private final static String AGENTS_OUT_FILE_FOLDER = "agentsOut//";
	private final static String AGENTS_IN_FILE_FOLDER = "agentsIn//";
	private final static File dir = new File(AGENTS_IN_FILE_FOLDER);
	private final static int PORT = 10000;
	private String ip;
	private int JVM_id;

	private static final long serialVersionUID = 1L;
	private Object tcLock;
	private int pointer;
	public static Map<Integer, Client> cases = new HashMap<Integer, Client>();
	public static ArrayList<Integer> casesID = new ArrayList<Integer>();
	public static ArrayList<Server> servers = new ArrayList<Server>();
	private List<DefaultBelief> agents = new ArrayList<DefaultBelief>();
	private List<DefaultBelief> waitList = new ArrayList<DefaultBelief>();

	private int cpuUsage;
	private int memAvail;
	private int machineAbility;
	private int loopCount;

	/* debug */
	// private Scanner in = new Scanner(System.in);

	Server() {
		Server.deleteAllAgentsFile();
		new GetFile(PORT).start();
		tcLock = new Object();
		this.JVM_id = this.hashCode();
		try {
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.cpuUsage = 0;
		this.memAvail = 0;

		System.out.println("JVM " + this.JVM_id + " starts");
	}

	public int getJVMId() {
		return this.JVM_id;
	}

	public String getIp() {
		return this.ip;
	}

	public static void main(String[] args) {
		Server c = new Server();
		synchronized (Server.servers) {
			Server.servers.add(c);
		}
		new Thread(c).start();
	}

	public void run() {
		synchronized (this.tcLock) {
			this.pointer = 0;
			this.loopCount = 0;
		}
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

	public static Server assign() {
		int mode = 0;
		int sel;
		Server se;

		switch (mode) {
		case 0:// random choose
			sel = (int) (Math.random() * servers.size());
			return Server.servers.get(sel);
		case 1:// based on agent_list size and wait_list size
			se = Server.servers.get(0);
			for (Server s : Server.servers)
				if (s.agents.size() + s.waitList.size() < se.agents.size()
						+ se.waitList.size())
					se = s;
			return se;
		case 2:// based on machine performance
			se = Server.servers.get(0);
			for (Server s : Server.servers)
				if (s.machineAbility < se.machineAbility)
					se = s;
			return se;
		case 3:// based on agents' relationship with each other
		default:
			return null;
		}
	}

	public static int assignByGroup(DefaultBelief agent) {
		return 0;
	}

	public void migrate() throws IOException, ClassNotFoundException {
		System.out.println(Server.cases);
		DefaultBelief a = this.agents.get(0);
		System.out.println(a);
		a.setMigrate(true);
		synchronized (this.agents) {
			this.agents.remove(a);
		}
	}

	public void recover() throws IOException, ClassNotFoundException {
		File[] flist = dir.listFiles();
		System.out.println(flist);
		for (File f : flist) {
			File mig = f;
			FileInputStream fin = new FileInputStream(mig);
			ObjectInputStream objin = new ObjectInputStream(fin);
			DefaultBelief ag = (DefaultBelief) objin.readObject();
			Client c = Server.cases.get(ag.getCaseID());
			synchronized (c.agentList) {
				c.agentList.put(ag.getID(), ag);
			}
			ag.setMain(Server.cases.get(ag.getCaseID()));
			ag.setMigrate(false);
			System.out.println(ag);
			Server s = Server.servers.get((int) Math.random()
					* Server.servers.size());
			synchronized (s.waitList) {
				s.waitList.add(ag);
			}

			try {
				objin.close();
				fin.close();
				f.delete();
				System.out.println("******" + ag + "*****");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void deleteAllAgentsFile() {
		File dir = new File(AGENTS_IN_FILE_FOLDER);
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
		//			
		// synchronized (this.tcLock) {
		// int cpuTemp = CPU.INSTANCE.getCpuUsage();
		// if (cpuTemp < 100)
		// this.cpuUsage = (this.cpuUsage + cpuTemp)
		// / (++this.loopCount);
		// this.memAvail = MEM.INSTANCE.getMEMUsage();
		// this.machineAbility = this.memAvail * this.cpuUsage;
		// }
		System.out.println("init Loop 2");
		for (DefaultBelief ag : this.waitList) {
			ag.setMain(Server.cases.get(ag.getCaseID()));
			synchronized (this.agents) {
				this.agents.add(ag);
			}
			synchronized (this.waitList) {
				this.waitList.remove(ag);
			}
			new Thread(ag).start();// the problem is here, I once wrote it as
			// new Thread(ag).run()
		}
		System.out.println("init Loop 3");
	}

	public void mainLoop() {
		if (cases.size() - 1 >= pointer) {
			System.out.println("pointer " + pointer + " cases.size "
					+ cases.size());
			Client oneCase = cases.get(casesID.get(pointer));
			if (oneCase.isFinished()) {
				ArrayList<Tuple> table = oneCase.getTable();
				System.out.println(table);
				for (int i = 0; i < table.size(); i++) {
					DefaultBelief ag = null;
					Tuple one = table.get(i);
					if (one.JVM_id == this.JVM_id) {
						Object[] args = new Object[one.args.size()];
						for (int j = 0; j < one.args.size(); j++)
							args[j] = Integer.parseInt(one.args.get(j)
									.toString());

						Object tempObj;
						try {
							System.out.println(one.agTy);
							tempObj = InvokeMethod.newInstance(one.agTy, args);
							if (tempObj instanceof DefaultBelief) {
								ag = (DefaultBelief) tempObj;
								ag.setMain(oneCase);
								ag.setID(one.id);
								ag.setPath(one.path);
								this.addPc(oneCase, ag);
								synchronized (tcLock) {
									this.agents.add(ag);
								}
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
				synchronized (tcLock) {
					pointer++;
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
}