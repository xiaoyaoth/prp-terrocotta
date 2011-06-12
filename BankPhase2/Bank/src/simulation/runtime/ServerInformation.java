package simulation.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ServerInformation {
	private final static int PORT = 10000;
	// private String ip = "192.168.131.1";
	private int jVM_id;
	private int perf;
	private int eventCount;
	private int agentCount;
	private int agentCountTemp;
	private int agentTotal;
	private double ratio;
	private PerformanceThread perfThread;
	private ArrayList<byte[]> serializedAgents;
	private String ip;

	public ServerInformation(Integer jVM_id) {
		this.jVM_id = jVM_id;
		this.perfThread = new PerformanceThread(this);
		this.serializedAgents = new ArrayList<byte[]>();
		try {
			this.ip = new BufferedReader(new FileReader(new File(
					"config\\ip.txt"))).readLine();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread perfT = new Thread(this.perfThread);
		perfT.setPriority(8);
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

	public synchronized void addAgentCountTemp() {
		this.agentCountTemp++;
	}
	
	public synchronized void decAgentTotal() {
		this.agentTotal--;
	}

	public synchronized void incAgentTotal() {
		this.agentTotal++;
	}
	
	public synchronized void incAgentTotal(int amt){
		this.agentTotal+=amt;
	}
	
	public synchronized int getAgentCountTemp(){
		return this.agentCountTemp;
	}

	public synchronized int getPerf() {
		return this.perf;
	}

	public synchronized double getRatio() {
		if (this.agentTotal > 0)
			this.ratio = (double) this.agentCount / this.agentTotal;
		else
			this.ratio = Integer.MAX_VALUE;
		return this.ratio;
	}	

	public int getAgentTotal() {
		return agentTotal;
	}

	public int getAgentCount() {
		return agentCount;
	}

	public int getEventCount() {
		return eventCount;
	}

	public int getJVM_id() {
		return jVM_id;
	}

	public PerformanceThread getPerfThread() {
		return this.perfThread;
	}
	
	public String getIp(){
		return this.ip;
	}
	
	
	public synchronized void setAgentCountTemp(int count){
		this.agentCountTemp = 0;
	}
	
	public synchronized void setRatio(){
		if (this.agentTotal > 0)
			this.ratio = (double)this.agentCountTemp/(double)this.agentTotal;
		else
			this.ratio = Integer.MAX_VALUE;
	}
	
	public synchronized void setAgentCount(){
		this.agentCount = this.agentCountTemp;
	}
	
	public synchronized void cleanData(){
		this.agentCountTemp = 0;
		this.eventCount = 0;
	}	

	public synchronized void setPerf(int perf) {
		this.perf = perf;
	}
}
