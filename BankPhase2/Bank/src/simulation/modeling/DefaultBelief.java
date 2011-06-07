package simulation.modeling;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import simulation.runtime.Server;

public class DefaultBelief extends PlanManager implements Runnable,
		Serializable {
	/* Global Const */
	// private final static String AGENTS_OUT_FILE_FOLDER = "agentsOut//";
	// private final static int PORT = 10000;

	private int pCounter = 0;
	private ArrayList<PlanCondition> pc;
	private int id, tick = 0, lifeCycle = -1, ownTick = 0;

	private boolean migrate = false;
	private int caseID;
	private ArrayList<MessageInfo> sndMessageBox;
	private ArrayList<MessageInfo> rcvMessageBox;
	private ArrayList<Integer> connectIDs;
	private Path path;
	private Integer hostServerID;
	private String debugMessage;

	transient private boolean nextTick = true;
	transient protected MainInterface main;
	transient private Lock tcLock = new Lock();

	public DefaultBelief() {
		this.setSub(this);
		this.pc = new ArrayList<PlanCondition>();
		this.sndMessageBox = new ArrayList<MessageInfo>();
		this.rcvMessageBox = new ArrayList<MessageInfo>();
		this.connectIDs = new ArrayList<Integer>();
	}

	public void printDebugMessage() {
		System.out.println(this.id);
		System.out.println("nextTick:" + this.nextTick);
		System.out.println("migrate:" + this.migrate);
		System.out.println("debugMessage:" + this.debugMessage);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void notifyTcLock() {
		synchronized (this.tcLock) {
			this.tcLock.notify();
		}
	}

	public synchronized void recover(MainInterface main) {
		this.tcLock = new Lock();
		this.main = main;
		this.setNextTick();
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
		while (this.nextTick && (this.getLifeCycle() == -1 || this.isNoLife())) {
			try {
				synchronized (this.main.getClock().getNowLock()) {
					this.debugMessage = "1";
					this.lifeCycle--;
					while (this.getTick() >= this.main.getClock().getTick()
							|| this.main.getClock().getNow() == 0)
						this.main.getClock().getNowLock().wait();
					this.debugMessage = "2";
				}
				synchronized (this.tcLock) {
					this.addTick();
					this.createPlans();
					this.submitPlans();
					if (this.migrate) {
						// System.out.print(this.id + "migrate ");
						this.nextTick = false;
					}
					this.main.getClock().decNow();
					Server.serverInfo.get(this.hostServerID).addAgentCount();
					this.debugMessage = "3";
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out
						.println("*************DefaultBelief.java****************");
				System.out
						.println("in DefaultBelief.java:\n main:" + this.main);
				System.out.println(" clk:" + this.main.getClock());
				System.out.println(" nowLock:"
						+ this.main.getClock().getNowLock());
				System.out.println(" tick:" + this.tick + " left:"
						+ this.getMain().getClock().getTick());
				System.out.println(this.rcvMessageBox);
				System.out
						.println("*************DefaultBelief.java****************");
			}
		}
		/* added on May 2nd by xiaoyaoth */
		synchronized (this.tcLock) {
			Server.serverInfo.get(this.hostServerID).decAgentTotal();
			if (!this.migrate) {
				// System.out.print("ag:" + this.id +
				// "fini in DefaultBelief.java");
				this.debugMessage = "4";
				this.clean(this.pc);
				this.clean(this.sndMessageBox);
				this.clean(this.rcvMessageBox);
				this.clean(this.connectIDs);
				this.path.clear();
				this.path = null;
				this.main = null;
				this.cleanPlans();
				this.debugMessage = "5";
			} else
				try {
					this.debugMessage = "6";
					this.setNextTick();
					this.debugMessage = "NTtrue,waiting";
					this.tcLock.wait();
					this.debugMessage = "NTfalse, running";
					this.printDebugMessage();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		// this.finalize();
		// System.out.println("ag:" + this.id +
		// "finalize in DefaultBelief.java");
		/* added fini */
	}

	public void setMigrate(boolean migrate) {
		synchronized (tcLock) {
			this.migrate = migrate;
		}
	}

//	public void migrate() throws IOException {
//		synchronized (this.tcLock) {
//			this.nextTick = false;
//		}
//	}

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
		return this.lifeCycle > 0;
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
		if (flag && this.sndMessageBox != null)
			synchronized (this.sndMessageBox) {
				this.sndMessageBox.add(mi);
			}
		else if (this.rcvMessageBox != null)
			synchronized (this.rcvMessageBox) {
				this.rcvMessageBox.add(mi);
			}
		else
			return;
	}

	// public void removeMess(boolean flag, int index) {
	// if (flag && this.sndMessageBox != null)
	// this.sndMessageBox.remove(index);
	// else if (this.rcvMessageBox != null)
	// this.rcvMessageBox.remove(index);
	// else
	// return;
	// }

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
		synchronized (this.rcvMessageBox) {
			while (this.rcvMessageBox.size() > 0) {
				MessageInfo mi = this.rcvMessageBox.remove(0);
				if (mi == null)
					System.out.println("in DefaultBelief.java, mi is null");
				if (!mi.getRFlag()) {
					/* edited by Xiaosong */
					/* edited fini */
					mi.setRFlag();
					String temp = mi.getContent();
					/* edited on May 2nd */
					mi = null;
					/* edited fini */
					/*
					 * edited by Xiaosong if(temp.endsWith("aaaaaaaaaaaaaaaa"))
					 * System.out.print("1"); edited fini
					 */
					int kh = temp.indexOf("(");
					if (kh > -1) {
						String pn = temp.substring(0, kh);
						int pIndex = getPCIndex(pn);
						if (pIndex > -1) {
							PlanCondition tpc = pc.get(pIndex);
							PlanInstance pi = new PlanInstance(this.getID(),
									tpc.getID(), tpc.getNeedTicks(), pn);
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
	}

	private void sendMessages() {
		// for (int i = 0; i < this.sndMessageBox.size(); i++) {
		// MessageInfo mi = this.sndMessageBox.get(i);

		// edited by xiaoyaoth
		synchronized (this.sndMessageBox) {
			while (this.sndMessageBox.size() > 0) {
				MessageInfo mi = this.sndMessageBox.remove(0);
				if (!mi.getSFlag()) {
					mi.setSFlag();
					this.main.getAgent(mi.getRcv()).addMess(false, mi);
				}
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
	}

	/* End of Default Action */

	public void setCaseID(int caseID) {
		this.caseID = caseID;
	}

	public int getCaseID() {
		return caseID;
	}

	/* edited by Xiaosong */

	public synchronized void setHostServerID(int hostServerID) {
		this.hostServerID = hostServerID;
	}

	public int getHostServerID() {
		return this.hostServerID;
	}

	public boolean isNextTick() {
		return this.nextTick;
	}

	public boolean isMigrate() {
		return this.migrate;
	}

	public synchronized void setNextTick() {
		this.nextTick = true;
		this.migrate = false;
	}

	public <T> void clean(ArrayList<T> list) {
		for (int i = 0; i < list.size(); i++) {
			@SuppressWarnings("unused")
			T t = list.remove(i);
			t = null;
		}
		list.clear();
		list = null;
	}

	/* edit fini */
}