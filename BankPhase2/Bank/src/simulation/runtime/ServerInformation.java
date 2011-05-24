package simulation.runtime;

import java.io.IOException;
import java.util.ArrayList;

public class ServerInformation {
	private final static int PORT = 10000;
	//private String ip = "192.168.131.1";
	private int jVM_id;
	private int perf;
	private int eventCount;
	private int agentCount;
	private int agentTotal;
	private double ratio;
	private PerformanceThread perfThread;
	private ArrayList<byte[]> serializedAgents;

	public ServerInformation(Integer jVM_id) {
		this.jVM_id = jVM_id;
		this.perfThread = new PerformanceThread(this);
		this.serializedAgents = new ArrayList<byte[]>();
		Thread perfT = new Thread(this.perfThread);
		perfT.setName("PerformanceThreaddd");
		perfT.start();
	}

	public synchronized void addMigingAgentsInList(byte[] bs) {
		this.serializedAgents.add(bs);
	}

	public synchronized byte[] getMigAgents() {
		if (this.serializedAgents.size() > 0)
			return this.serializedAgents.remove(0);
		return null;
	}

	public synchronized void addEventCount() {
		this.eventCount++;
	}

	public synchronized void addAgentCount() {
		this.agentCount++;
	}

	public synchronized void decAgentTotal() {
		try {
			java.io.FileWriter fw = new java.io.FileWriter(new java.io.File(
					"statistics\\sinfo"+this.jVM_id+".txt"),true);
			java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
			bw.append("decAgentTotal Called\t" + (this.agentTotal - 1)+"\tn\n");
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.agentTotal--;
	}

	public synchronized void incAgentTotal() {
		try {
			java.io.FileWriter fw = new java.io.FileWriter(new java.io.File(
					"statistics\\sinfo"+this.jVM_id+".txt"), true);
			java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
			bw.append("incAgentTotal Called\t" + (this.agentTotal + 1)+"\tp\n\r");
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.agentTotal++;
	}

	public synchronized int getPerf() {
		return this.perf;
	}

	public synchronized double getRatio() {
		if (this.agentTotal > 0)
			this.ratio = (double)this.agentCount / this.agentTotal;
		else
			this.ratio = Integer.MAX_VALUE;
		return this.ratio;
	}

	public int getAgentTotal() {
		return agentTotal;
	}

	public synchronized void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public synchronized void setAgentCount(int agentCount) {
		this.agentCount = agentCount;
	}

	public int getAgentCount() {
		return agentCount;
	}

	public synchronized void setEventCount(int eventCount) {
		this.eventCount = eventCount;
	}

	public int getEventCount() {
		return eventCount;
	}

	public synchronized void setPerf(int perf) {
		this.perf = perf;
	}

	public synchronized void setJVM_id(int jVM_id) {
		this.jVM_id = jVM_id;
	}

	public int getJVM_id() {
		return jVM_id;
	}

	public PerformanceThread getPerfThread() {
		return this.perfThread;
	}
}
