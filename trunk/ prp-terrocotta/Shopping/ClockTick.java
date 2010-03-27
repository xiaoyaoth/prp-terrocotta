public class ClockTick implements Runnable
{
//	private static int tick, left;
//	private static int now;
//	public static boolean goOn;
//	private static Object tickLock = new Object();
//	public static Object nowLock = new Object();
//	ServerMachine one;
	
	private int tick, left;
	private int now;
	public boolean goOn;
	private Object tickLock = new Object();
	public Object nowLock = new Object();
	
	public ServerMachine one;
    
	public ClockTick(ServerMachine one)
	{
		tick = 0;
		left = 0;
		goOn = false;
		this.one = one;
	}

	public void incLeft(int incr)
	{
		left += incr;
	}

	public int getTick()
	{
		return tick;
	}

	public void run()
	{
		goOn = true;
		while (tick < left)
		{
			while (goOn && tick < left)
			{
				try {
					Thread.sleep(2);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				synchronized (tickLock)
				{
					try {
						while (now > 0) tickLock.wait();
					}
					catch (Exception e) {}
				}
				synchronized (nowLock)
				{
					one.gui.lb2.setText("Tick£º" + ++tick);
					now = one.caseTable.size();
					System.out.println("now"+now);
					nowLock.notifyAll();
				}
			}
		}
		goOn = false;
		one.gui.startBtn.setLabel("¿ªÊ¼·ÂÕæ");
	}

	public int getNow()
	{
		synchronized (nowLock) { return now; }
	}

	public void decNow()
	{
		synchronized (nowLock) { now--; }
		if (now <= 0) synchronized (tickLock) {	tickLock.notifyAll(); }
	}
}