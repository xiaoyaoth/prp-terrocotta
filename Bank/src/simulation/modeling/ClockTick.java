package simulation.modeling;

import java.io.Serializable;
import java.util.Date;

public class ClockTick implements Runnable, Serializable {
	private int tick, left, now;
	private boolean goOn;

	// setting hold to false means to let the flow pass through it

	private boolean holdDecNow;
	private boolean holdIncNow;
	private boolean holdAddTick;
	private Lock tcLock = new Lock();
	private MainInterface main;

	private long duration;
	private boolean fini;

	public void setGoOn(boolean goOn) {
		this.goOn = goOn;
	}

	public boolean isGoOn() {
		return this.goOn;
	}

	public ClockTick(MainInterface main) {
		tick = 0;
		left = 0;
		this.goOn = false;
		this.holdDecNow = false;
		this.holdIncNow = true;
		this.holdAddTick = false;
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
		while (this.goOn && this.tick < this.left) {
			while (this.holdAddTick) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			synchronized (this.tcLock) {
				this.tick++;
			}
		}
		while (this.now != this.main.getTotal()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		synchronized (tcLock) {
			this.goOn = false;
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
		return now;
	}

	public synchronized void decNow() {
		this.now--;
		if (this.now == 0){
			this.holdDecNow = true; /* hold agent until "now" is 0 */
			this.holdIncNow = false;
		}
	}

	public synchronized void incNow() {
		this.now++;
		if (this.now == this.main.getTotal()){
			this.holdDecNow = false; /* hold agent until "now" is total */
			this.holdIncNow = true;
			this.holdAddTick = false;
		}
	}
	
	public boolean isHoldDecNow(){
		return this.holdDecNow;
	}
	
	public boolean isHoldIncNow(){
		return this.holdIncNow;
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

	public static void main(String[] args) {
		ClockTick clk = new ClockTick(null);
		new Thread(clk).start();
	}
}
