package simulation.runtime;

import simulation.modeling.*;

import java.util.ArrayList;

public class ClientMachine extends Thread {
	private int JVM_id;
	public static int JVM_counter;
	public int pointer;
	public static ArrayList<ServerMachine> cases = new ArrayList<ServerMachine>();

	ClientMachine() {
		JVM_id = JVM_counter;
		System.out.println("JVM " + JVM_id + " starts");
	}

	public static void main(String[] args) {
		ClientMachine c = new ClientMachine();
		JVM_counter++;
		c.start();
	}

	public void run() {
		Integer id = JVM_id;
		pointer = 0;
		while (true) {
			if (cases.size() - 1 >= pointer) {
				System.out.println("pointer " + pointer + "cases.size "
						+ cases.size());
				ServerMachine oneCase = cases.get(pointer);
				if (oneCase.conv_fini) {
					ArrayList<Tuple> table = oneCase.caseTable;
					System.out.println(table);
					for (int i = 0; i < table.size(); i++) {
						DefaultBelief ag = null;
						Tuple one = table.get(i);
						
						if (one.JVM_id == id) {
							Object[] args = new Object[one.args.size()];
							for (int j = 0; j < one.args.size(); j++)
								args[j] = Integer.parseInt(one.args.get(j).toString());

							Object tempObj;
							try {
								tempObj = InvokeMethod.newInstance("simulation.runtime."
										+ one.agTy,	args);
								if (tempObj instanceof DefaultBelief) {
									ag = (DefaultBelief) tempObj;
									ag.setMain(oneCase);
									ag.setID(one.id);
									PlanCondition pc = new PlanCondition(1, 0, "shop");
									ag.addPC(pc);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							synchronized (oneCase.agentlist) {
								oneCase.agentlist.put(ag.getID(), ag);
							}
							synchronized (oneCase.pathlist) {
								oneCase.pathlist.add(one.path);
							}
							synchronized (oneCase.idlist) {
								oneCase.idlist.add(ag.getID());
							}
							new Thread(ag).start();
						}
					}
					System.out
							.println("[This scenario is completely arranged!]");
					System.out.println();
					oneCase.control(1);
					pointer++;
				}
			} else {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
