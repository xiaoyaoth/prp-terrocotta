package simulation.modeling;

import java.io.Serializable;
import java.util.Date;

import simulation.runtime.Server;

public class ClockTick implements Runnable, Serializable {
	private int tick, left, now;
	private boolean goOn;
	private Lock tickLock = new Lock();
	private Lock nowLock = new Lock();
	private Lock tcLock = new Lock();
	private MainInterface main;

	private long duration;
	private boolean fini;

	public Object getTickLock() {
		return this.tickLock;
	}

	public Object getNowLock() {
		return this.nowLock;
	}

	public void setGoOn(boolean goOn) {
		this.goOn = goOn;
	}

	public boolean isGoOn() {
		return this.goOn;
	}

	public ClockTick(MainInterface main) {
		tick = 0;
		left = 0;
		goOn = false;
		fini = false;
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
		long start, end;
		synchronized (tcLock) {
			this.goOn = true;
			Date time = new Date();
			start = time.getTime();
			System.out.println(start);
		}
		while (this.tick < this.left) {
			System.out.print("this.tick<this.left");
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
				System.out.println("\n Tick " + tick + " :" + this.now);
				synchronized (nowLock) {
					this.now = this.main.getTotal();
					this.nowLock.notifyAll();
				}
				// try {
				// Thread.sleep(500);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
		}
		while(this.now != 0);
		synchronized (tcLock) {
			goOn = false;
			Date time = new Date();
			end = time.getTime();
			System.out.println(start);
			System.out.println(end);
			System.out.print("time consumed ");
			System.out.println(end - start);
			this.duration = end - start;
			/*
			 * 这里不是fini,当clocktick把所有的agent唤醒，clocktick结束了，但是Agent还在运行。
			 * 所以当所有Agent结束运行才是一切的终结，想办法弄一下
			 */
			fini = true;
			this.main = null;
		}
	}

	public int getNow() {
		synchronized (this.nowLock) {
			return now;
		}
	}

	public void decNow() {
		synchronized (this.nowLock) {
			//System.out.print(now + " ");
			--now;
		}
		if (this.now <= 0)
			synchronized (this.tickLock) {
				this.tickLock.notifyAll();
			}
	}

	public long getDuration() {
		return this.duration;
	}

	public boolean isFini() {
		return this.fini;
	}

	public void setMain(MainInterface main) {
		this.main = main;
	}
}
