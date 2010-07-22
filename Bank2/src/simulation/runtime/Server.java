package simulation.runtime;

import simulation.modeling.*;

import java.util.ArrayList;

public class Server extends Thread {
	private int JVM_id;
	public static int JVM_counter;
	private int pointer;
	public static ArrayList<Client> cases = new ArrayList<Client>();

	Server() {
		JVM_id = JVM_counter;
		System.out.println("JVM " + JVM_id + " starts");
	}

	public static void main(String[] args) {
		Server c = new Server();
		JVM_counter++;
		c.start();
	}

	public void run() {
		Integer id = JVM_id;
		pointer = 0;
		while (true) {
			if (cases.size() - 1 >= pointer) 
			{
				System.out.println("pointer " + pointer + "cases.size "	+ cases.size());
				Client oneCase = cases.get(pointer);
				if (oneCase.isFinished())
				{
					ArrayList<Tuple> table = oneCase.getTable();
					System.out.println(table);
					for (int i = 0; i < table.size(); i++) 
					{
						DefaultBelief ag = null;
						Tuple one = table.get(i);
						if (one.JVM_id == id) 
						{
							Object[] args = new Object[one.args.size()];
							for (int j = 0; j < one.args.size(); j++)
								args[j] = Integer.parseInt(one.args.get(j).toString());

							Object tempObj;
							try {
								tempObj = InvokeMethod.newInstance(one.agTy, args);
								if (tempObj instanceof DefaultBelief) 
								{
									ag = (DefaultBelief) tempObj;
									ag.setMain(oneCase);
									ag.setID(one.id);
									System.out.println("I am here");
									this.addPc(oneCase, ag);			
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
					oneCase.control(1, oneCase.getTicks());
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
	
	public void addPc(Client oneCase, DefaultBelief ag){
		for (int i=0; i<oneCase.getPC().size(); i++)
		{
			String cName = oneCase.getPC().get(i).cName;
			if (ag.getClass().getName().equals(cName))
			{
				System.out.print(ag.getClass().getName() + " ");
				System.out.println(cName);
				PlanCondition pc = new PlanCondition(oneCase.getPC().get(i).interval, 
						oneCase.getPC().get(i).needTicks, oneCase.getPC().get(i).funcName);
				ag.addPC(pc);
			}
		}
	}
}
