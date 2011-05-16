package simulation.runtime;

import java.io.File;
import java.util.Iterator;

import simulation.modeling.Lock;

public class PerformanceThread implements Runnable {
	private int machineAbility;
	private int loopCount;
	private int weakPoint;
	private int jVM_id;
	private Lock tcLock;
	private ServerInformation sInfo;

	public PerformanceThread(ServerInformation sInfo) {
		this.sInfo = sInfo;
		this.jVM_id = sInfo.getJVM_id();
		this.loopCount = 0;
		this.tcLock = new Lock();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			synchronized (this.tcLock) {
				if (this.sInfo.getAgentTotal() > 0)
					this.sInfo.setRatio((double) this.sInfo.getAgentCount()
							/ (double) this.sInfo.getAgentTotal());
				else
					this.sInfo.setRatio(Integer.MAX_VALUE);
				System.out.println(" LoopCount:" + this.loopCount++
						+ " EventCount:" + this.sInfo.getEventCount()
						+ " AgentCount:" + this.sInfo.getAgentCount()
						+ " AgentTotal:" + this.sInfo.getAgentTotal()
						+ " ratio:" + this.sInfo.getRatio() + " weak:"
						+ this.weakPoint+" "+Server.serverInfo);
				if (this.sInfo.getAgentTotal() > 0 && Server.serverInfo.size()>1) {
					if (this.sInfo.getRatio() < 0.5)
						weakPoint++;
					else
						weakPoint = 0;
				} else
					weakPoint = 0;
				this.pickOneSnrToBeMigrated();
				this.sInfo.setEventCount(0);
				this.sInfo.setAgentCount(0);
			}
			synchronized (Server.serverInfo) {
				this.sInfo.setPerf(this.machineAbility);
				Server.serverInfo.put(this.sInfo.getjVM_id(), sInfo);
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void pickOneSnrToBeMigrated() {
		Iterator<Scenario> iter = ScenariosMgr.getSnrs().values().iterator();
		Scenario c = null;
		int tickTemp = 0;
		if (this.weakPoint == 3) {
			while (iter.hasNext()) {
				Scenario snr = iter.next();
				int tickRemained = snr.getTicks() - snr.getClock().getTick();
				if (tickRemained > tickTemp && this.jVM_id == snr.getHostID()) {
					tickTemp = tickRemained;
					c = snr;
				}
			}
			if (c != null) {
				c.setMigrate(this.jVM_id);
				try {
//					MigrateFiniMonitor mfm = new MigrateFiniMonitor(this);
//					Thread mfmt = new Thread(mfm);
//					mfmt.start();
					this.tcLock.wait(1000);
					File file = new File("agentsOut");
					int count;
					do{	
						count = 0;
						File[] flist = file.listFiles();
						for (File f : flist) {
							if (!f.isDirectory())
								count++;
						}
						Thread.sleep(5000);
					}while(count>0);
//					System.out
//							.println("perfThread resumed, migrateFiniMonitor terminated");
//					mfmt = null;
//					mfm = null;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println("Client is null");
			}
			this.weakPoint = 0;
		}
	}

	public void notifyTcLock() {
		synchronized (this.tcLock) {
			this.tcLock.notify();
		}
	}

	private class MigrateFiniMonitor implements Runnable {
		PerformanceThread perfThread;
		private static final String FOLDER_TO_BE_CHECKED = "agentsOut";
		private int confirmClean;
		private Lock tcLock;

		public MigrateFiniMonitor(PerformanceThread perfThread) {
			this.perfThread = perfThread;
			this.tcLock = new Lock();
			System.out.println("PerfThread is waiting, MonitorThread start");
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			File file = new File(FOLDER_TO_BE_CHECKED);
			while (this.confirmClean < 3) {
				System.out.println(this.confirmClean);
				int count = 0;
				File[] flist = file.listFiles();
				for (File f : flist) {
					if (!f.isDirectory())
						count++;
				}
				if (count == 0)
					synchronized (this.tcLock) {
						confirmClean++;
					}
				else
					this.confirmClean = 0;
				count = 0;
				if (this.confirmClean == 3) {
					this.perfThread.notifyTcLock();
					try {
						this.finalize();
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}