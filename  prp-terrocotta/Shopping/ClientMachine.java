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
			if (cases.size()-1>=pointer) {
				ServerMachine oneSenario = cases.get(pointer);
				if (oneSenario.conv_fini) {
					ArrayList<ArrayList<Integer>> oneCase = oneSenario.caseTable;
					System.out.println(oneCase);
					for (int i = 0; i < oneCase.size(); i++) {
						Agent ag = null;
						ArrayList<Integer> one = oneCase.get(i);
						if (one.get(0).equals(id)) {
							System.out.println(one.get(1)+" "+one.get(2));
							ag = new Agent(one.get(1),one.get(2));
							synchronized (oneSenario.map) {
								oneSenario.map.put(ag.getID(), ag);
							}
						}
						System.out.println(ag);
						new Thread(ag).start();
					}
					System.out
							.println("[This scenario is completely arranged!]");
					System.out.println();
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
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
