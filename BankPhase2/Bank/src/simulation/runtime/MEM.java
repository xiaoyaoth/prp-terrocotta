package simulation.runtime;

/**
 * MEM
 * 
 * @author Thierry Janaudy
 */

public final class MEM {
	/**
	 * INSTANCE
	 */
	public static final MEM INSTANCE = new MEM();
	
	static {
		System.loadLibrary("CPUUSAGE");
	}
	
	/**
	 * MEM
	 */
	private MEM() {
	}
	
	/**
	 * getMEMUsage
	 * @return
	 */
	public native int getMEMUsage();
	
	/**
	 * main
	 * @param _
	 */
	public static void main(String _[]) throws Exception {
		while(true) {
			Thread.sleep(1000L);
			System.out.println("Current available MEM: " + MEM.INSTANCE.getMEMUsage());
		}
	}

}
