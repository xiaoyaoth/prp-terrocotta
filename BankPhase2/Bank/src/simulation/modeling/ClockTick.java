package simulation.modeling;

import java.io.Serializable;
import java.util.Date;

public class ClockTick implements Runnable, Serializable {
	private int tick, left, now;
	private boolean goOn;
	private Lock tickLock = new Lock();
	private Lock nowLock = new Lock();
	private Lock tcLock = new Lock();
	private MainInterface main;

	private String duration;

	public Object getTickLock() {
		return this.tickLock;
	}

	public Object getTcLock() {
		return this.tcLock;
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
			System.out.println("in ClockTick.java now is " + this.now);
		}
		while (this.tick < this.left) {
			// System.out.print("this.tick<this.left");
			while (this.goOn && this.tick < this.left) {
				synchronized (nowLock) {
					++this.tick;
				}
				System.out.println(this.main.getCaseID()+" Tick " + tick + " :" + this.now);
				synchronized (this.tickLock) {
					synchronized (nowLock) {
						this.now = this.main.getTotal();
						this.nowLock.notifyAll();
					}

					try {
						System.out.print("A ");
						while (this.now > 0) {
							//System.out.println("tickLock locked");
							this.tickLock.wait();
							//System.out.println("tickLock released");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		synchronized (tcLock) {
			while (this.now > 0)
				try {
					this.tcLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			goOn = false;
			Date time = new Date();
			end = time.getTime();
			this.duration = "start:" + start + " end:" + end + " duration:"
					+ (end - start);
			/*
			 * 这里不是fini,当clocktick把所有的agent唤醒，clocktick结束了，但是Agent还在运行。
			 * 所以当所有Agent结束运行才是一切的终结，想办法弄一下
			 */
			this.tcLock.notify();
			System.out.println("ClockTickFini in ClockTick.java");
			this.main = null;
		}
	}

	public int getNow() {
		synchronized (this.nowLock) {
			return now;
		}
	}

	public void decNow() {
		synchronized (this.tickLock) {
			//System.out.print(now + " ");
			--now;
			if (this.now <= 0) {
				this.tickLock.notifyAll();
				//System.out.println("B ");
			}
		}
	}

	public String getDuration() {
		return this.duration;
	}

	public void setMain(MainInterface main) {
		this.main = main;
	}

	public static void main(String[] args) {
		ClockTick clk = new ClockTick(null);
		new Thread(clk).start();
	}
}
