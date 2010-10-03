package simulation.modeling;

import java.io.Serializable;

public class ClockTick implements Runnable, Serializable {
	private int tick, left, now;
	private boolean goOn;
	private Lock tickLock = new Lock();
	private Lock nowLock = new Lock();
	private Lock tcLock = new Lock();
	private transient MainInterface main;
	
	public void setMain(MainInterface main){
		this.main = main;
	}

	public Object getTickLock() {
		return this.tickLock;
	}

	public Object getNowLock() {
		return this.nowLock;
	}

	public void setGoOn(boolean goOn) {
		this.goOn = goOn;
	}
	
	public boolean isGoOn(){
		return this.goOn;
	}

	public ClockTick(MainInterface main) {
		tick = 0;
		left = 0;
		goOn = false;
		this.main = main;
	}

	public void incLeft(int incr) {
		synchronized (tcLock) {
			this.left += incr;
		}
	}

	public int getTick() {
		return this.tick;
	}

	public void run() {
		synchronized (tcLock) {
			this.goOn = true;
		}
		while (this.tick < this.left) {
			while (this.goOn && this.tick < this.left) {
				synchronized (this.tickLock) {
					try {
						while (this.now > 0)
							this.tickLock.wait();
					} catch (Exception e) {
					}
				}
				synchronized (nowLock) {
					++this.tick;
				}
				//System.out.println("\nTick£º" + tick);
				synchronized (nowLock) {
					this.now = this.main.getTotal();
					this.nowLock.notifyAll();
				}
			}
		}
		synchronized (tcLock) {
			goOn = false;
		}
	}

	public int getNow() {
		synchronized (this.nowLock) {
			return now;
		}
	}

	public void decNow() {
		synchronized (this.nowLock) {
//			System.out.println(--now);
			--now;
		}
		if (this.now <= 0)
			synchronized (this.tickLock) {
				this.tickLock.notifyAll();
			}
	}
}
