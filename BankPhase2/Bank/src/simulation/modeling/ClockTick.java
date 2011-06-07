package simulation.modeling;

import java.io.Serializable;
import java.util.Date;

public class ClockTick implements Runnable, Serializable {
	private int tick, left, now;
	private boolean goOn;
	transient private Lock tickLock;
	transient private Lock nowLock;
	transient private Lock tcLock;
	transient private MainInterface main;

	private String duration;
	private boolean migrate;

	public ClockTick(MainInterface main) {
		this.tcLock = new Lock();
		this.nowLock = new Lock();
		this.tickLock = new Lock();
		tick = 0;
		left = 0;
		goOn = false;
		this.migrate = false;
		this.main = main;
	}
	
	public void recover(MainInterface main){
		this.main = main;
		this.migrate = false;
		this.tickLock = new Lock();
		this.nowLock = new Lock();
		this.tcLock = new Lock();
	}

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
		
		if(this.migrate){
			synchronized(this.tickLock){
				try {
					this.tickLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		while (!this.migrate && this.tick < this.left) {
			// System.out.print("this.tick<this.left");
			while (!this.migrate && this.goOn && this.tick < this.left) {
				synchronized (nowLock) {
					++this.tick;
				}
				System.out.println(this.main.getCaseID() + " Tick " + tick
						+ " :" + this.now);
				synchronized (this.tickLock) {
					synchronized (nowLock) {
						this.now = this.main.getTotal();
						this.nowLock.notifyAll();
					}

					try {
						System.out.print("A ");
						while (this.now > 0) {
							// System.out.println("tickLock locked");
							this.tickLock.wait();
							// System.out.println("tickLock released");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (!this.migrate) {
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
		}else{
			System.out.println("in ClockTick.java, clk is in file now");
		}
	}

	public int getNow() {
		synchronized (this.nowLock) {
			return now;
		}
	}

	public void decNow() {
		synchronized (this.tickLock) {
			// System.out.print(now + " ");
			--now;
			if (this.now <= 0) {
				this.tickLock.notifyAll();
				// System.out.println("B ");
			}
		}
	}

	public void notifyTickLock(){
		synchronized(this.tickLock){
			this.tickLock.notifyAll();
		}
	}
	
	public void notifyTcLock(){
		synchronized(this.tcLock){
			this.tcLock.notifyAll();
		}
	}
	
	public void notifyNowLock(){
		synchronized(this.nowLock){
			this.nowLock.notifyAll();
		}
	}
	
	public String getDuration() {
		return this.duration;
	}

	public void setMain(MainInterface main) {
		this.main = main;
	}

	/* lock on tickLock, so that this func is called when clk is waiting */
	public void setMigrate(boolean migrate) {
		synchronized (this.tickLock) {
			this.migrate = migrate;
		}
	}

	public boolean isMigrate() {
		return this.migrate;
	}
	
	public void print(){
		System.out.println("***************");
		System.out.println(this);
		System.out.println("goOn: "+this.goOn);
		System.out.println("left: "+this.left);
		System.out.println("migrate: "+this.migrate);
		System.out.println("now: "+this.now);
		System.out.println("tick: "+this.tick);
		System.out.println("main: "+this.main);
		System.out.println("tcLock: "+this.tcLock);
		System.out.println("***************");
	}

	public static void main(String[] args) {
		ClockTick clk = new ClockTick(null);
		new Thread(clk).start();
	}
}
