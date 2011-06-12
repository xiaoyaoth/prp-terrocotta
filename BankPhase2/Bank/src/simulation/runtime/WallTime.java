package simulation.runtime;

public class WallTime {
	private static WallTime INSTANCE;
	
	private WallTime(){
	}
	
	public long getTime(){
		return new java.util.Date().getTime();
	}
	
	public static WallTime getInstance(){
		if(INSTANCE==null)
			INSTANCE=new WallTime();
		return INSTANCE;
	}
	public static void main(String[] args) throws InterruptedException{
		System.out.println(WallTime.getInstance().getTime());
		Thread.sleep(1000);
		System.out.println(WallTime.getInstance().getTime());
		Thread.sleep(1000);
		System.out.println(WallTime.getInstance().getTime());
		Thread.sleep(1000);
		System.out.println(WallTime.getInstance().getTime());
		Thread.sleep(1000);
		System.out.println(WallTime.getInstance().getTime());
		
	}
}
