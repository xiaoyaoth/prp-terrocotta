package simulation.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import simulation.modeling.Lock;

public class ScenariosMgr implements Runnable {

	private static Queue<Parse>[] priorityQueue;
	private SnrMonitorThread mon = new SnrMonitorThread();
	private static Map<Integer, Scenario> snrs = new HashMap<Integer, Scenario>();
	private static ArrayList<Integer> snrIDs = new ArrayList<Integer>();
	private static int finiCaseNum;
	private static int snrID;
	private static Lock snrPollLock;
	private static final int QUEUE_ARRAY_SIZE = 2;
	private static boolean debug = false;

	public ScenariosMgr() {
		priorityQueue = new Queue[QUEUE_ARRAY_SIZE];
		for (int i = 1; i < QUEUE_ARRAY_SIZE; i++)
			//priorityQueue[i] = new PriorityQueue<Parse>(5, compNumAndTick);
			priorityQueue[i] = new LinkedList<Parse>();
		/* [0] is the lowest priority queue, without sort */
		priorityQueue[0] = new LinkedList<Parse>();

		snrPollLock = new Lock();

		/* monitor starts */
		Thread monThread = new Thread(mon);
		monThread.setName("MonitorThread");
		monThread.start();
		/* ScenariosMgr starts */
		Thread cmThread = new Thread(this);
		cmThread.setName("ClientMgrThread");
		cmThread.start();
	}

	private class SnrMonitorThread implements Runnable {
		String snrQueuePath = "snrQueue\\";

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//this.cleanCfgFolder();
			while (true) {
				readCfg();
			}
		}

		private void cleanCfgFolder() {
			File dir = new File(snrQueuePath);
			File[] flist = dir.listFiles();
			for (File f : flist) {
				if (!f.isDirectory())
					f.delete();
			}
		}

		private void readCfg() {
			File dir = new File(snrQueuePath);
			try {
				if (!dir.isDirectory())
					throw new Exception();
				File[] flist = dir.listFiles();
				int amt = 0;
				for (File f : flist) {
					if (!f.isDirectory()) {
						FileReader fr = new FileReader(f);
						BufferedReader br = new BufferedReader(fr);
						String cfg = br.readLine();
						String[] segs = cfg.split("_");
						System.out.println(segs[0] + " " + segs[1] + " "
								+ segs[2] + " " + segs[3]);
						br.close();
						fr.close();
						f.delete();
						f = null;
						this.addSnr(segs[1], segs[2], segs[3]);
						amt++;
					}
				}
				flist = null;
				dir = null;
				if (amt > 0)
					synchronized (snrPollLock) {
						snrPollLock.notifyAll();
					}
				Thread.sleep(30000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void addSnr(String usr, String tick, String prior) {
			int pr = Integer.parseInt(prior);
			if (pr > 0 && pr < QUEUE_ARRAY_SIZE) {
				synchronized (priorityQueue) {
					priorityQueue[pr].offer(new Parse(usr, tick, prior));
				}
			} else
				System.err.println("In ScenariosMgr.java, addSnr error");
			printQueue();
		}
	}

	public static void main(String[] args) {
		new ScenariosMgr();
	}

	private static Parse pollAndAdjustQueue() {
		Parse oneSnr = ScenariosMgr.pollSnr();
		while (oneSnr == null)
			oneSnr = ScenariosMgr.pollSnr();

		int bestId = ScenariosMgr.assign();
		oneSnr.setHostId(bestId);

		if (!debug) {
			if (ScenariosMgr.adjustSnr(bestId, oneSnr))
				return null;

			Server.serverInfo.get(bestId).incAgentTotal(
					oneSnr.getAgentTotalNum());
		}
		System.out.println(oneSnr);
		return oneSnr;
	}

	private static double ratioCal(int hostId, int newAgNum) {
		int AgCount = Server.serverInfo.get(hostId).getAgentCount();
		int AgTotal = Server.serverInfo.get(hostId).getAgentTotal();
		if (AgTotal != 0)
			return (double) AgCount / (double) (AgTotal + newAgNum);
		else
			return Integer.MAX_VALUE;
	}

	private static boolean adjustSnr(Integer bestId, Parse oneSnr) {
		int weak = 0;
		double ratio = ratioCal(bestId, oneSnr.getAgentTotalNum());
		while (ratio < PerformanceThread.getThreshold()) {
			if (weak > 3) {
				int prior = oneSnr.getPrior();
				if (prior > 0)
					prior--;
				ScenariosMgr.priorityQueue[prior].add(oneSnr);
				synchronized (ScenariosMgr.snrs) {
					try {
						System.out
								.println("adjustLock waiting, in ScenariosMgr");
						ScenariosMgr.printQueue();
						ScenariosMgr.snrs.wait();
						System.out
								.println("adjustLock running, in ScenariosMgr");
						/* adjustLock will be notifed in Scenario.java run() */
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return true;
			} else
				ratio = ratioCal(bestId, oneSnr.getAgentTotalNum());
			System.out.print(ratio + "adjustWeak" + (weak++) + " ");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	private static Parse pollSnr() {
		Parse p = null;
		synchronized (priorityQueue) {
			for (int i = QUEUE_ARRAY_SIZE - 1; i >= 0; i--) {
				p = priorityQueue[i].poll();
				if (p != null)
					break;
			}
		}
		if (p == null) {
			synchronized (snrPollLock) {
				try {
					System.out
							.println("in ScenariosMgr, !!!queue empty, waiting!!!");
					snrPollLock.wait();
					System.out
							.println("in ScenariosMgr, !!!queue is notified!!!");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return p;
	}

	private static void printQueue() {
		System.out.println("*******in ScenariosMgr.java, priorityQueue***");
		for (int i = 0; i < QUEUE_ARRAY_SIZE; i++) {
			System.out.println("#" + i);
			for (Parse p : priorityQueue[i])
				System.out.print(p + " ");
			System.out.println();
		}
		System.out.println("~~~~~~~in ScenariosMgr.java, priorityQueue~~~");
	}

	@Override
	/*
	 * the policy now is to find a minimal time snr, at the same time the server
	 * should have enough place to run the scenario As shown, pollSnr() selects
	 * the minimal time snr adjustQueue guarantee that the best server is
	 * available for the scenario
	 */
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				if (debug)
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				Parse oneSnr = null;
				while (oneSnr == null)
					oneSnr = ScenariosMgr.pollAndAdjustQueue();
				System.out.println("in ScenariosMgr.java, run() takes care it");
				Scenario c = new Scenario(oneSnr);
				new Thread(c).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static synchronized void put(Scenario c) {
		synchronized (ScenariosMgr.snrs) {
			ScenariosMgr.snrs.put(c.getCaseID(), c);
		}
	}

	public static synchronized void add(Scenario c) {
		synchronized (ScenariosMgr.snrs) {
			ScenariosMgr.snrs.put(c.getCaseID(), c);
		}
		synchronized (ScenariosMgr.snrIDs) {
			ScenariosMgr.snrIDs.add(c.getCaseID());
		}
	}

	public static synchronized void remove(Scenario c) {
		synchronized (ScenariosMgr.snrs) {
			ScenariosMgr.snrs.remove(c);
			ScenariosMgr.snrs.keySet().remove(c.getCaseID());
		}
		synchronized (ScenariosMgr.snrIDs) {
			ScenariosMgr.snrIDs.remove(c.getCaseID());
		}
		c = null;
	}

	public static Map<Integer, Scenario> getSnrs() {
		return ScenariosMgr.snrs;
	}

	public static ArrayList<Integer> getSnrIDs() {
		return ScenariosMgr.snrIDs;
	}

	public synchronized static Integer newSnrID() {
		return ScenariosMgr.snrID++;
	}

	public static Integer assign() {
		int mode = 2;
		int bestId = -1;
		int tempId = -1;
		Iterator<Integer> iter = Server.serverInfo.keySet().iterator();

		switch (mode) {
		default:
		case 0:
			int res = 0;
			int count = (int) (Server.serverInfo.size() * Math.random());
			for (int i = 0; i <= count; i++)
				res = iter.next();
			System.out.print(count + " ");
			return res;
		case 1:// based on machine performance
			int bestPerf = 0;
			while (iter.hasNext()) {
				tempId = iter.next();
				int tempPerf = Server.serverInfo.get(tempId).getPerf();
				if (tempPerf >= bestPerf) {
					bestPerf = tempPerf;
					bestId = tempId;
				}
			}
			return bestId;
		case 2:
			double bestRatio = 0;
			while (iter.hasNext()) {
				tempId = iter.next();
				double tempRatio = Server.serverInfo.get(tempId).getRatio();
				if (tempRatio >= bestRatio) {
					bestRatio = tempRatio;
					bestId = tempId;
				}
			}
			System.out
					.println("in ScenariosMgr.java, assign called, bestID is "
							+ bestId);
			return bestId;
		}
	}

	public synchronized static void incFiniCaseNum() {
		ScenariosMgr.finiCaseNum++;
	}

	public synchronized static int getFiniCaseNum() {
		return finiCaseNum;
	}

	private Comparator<Parse> compTick = new Comparator<Parse>() {
		@Override
		public int compare(Parse arg0, Parse arg1) {
			// TODO Auto-generated method stub
			if (arg0.getTick() > arg1.getTick())
				return 1;
			else if (arg0.getTick() < arg1.getTick())
				return -1;
			else
				return 0;
		}
	};

	@SuppressWarnings("unused")
	private Comparator<Parse> compAgentNum = new Comparator<Parse>() {
		@Override
		public int compare(Parse arg0, Parse arg1) {
			// TODO Auto-generated method stub
			if (arg0.getAgentTotalNum() > arg1.getAgentTotalNum())
				return 1;
			else if (arg0.getAgentTotalNum() < arg1.getAgentTotalNum())
				return -1;
			else
				return 0;
		}
	};

	@SuppressWarnings("unused")
	private Comparator<Parse> compNumAndTick = new Comparator<Parse>() {
		@Override
		public int compare(Parse arg0, Parse arg1) {
			// TODO Auto-generated method stub
			int e1 = arg0.getAgentTotalNum() * arg0.getTick();
			int e2 = arg1.getAgentTotalNum() * arg1.getTick();
			if (e1 > e2)
				return 1;
			else if (e1 < e2)
				return -1;
			else
				return 0;
		}
	};
}