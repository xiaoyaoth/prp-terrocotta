package simulation.modeling;

import java.io.Serializable;

import simulation.runtime.ScenariosMgr;

public class ClockTick implements Runnable, Serializable {
	private int tick, left, now;
	private long start, end;
	private boolean goOn;
	transient private Lock tickLock;
	transient private Lock nowLock;
	transient private Lock tcLock;
	transient private MainInterface main;

	private String duration;
	private boolean migrate;

	/* for debug purpose */
	transient boolean waiting;

	public ClockTick(MainInterface main) {
		this.main = main;
		this.tcLock = new Lock();
		this.nowLock = new Lock();
		this.tickLock = new Lock();
		this.tick = 0;
		this.left = 0;
		this.goOn = false;
		this.migrate = false;
		this.duration = "empty";
	}

	public void recover(MainInterface main) {
		this.main = main;
		this.migrate = true;
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
	
	public boolean enoughRemainTick(){
		return (this.left - this.tick)>1;
	}

	public void run() {

		System.out.println("in ClockTick.java now is " + this.now);

		if (this.migrate) {
			synchronized (this.tickLock) {
				try {
					this.tickLock.wait();
					this.migrate = false;
					System.out.println();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			synchronized (tcLock) {
				this.goOn = true;
				this.start = new java.util.Date().getTime();
				System.out.println(this.start);
			}
		}

		while (!this.migrate && this.tick < this.left) {
			// System.out.print("this.tick<this.left");
			while (!this.migrate && this.goOn && this.tick < this.left) {
				System.out.println(this.main.getCaseID() + " Tick " + tick
						+ " :" + this.now);
				synchronized (nowLock) {
					++this.tick;
				}
				synchronized (this.tickLock) {
					synchronized (nowLock) {
						this.now = this.main.getTotal();
						this.nowLock.notifyAll();
					}

					try {
						while (this.now > 0) {
							// System.out.println("tickLock locked");
							this.waiting = true;
							this.tickLock.wait();
							this.waiting = false;
							if (this.migrate)
								break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		if (!this.migrate) {
			synchronized (this.tickLock) {
				while (this.now > 0)
					try {
						System.out
								.println("outloop waiting, in ClockTick.java");
						this.tickLock.wait();
						System.out.println("outloop waiting end");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				goOn = false;
				this.end = new java.util.Date().getTime();
				this.duration = "start:" + this.start + " end:" + this.end
						+ " duration:" + (this.end - this.start);
				/*
				 * 这里不是fini,当clocktick把所有的agent唤醒，clocktick结束了，但是Agent还在运行。
				 * 所以当所有Agent结束运行才是一切的终结，想办法弄一下
				 */
				synchronized (this.tcLock) {
					this.tcLock.notify();
				}
				System.out.println("ClockTickFini in ClockTick.java");
				ScenariosMgr.incFiniCaseNum();
				this.main = null;
			}
		} else {
			System.out.println("2. in ClockTick.java, clk is in file now");
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
			if (this.now == 0) {
				this.tickLock.notifyAll();
				// System.out.println("B ");
			}
		}
	}

	public void notifyTickLock() {
		synchronized (this.tickLock) {
			this.tickLock.notifyAll();
		}
	}

	public void notifyTcLock() {
		synchronized (this.tcLock) {
			this.tcLock.notifyAll();
		}
	}

	public void notifyNowLock() {
		synchronized (this.nowLock) {
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

	public void print() {
		// System.out.println("*****ClockTick Info**********");
		// System.out.println(this);
		// System.out.println("goOn:\t " + this.goOn);
		// System.out.println("left:\t " + this.left);
		// System.out.println("migrate:\t " + this.migrate);
		// System.out.println("now:\t " + this.now);
		// System.out.println("tick:\t " + this.tick);
		// System.out.println("main:\t " + this.main);
		// System.out.println("tcLock:\t " + this.tcLock);
		// System.out.println("start:\t " + this.start);
		// System.out.println("end:\t " + this.end);
		// System.out.println("duration:\t " + this.duration);
		// System.out.println("waiting:\t " + this.waiting);
		// System.out.println("*****ClockTick Info end*******");
		System.out.println(this.debugMessage());
	}

	public String debugMessage() {
		return "*****ClockTick Info**********\n" + this + "\ngoOn:\t "
				+ this.goOn + "\nleft:\t " + this.left + "\nmigrate:\t "
				+ this.migrate + "\nnow:\t " + this.now + "\ntick:\t "
				+ this.tick + "\nmain:\t " + this.main + "\ntcLock:\t "
				+ this.tcLock + "\nstart:\t " + this.start + "\nend:\t "
				+ this.end + "\nduration:\t " + this.duration + "\nwaiting:\t "
				+ this.waiting + "\n*****ClockTick Info end*******";
	}

	public static void main(String[] args) {
		ClockTick clk = new ClockTick(null);
		new Thread(clk).start();
	}
}
