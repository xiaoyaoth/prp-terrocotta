package simulation.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ScenariosMgr implements Runnable {

	private static Queue<Parse>[] priorityQueue;
	private SnrMonitorThread mon = new SnrMonitorThread();
	private static Map<Integer, Scenario> snrs = new HashMap<Integer, Scenario>();
	private static ArrayList<Integer> snrIDs = new ArrayList<Integer>();
	public static int finiCaseNum;
	private static int snrID;

	public ScenariosMgr() {
		Comparator<Parse> compTick = new Comparator<Parse>() {
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
		Comparator<Parse> compAgentNum = new Comparator<Parse>() {
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

		priorityQueue = new PriorityQueue[3];
		priorityQueue[0] = new PriorityQueue<Parse>(5, compTick);
		priorityQueue[1] = new PriorityQueue<Parse>(5, compTick);
		priorityQueue[2] = new PriorityQueue<Parse>(5, compTick);
		Thread monThread = new Thread(mon);
		monThread.setName("MonitorThread");
		monThread.start();
		Thread cmThread = new Thread(this);
		cmThread.setName("ClientMgrThread");
		cmThread.start();
	}

	private class SnrMonitorThread implements Runnable {
		String snrQueuePath = "snrQueue\\";

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				readCfg();
				// checkFini();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private void readCfg() {
			File dir = new File(snrQueuePath);
			try {
				if (!dir.isDirectory())
					throw new Exception();
				File[] flist = dir.listFiles();
				for (File f : flist) {
					if (!f.isDirectory()) {
						System.out.println("************readCfg************");
						System.out.println(flist);
						System.out.println(f);
						System.out.println("~~~~~~~~~~~~readCfg~~~~~~~~~~~~");
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
					}
				}
				flist = null;
				dir = null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void addSnr(String usr, String tick, String prior) {
			int pr = Integer.parseInt(prior);
			System.out.println("I am here");
			if (pr == 0 || pr == 1 || pr == 2)
				synchronized (priorityQueue) {
					priorityQueue[pr].add(new Parse(usr, tick));
				}
			else
				System.err.println("In ScenariosMgr.java, addSnr error");
			printQueue();
		}

		private void printQueue() {
			System.out.println("*************printQueue*****************");
			for (int i = 0; i < 3; i++) {
				for (Parse p : priorityQueue[i])
					System.out.print(p + " ");
				System.out.println();
			}
			System.out.println("~~~~~~~~~~~~~printQueue~~~~~~~~~~~~~~~~~");
		}
	}

	public static void main(String[] args) {
		new ScenariosMgr();
	}

	private static Parse pollSnr() {
		Parse p = null;
		int debug = 0;
		try {
			if (debug == 1) {
				int bestId = assign();
				while (Server.serverInfo.get(bestId).getRatio() < PerformanceThread
						.getThreshold())
					Thread.sleep(1000);
			} else if (debug == 2) {
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		synchronized (priorityQueue) {
			if (priorityQueue[2].size() != 0)
				p = priorityQueue[2].poll();
			else if (priorityQueue[1].size() != 0)
				p = priorityQueue[1].poll();
			else if (priorityQueue[0].size() != 0)
				p = priorityQueue[0].poll();
			else
				try {
					priorityQueue.wait(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return p;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				Parse oneSnr = ScenariosMgr.pollSnr();
				while(oneSnr == null)
					oneSnr = ScenariosMgr.pollSnr();
				System.out.println("in ScenariosMgr.java, I take it");
				int debugMode = 0;

				Integer hostId;
				if (debugMode == 0)
					hostId = ScenariosMgr.assign();
				else
					hostId = ScenariosMgr.assignWithPriority();

				Scenario c = new Scenario(hostId, oneSnr);
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

	public static Integer assignWithPriority() {
		Integer hostID = ScenariosMgr.assign();
		while (Server.serverInfo.get(hostID).getRatio() < PerformanceThread
				.getThreshold()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			hostID = ScenariosMgr.assign();
		}
		return hostID;
	}

	public static Integer assign() {
		int mode = 0;
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
			System.out.print(count+" ");
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
}
