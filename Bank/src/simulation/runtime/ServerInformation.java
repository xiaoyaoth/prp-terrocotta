package simulation.runtime;

public class ServerInformation {
	private final static int PORT = 10000;
	private String ip = "192.168.131.1";
	private int JVM_id;
	private int perf;
	private int eventCount;
	private int agentCount;
	private int agentTotal;
	private double ratio;

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
		// System.out.println("decAgentTotal Called");
		this.agentTotal--;
	}
	
	public synchronized void incAgentTotal() {
		// System.out.println("decAgentTotal Called");
		this.agentTotal++;
	}

	public synchronized int getPerf() {
		return this.perf;
	}

	public synchronized double getRatio() {
		return this.ratio;
	}

	public void setJVM_id(int jVM_id) {
		JVM_id = jVM_id;
	}

	public int getJVM_id() {
		return JVM_id;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setAgentTotal(int agentTotal) {
		this.agentTotal = agentTotal;
	}

	public int getAgentTotal() {
		return agentTotal;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public void setAgentCount(int agentCount) {
		this.agentCount = agentCount;
	}

	public int getAgentCount() {
		return agentCount;
	}

	public void setEventCount(int eventCount) {
		this.eventCount = eventCount;
	}

	public int getEventCount() {
		return eventCount;
	}

	public void setPerf(int perf) {
		this.perf = perf;
	}
}
