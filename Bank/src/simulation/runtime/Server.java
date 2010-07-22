package simulation.runtime;

import simulation.modeling.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server implements Runnable {
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
		synchronized (servers) {
			servers.add(c);
		}
		new Thread(c).start();
	}

	public void run() {
		Integer id = this.JVM_id;
		synchronized (this.tcLock) {
			this.pointer = 0;
		}
		while (true) {
			for(DefaultBelief ag: waitList){
				ag.setMain(cases.get(ag.getCaseID()));
				new Thread(ag).run();
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
					oneCase.control(1, oneCase.getTicks());
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

	public static int assign() {
		int sel = (int) (Math.random() * servers.size());
		return servers.get(sel).JVM_id;
	}
	
	public void migrate() throws IOException, ClassNotFoundException{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();		
		ObjectOutputStream out = new ObjectOutputStream(bout);		
		for(DefaultBelief ag: this.agents){
			ag.setMigrate(true);
			out.writeObject(ag);
			this.agents.remove(ag);
		}
		out.writeObject("end of serialize");
		out.flush();
		
		Server.servers.remove(this);
		
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bin);
		Object newAg = in.readObject();
		DefaultBelief ag = (DefaultBelief)newAg;
		if(!(newAg instanceof String)){			
			Client c = cases.get(ag.getCaseID());
			c.agentList.put(ag.getID(), ag);
			Server s = Server.servers.get(assign());
			s.waitList.add(ag);
		}
	}
}
