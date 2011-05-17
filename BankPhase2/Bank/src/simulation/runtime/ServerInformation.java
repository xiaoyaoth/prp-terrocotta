package simulation.runtime;


public class ServerInformation {
	private final static int PORT = 10000;
	private String ip = "192.168.131.1";
	private int jVM_id;
	private int perf;
	private int eventCount;
	private int agentCount;
	private int agentTotal;
	private double ratio;
	private PerformanceThread perfThread;
	
	public ServerInformation(Integer jVM_id){
		this.jVM_id = jVM_id;
		this.perfThread = new PerformanceThread(this);
		Thread perfT = new Thread(this.perfThread);
		perfT.setName("PerformanceThreaddd");
		perfT.start();
	}

	public String getIp() {
		return this.ip;
	}

	public synchronized void addEventCount() {
		this.eventCount++;
	}

	public synchronized void addAgentCount() {
		this.agentCount++;
	}

	public synchronized void decAgentTotal() {
		//System.out.println("decAgentTotal Called");
		this.agentTotal--;
	}
	
	public synchronized void incAgentTotal() {
		//System.out.println("incAgentTotal Called");
		this.agentTotal++;
	}

	public synchronized int getPerf() {
		return this.perf;
	}

	public synchronized double getRatio() {
		return this.ratio;
	}

	public synchronized void setIp(String ip) {
		this.ip = ip;
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
	
	public PerformanceThread getPerfThread(){
		return this.perfThread;
	}
}
