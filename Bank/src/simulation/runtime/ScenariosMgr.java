package simulation.runtime;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import simulation.modeling.Lock;

public class ScenariosMgr implements Runnable {

	private static BlockingQueue<String[]> snrCfgs = new LinkedBlockingQueue<String[]>();
	private SnrMonitorThread mon = new SnrMonitorThread();
	private static LinkedHashMap<Integer, Scenario> snrs = new LinkedHashMap<Integer, Scenario>();
	public static int finiCaseNum;

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
				checkFini();
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
						String filename = f.getName();
						String[] segs = filename.split("_");
						System.out.println(segs[0] + " " + segs[1] + " "
								+ segs[2]);
						this.addSnr(segs[1], segs[2]);
						f.delete();
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void checkFini() {
			Iterator<Scenario> iter = ScenariosMgr.snrs.values().iterator();
			while (iter.hasNext()) {
				Scenario c = iter.next();
				if (c.isExecFinished()) {
					ScenariosMgr.remove(c);
					c = null;
				}
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
	}

	public static synchronized void remove(Scenario c) {
		synchronized (ScenariosMgr.snrs) {
			ScenariosMgr.snrs.remove(c);
			ScenariosMgr.snrs.keySet().remove(c.getCaseID());
		}
	}

	public static LinkedHashMap<Integer, Scenario> getSnrs() {
		return ScenariosMgr.snrs;
	}
}