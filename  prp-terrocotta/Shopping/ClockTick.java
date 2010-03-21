public class ClockTick implements Runnable
{
	private static int tick, left, now;
	public static boolean goOn;
	private static Object tickLock = new Object();
	public static Object nowLock = new Object();
    
	public ClockTick()
	{
		tick = 0;
		left = 0;
		goOn = false;
	}

	public static void incLeft(int incr)
	{
		left += incr;
	}

	public static int getTick()
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
				synchronized (tickLock)
				{
					try {
						while (now > 0) tickLock.wait();
					}
					catch (Exception e) {}
				}
				synchronized (nowLock)
				{
					GUI.lb2.setText("Tick£º" + ++tick);
					now = Agent.agentList.size();
					nowLock.notifyAll();
				}
			}
		}
		goOn = false;
		GUI.startBtn.setLabel("¿ªÊ¼·ÂÕæ");
	}

	public static int getNow()
	{
		synchronized (nowLock) { return now; }
	}

	public static void decNow()
	{
		synchronized (nowLock) { now--; }
		if (now <= 0) synchronized (tickLock) {	tickLock.notifyAll(); }
	}
}