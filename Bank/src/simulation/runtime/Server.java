package simulation.runtime;

import simulation.modeling.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server implements Runnable, Serializable {
	private Object tcLock;
	private int JVM_id;
	private int pointer;
	public static Map<Integer, Client> cases = new HashMap<Integer, Client>();
	public static ArrayList<Integer> casesID = new ArrayList<Integer>();
	public static ArrayList<Server> servers = new ArrayList<Server>();
	private List<DefaultBelief> agents = new ArrayList<DefaultBelief>();
	private List<DefaultBelief> waitList = new ArrayList<DefaultBelief>();

	Server() {
		tcLock = new Object();
		JVM_id = this.hashCode();
		System.out.println("JVM " + JVM_id + " starts");
	}

	public static void main(String[] args) {
		Server c = new Server();
		synchronized (Server.servers) {
			Server.servers.add(c);
		}
		new Thread(c).start();
	}

	public void run() {
		Integer id = this.JVM_id;
		synchronized (this.tcLock) {
			this.pointer = 0;
		}
		while (true) {
			for (DefaultBelief ag : waitList) {
				ag.setMain(cases.get(ag.getCaseID()));
				new Thread(ag).run();
				synchronized (this.agents) {
					this.agents.add(ag);
				}
				synchronized (this.waitList) {
					this.waitList.remove(ag);
				}
			}
			if (cases.size() - 1 >= pointer) {
				System.out.println("pointer " + pointer + "cases.size "
						+ cases.size());
				Client oneCase = cases.get(casesID.get(pointer));
				if (oneCase.isFinished()) {
					ArrayList<Tuple> table = oneCase.getTable();
					System.out.println(table);
					for (int i = 0; i < table.size(); i++) {
						DefaultBelief ag = null;
						Tuple one = table.get(i);
						if (one.JVM_id == id) {
							Object[] args = new Object[one.args.size()];
							for (int j = 0; j < one.args.size(); j++)
								args[j] = Integer.parseInt(one.args.get(j)
										.toString());

							Object tempObj;
							try {
								System.out.println(one.agTy);
								tempObj = InvokeMethod.newInstance(one.agTy,
										args);
								if (tempObj instanceof DefaultBelief) {
									ag = (DefaultBelief) tempObj;
									ag.setMain(oneCase);
									ag.setID(one.id);
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
					System.out
							.println("[This scenario is completely arranged!]");
					System.out.println();
					//oneCase.control(1, oneCase.getTicks());
					if(!oneCase.getClock().isGoOn())
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

	public static int assign(int mode) {
		int sel;
		switch (mode) {
		case 0:
			sel = (int) (Math.random() * servers.size());
			return Server.servers.get(sel).JVM_id;
		case 1:
			Server se = Server.servers.get(0);
			for (Server s : Server.servers)
				if (s.agents.size() + s.waitList.size() < se.agents.size()
						+ se.waitList.size())
					se = s;
			return se.JVM_id;
		case 2:
			
		default:
			return -1;
		}
	}

	public void migrate() throws IOException, ClassNotFoundException {
		java.util.Scanner input = new java.util.Scanner(System.in);
		System.out.println(cases);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);

		DefaultBelief a = this.agents.get(0);
		a.setMigrate(true);
		System.out.println(a);
		out.writeObject(a);
		synchronized (this.agents) {
			this.agents.remove(0);
		}
		out.writeObject("haha");
		out.flush();

//		 synchronized (Server.servers) {
//		 Server.servers.remove(this);
//		 }

		input.next();
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bin);
		DefaultBelief ag = (DefaultBelief) in.readObject();

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
	}

	public void migrateAll() throws IOException, ClassNotFoundException {
		java.util.Scanner input = new java.util.Scanner(System.in);
		System.out.println(cases);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		System.out.println(this.agents);
		int size = this.agents.size();
		synchronized (this.agents) {
			for (int i = 0; i < size; i++) {
				DefaultBelief ag = this.agents.get(i);
				ag.setMigrate(true);
				out.writeObject(ag);
				System.out.println(ag);
			}

			this.agents.clear();
		}

		out.writeObject("haha");
		out.flush();

		// synchronized (Server.servers) {
		// Server.servers.remove(this);
		// }

		System.out.println(this.agents);
		System.out.println("\nstuck in the middle");
		input.next();
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bin);
		while (true) {
			Object newAg = in.readObject();
			System.out.println(newAg);
			if (newAg instanceof String)
				break;
			DefaultBelief ag = (DefaultBelief) newAg;
			Client c = cases.get(ag.getCaseID());
			if (c == null)
				System.out.println(ag.getCaseID());
			if (ag == null)
				System.out.println(" tnnd i'm null ");
			synchronized (c.agentList) {
				c.agentList.put(ag.getID(), ag);
			}
			Server s = Server.servers.get((int) Math.random()
					* Server.servers.size());
			synchronized (s.waitList) {
				s.waitList.add(ag);
			}
		}
		System.out.println(this.agents);
		System.out.println("Migrate fini");
		input.next();
	}
}
