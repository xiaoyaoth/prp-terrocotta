package simulation.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ScenariosMgr implements Runnable {

	private static BlockingQueue<String[]> snrCfgs = new LinkedBlockingQueue<String[]>();
	private SnrMonitorThread mon = new SnrMonitorThread();
	private static Map<Integer, Scenario> snrs = new HashMap<Integer, Scenario>();
	private static ArrayList<Integer> snrIDs = new ArrayList<Integer>();
	public static int finiCaseNum;
	private static int snrID;

	public ScenariosMgr() {
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
				//checkFini();
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
						FileReader fr = new FileReader(f);
						BufferedReader br = new BufferedReader(fr);
						String cfg = br.readLine();
						String[] segs = cfg.split("_");
						System.out.println(segs[0] + " " + segs[1] + " "
								+ segs[2]);
						this.addSnr(segs[1], segs[2]);
						br.close();
						fr.close();
						f.delete();
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void addSnr(String usr, String tick) {
			String[] snr = new String[2];
			snr[0] = usr;
			snr[1] = tick;
			ScenariosMgr.snrCfgs.add(snr);
		}
	}

	public static void main(String[] args) {
		new ScenariosMgr();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				String[] oneSnr = ScenariosMgr.snrCfgs.take();
				System.out.println("in ScenariosMgr.java, I take it");
				Scenario c = new Scenario(oneSnr[0], oneSnr[1]);
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
			} catch (InterruptedException e) {
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
	
	public synchronized static Integer newSnrID(){
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
			for (int i = 0; i < Server.serverInfo.size() * Math.random(); i++)
				res = iter.next();
			return res;
		case 1:// based on machine performance
			int bestPerf =0 ;
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
			System.out.println("assign called, bestID is "+bestId);
			return bestId;
		case 3:
			// case 1:// based on agent_list size and wait_list size
			// se = Server.servers.get(0);
			// for (Server s : Server.servers)
			// if (s.agents.size() < se.agents.size())
			// se = s;
			// return se;
		case 4:// based on agents' relationship with each other
			/*
			 * actually it should be the logic of the Client instead, the Server
			 * just responsible for return proper JVM_id from the perspective of
			 * Hardware thus should not burden the work to assign a agent a
			 * specific JVM_id
			 */
			return -1;
		}
	}
}
