package simulation.modeling;

public class ClockTick implements Runnable
{
	private int tick, left, now;
	public boolean goOn;
	private Object tickLock = new Object();
	public Object nowLock = new Object();
	public Object tcLock = new Object();
	private MainInterface main;
    
	public ClockTick(MainInterface main)
	{
		tick = 0;
		left = 0;
		goOn = false;
		this.main = main;
	}

	public void incLeft(int incr)
	{
		synchronized(tcLock){
		this.left += incr;}
	}

	public int getTick()
	{
		return this.tick;
	}

	public void run()
	{
		synchronized(tcLock){
		this.goOn = true;}
		while (this.tick < this.left)
		{
			while (this.goOn && this.tick < this.left)
			{
				synchronized (this.tickLock)
				{
					try {
						while (this.now > 0) this.tickLock.wait();
					}
					catch (Exception e) {}
				}
				synchronized (nowLock)
				{
					++this.tick;
					System.out.println("\nTick£º" + tick);
					this.now = this.main.getTotal();
					this.nowLock.notifyAll();
				}
			}
		}
		synchronized(tcLock){
		goOn = false;		}
	}

	public int getNow()
	{
		synchronized (this.nowLock) { return now; }
	}

	public void decNow()
	{
		synchronized (this.nowLock) { now--; }
		if (this.now <= 0) synchronized (this.tickLock) 
		{	this.tickLock.notifyAll(); }
	}
}