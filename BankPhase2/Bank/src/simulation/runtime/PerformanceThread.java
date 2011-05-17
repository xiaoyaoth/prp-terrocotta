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
	private static double threshold;

	public PerformanceThread(ServerInformation sInfo) {
		this.sInfo = sInfo;
		this.jVM_id = sInfo.getJVM_id();
		this.loopCount = 0;
		this.tcLock = new Lock();
		threshold = 5;
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
					if (this.sInfo.getRatio() < threshold && this.sInfo.getRatio() != 0)
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
				Server.serverInfo.put(this.sInfo.getJVM_id(), sInfo);
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
					this.tcLock.wait();
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
	
	public static double getThreshold(){
		return threshold;
	}
}