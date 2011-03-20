package simulation.modeling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import simulation.runtime.SendFile;
import simulation.runtime.Server;

public class DefaultBelief extends PlanManager implements Runnable,
		Serializable {
	/* Global Const */
	private final static String AGENTS_OUT_FILE_FOLDER = "agentsOut//";
	private final static int PORT = 10000;

	private int pCounter = 0;
	private ArrayList<PlanCondition> pc = new ArrayList<PlanCondition>();
	private int id, tick = 0, lifeCycle = -1, ownTick = 0;
	
	private String ip;
	private Map<String,Integer>ipCount = new HashMap<String, Integer>();

	private boolean migrate = false;
	private boolean nextTick = true;

	protected transient MainInterface main;
	private int caseID;
	private ArrayList<MessageInfo> sndMessageBox = new ArrayList<MessageInfo>();
	private ArrayList<MessageInfo> rcvMessageBox = new ArrayList<MessageInfo>();
	private ArrayList<Integer> connectIDs = new ArrayList<Integer>();
	private Path path;
	private Lock tcLock = new Lock();

	public DefaultBelief() {
		this.setSub(this);
	}

	/* 自带Action */
	public void addPC(PlanCondition newPC) {
		newPC.setID(++pCounter);
		pc.add(newPC);
	}

	public int getPCIndex(int pcID) {
		for (int i = 0; i < pc.size(); i++)
			if (pc.get(i).getID() == pcID)
				return i;
		return -1;
	}

	public int getPCIndex(String pn) {
		for (int i = 0; i < pc.size(); i++)
			if (pc.get(i).getPlanName().equals(pn))
				return i;
		return -1;
	}

	public PlanCondition getPC(int index) {
		return pc.get(index);
	}

	/* 自带Action */

	/* Default Action */
	public void run() {
		while ((this.getLifeCycle() == -1 || this.isNoLife()) && this.nextTick) {
			synchronized (this.main.getClock().getNowLock()) {
				try {
					while (this.getTick() >= this.main.getClock().getTick()
							|| this.main.getClock().getNow() == 0)
						this.main.getClock().getNowLock().wait();
					this.addTick();
					this.createPlans();
					this.submitPlans();

					//System.out.println(this.id+" "+this.ipCount);
					if (this.migrate) {
						this.migrate();
					}

					this.main.getClock().decNow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void setMain(MainInterface main) {
		synchronized (tcLock) {
			this.caseID = main.getCaseID();
			this.main = main;
		}
	}

	public MainInterface getMain() {
		return this.main;
	}

	public void setPath(Path p) {
		this.path = p;
	}

	public Path getPath() {
		return this.path;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getID() {
		return this.id;
	}
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public String getIp(){
		return this.ip;
	}

	public int getOwnTick() {
		return this.ownTick;
	}

	public void setLifeCycle(int lifeCycle) {
		this.lifeCycle = lifeCycle;
	}

	public int getLifeCycle() {
		return this.lifeCycle;
	}

	public boolean isNoLife() {
		return --this.lifeCycle >= 0;
	}

	public void addTick() {
		this.tick++;
		this.ownTick++;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public int getTick() {
		return this.tick;
	}

	public void addMess(boolean flag, MessageInfo mi) {
		if (flag)
			this.sndMessageBox.add(mi);
		else
			this.rcvMessageBox.add(mi);
	}

	public void removeMess(boolean flag, int index) {
		if (flag)
			this.sndMessageBox.remove(index);
		else
			this.rcvMessageBox.remove(index);
	}

	protected void createPlans() {
		this.receiveMessages();
		this.sendMessages();
		for (int i = 0; i < pc.size(); i++) {
			PlanCondition tpc = pc.get(i);
			if (tpc.getInterval() > 0
					&& this.getOwnTick() % tpc.getInterval() == 0) {
				PlanInstance pi = new PlanInstance(this.getID(), tpc.getID(),
						tpc.getNeedTicks(), tpc.getPlanName());
				pi.setSize(0);
				this.addPlanInstance(pi);
			}
		}
	}

	private void receiveMessages() {
		for (int i = 0; i < this.rcvMessageBox.size(); i++) {
			MessageInfo mi = this.rcvMessageBox.get(i);
			if (!mi.getRFlag()) {
				/* edited by Xiaosong */
				this.addIpCount(mi.getIp());
				/* edited fini*/				
				mi.setRFlag();
				String temp = mi.getContent();
				/* edited by Xiaosong
				if(temp.endsWith("aaaaaaaaaaaaaaaa"))
					System.out.print("1");
				edited fini*/
				int kh = temp.indexOf("(");
				if (kh > -1) {
					String pn = temp.substring(0, kh);
					int pIndex = getPCIndex(pn);
					if (pIndex > -1) {
						PlanCondition tpc = pc.get(pIndex);
						PlanInstance pi = new PlanInstance(this.getID(), tpc
								.getID(), tpc.getNeedTicks(), pn);
						temp = temp.substring(1 + kh);
						int dh = temp.indexOf(", "), yh = temp.indexOf(")");
						if (yh != kh + 1) {
							ArrayList<String> al = new ArrayList<String>();
							while (dh > -1) {
								al.add(temp.substring(0, dh));
								temp = temp.substring(dh + 2);
								dh = temp.indexOf(", ");
							}
							al.add(temp.substring(0, temp.indexOf(")")));
							pi.setSize(al.size());
							for (int j = 0; j < al.size(); j++)
								pi.setPara(j, Integer.parseInt(al.get(j)));
						} else
							pi.setSize(0);
						this.addPlanInstance(pi);
					}
				}
			}
		}
	}

	private void sendMessages() {
		for (int i = 0; i < this.sndMessageBox.size(); i++) {
			MessageInfo mi = this.sndMessageBox.get(i);
			if (!mi.getSFlag()) {
				mi.setSFlag();
				this.main.getAgent(mi.getRcv()).addMess(false, mi);
			}
		}
	}

	public void addConn(int id) {
		this.connectIDs.add(id);
	}

	public void removeConn(int index) {
		this.connectIDs.remove(index);
	}

	public int getConn(int index) {
		return this.connectIDs.get(index);
	}

	public int getConnIndex(int id) {
		for (int i = 0; i < this.connectIDs.size(); i++)
			if (this.connectIDs.get(i) == id)
				return i;
		return -1;
	}

	public String toString() {
		return "Agent" + this.id;
		// return this.pCounter + " " + this.pc + " " + this.id + " " +
		// this.tick
		// + " " + this.lifeCycle + " " + this.ownTick + " " + this.migrate
		// + " " + this.main + " " + this.caseID + " "
		// + this.sndMessageBox + " " + this.rcvMessageBox + " "
		// + this.connectIDs;
	}

	/* End of Default Action */

	// This function violate everything I learn from the principle of SW design.
	public void migrate() throws IOException {
		File mig = new File(AGENTS_OUT_FILE_FOLDER + this.id + "rr" + System.currentTimeMillis());
		FileOutputStream fout = new FileOutputStream(mig);
		ObjectOutputStream objout = new ObjectOutputStream(fout);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			objout.writeObject(this);
			objout.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Server res;
		if ((res = Server.assign()) != null)
			new SendFile(res.getIp(), PORT, mig).start();
		synchronized (this.tcLock) {
			this.nextTick = false;
		}
		objout.close();
		fout.close();
	}

	public void setMigrate(boolean migrate) {
		synchronized (tcLock) {
			this.migrate = migrate;
		}
	}

	public void setCaseID(int caseID) {
		this.caseID = caseID;
	}

	public int getCaseID() {
		return caseID;
	}
	
	/*edited by Xiaosong*/
	private void addIpCount(String ip){
		if(this.ipCount.containsKey(ip)){
			int count = this.ipCount.get(ip);
			this.ipCount.put(ip, count+1);
		}else
			this.ipCount.put(ip, 1);
	}
	
	private String getMaxIp(){
		int tempCount = 0;
		String resIp = "127.0.0.1";
		for(String s:this.ipCount.keySet()){
			int count = this.ipCount.get(s);
			if(count > tempCount)
				resIp = s;
		}
		return resIp;
	}
	/*edit fini*/
}