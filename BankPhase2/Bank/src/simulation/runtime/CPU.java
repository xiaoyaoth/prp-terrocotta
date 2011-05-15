package simulation.runtime;

/**
 * CPU
 * 
 * @author Thierry Janaudy
 */
public final class CPU {
	/**
	 * INSTANCE
	 */
	public static final CPU INSTANCE = new CPU();
	
	static {
		System.loadLibrary("CPUUSAGE");
	}
	
	/**
	 * CPU
	 */
	private CPU() {
	}
	
	/**
	 * getCpuUsage
	 * @return
	 */
	public native int getCpuUsage();
	
	/**
	 * main
	 * @param _
	 */
	public static void main(String _[]) throws Exception {
		while(true) {
			Thread.sleep(1000L);
			System.out.println("Current CPU Usage: " + CPU.INSTANCE.getCpuUsage());
		}
	}
}
