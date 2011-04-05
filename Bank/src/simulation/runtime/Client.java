package simulation.runtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import simulation.modeling.ClockTick;
import simulation.modeling.DefaultBelief;
import simulation.modeling.MainInterface;
import simulation.modeling.Path;

public class Client implements MainInterface, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**** 构建分布式系统需要的成员 ****/
	private static String root = "config\\xx\\";
	private boolean finished = false;
	private ArrayList<Tuple> caseTable;
	private ClockTick clk;
	private int agentNum, totalTicks;
	public Map<Integer, DefaultBelief> agentList;
	private ArrayList<Integer> idList;
	private ArrayList<Path> pathList;
	private ArrayList<Func> pc = new ArrayList<Func>();
	private int caseID;

	private Path tempPath = null;
	private Server tempJVM = null;
	
	private int removeMutex;
	
	public int decMutex(){
		return this.removeMutex--;
	}

	public boolean isFinished() {
		return this.finished;
	}

	public ArrayList<Tuple> getTable() {
		return this.caseTable;
	}

	public ArrayList<Integer> getIDList() {
		return this.idList;
	}

	public ArrayList<Path> getPathList() {
		return this.pathList;
	}

	public int getTicks() {
		return this.totalTicks;
	}

	public ArrayList<Func> getPC() {
		return this.pc;
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

	public Client(String path, int totalTicks) throws IOException,
			ParserConfigurationException, SAXException {
		this.totalTicks = totalTicks;
		Parse p = new Parse(path);
		caseTable = p.table;
		getFileList(root + p.getSlnPath());
		agentList = new HashMap<Integer, DefaultBelief>();
		finished = false;
		clk = new ClockTick(this);
		System.out.println("clk in contruction " + clk);
		agentNum = 0;
		idList = new ArrayList<Integer>();
		pathList = new ArrayList<Path>();
		this.caseID = this.hashCode();
		this.control(1, this.getTicks());
	}

	public static void main(String[] args) throws IOException {
		try {
			Client oneCase = new Client(
					root + "USER\\" + args[0] + "\\snr.xml",
					Integer.parseInt(args[1]));
			// "snr.xml", 50);
			System.out.println("client half fini2");
			//System.out.println("lastAssignID:"+lastAssignID);
			for (int i = 0; i < oneCase.caseTable.size(); i++) {
				Tuple oneTuple = oneCase.caseTable.get(i);
				oneTuple.JVM_id = Server.assign();
				oneCase.agentNum++;
			}
			System.out.println("client half fini3");
			synchronized (Server.casesID) {
				Server.casesID.add(oneCase);
			}
			System.out.println("client half fini4");
			synchronized (Server.cases) {
				Server.cases.put(oneCase.caseID, oneCase);
				oneCase.finished = true;
			}
			System.out.println("client half fini5");
			// Scanner input = new Scanner(System.in);
			// while (true) {
			// input.next();
			// Server.servers.get(0).migrate();
			// }
			while (!oneCase.getClock().isFini()) {
				Thread.sleep(1000);
			}
			/* added on March 21 */
			synchronized (Server.cases) {
				Server.cases.remove(oneCase);
			}
			synchronized (Server.casesID) {
				Server.casesID.remove(oneCase);
				Server.finiCaseNumber++;
			}
			/* fini */
			System.out.println("fini");
			oneCase.output(oneCase.getClock().getDuration() + "");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* newly added *//* deleted on Mar 28th by xiaoyaoth
	public void assignAgentsLogically() {
		int mode = 0;
		switch (mode) {
		case 0:
			for (Tuple t : this.caseTable) {
				if (this.tempPath == null)
					this.tempPath = t.path;
				if (this.tempJVM == null)
					this.tempJVM = Server.assign();
				if (this.isPathOK(t))
					t.JVM_id = this.tempJVM.getJVMId();
				else
					t.JVM_id = Server.assign().getJVMId();
			}
		}
	}
	*/

	public boolean isPathOK(Tuple t) {
		return t.path.isLowerPath(this.tempPath)
				&& this.tempJVM.isStillAvailable();
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
		new Thread(clk).start();
	}

	public int getTotal() {
		return agentNum;
	}

	@SuppressWarnings({ "finally", "unchecked" })
	public <T> ArrayList<T> getAgentList(Class<T> targetClass) {
		ArrayList<T> ans = new ArrayList<T>();
		try {
			for (int i = 0; i < idList.size(); i++)
				if (agentList.get(idList.get(i)).getClass().equals(targetClass))
					ans.add((T) agentList.get(idList.get(i)));
		} catch (Exception ex) {
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

	public Map<Integer, DefaultBelief> getAgentList() {
		return this.agentList;
	}

	@Override
	public DefaultBelief getAgent(int id) {
		// TODO Auto-generated method stub
		return this.agentList.get(id);
	}

	public int getCaseID() {
		return this.caseID;
	}

	public void getFileList(String slnPath) {
		File file = new File(slnPath + "\\flc");
		if (file.isDirectory()) {
			String[] strList = file.list();
			for (int i = 0; i < strList.length; i++) {
				String temp = strList[i];
				System.out.println(temp);
				if (!temp.equals(".svn")) {
					int pos = temp.indexOf("_");
					String cName = temp.substring(0, pos);
					temp = temp.substring(pos + 1);
					pos = temp.indexOf("(");
					String fName = temp.substring(0, pos);
					temp = temp.substring(temp.indexOf("^") + 1);
					System.out.println(temp);
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

	public void output(String s) throws IOException {
		File fo = new File("statistics\\" + this.hashCode() + ".txt");
		FileWriter fw = new FileWriter(fo);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(s);
		bw.flush();
		bw.close();
		fw.close();
	}
}
