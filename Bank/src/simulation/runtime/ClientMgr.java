package simulation.runtime;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ClientMgr implements Runnable {

	private static BlockingQueue<String[]> snrs = new LinkedBlockingQueue<String[]>();
	private SnrMonitorThread mon = new SnrMonitorThread();
	
	public ClientMgr(){
		new Thread(mon).start();
		new Thread(this).start();
	}

	private class SnrMonitorThread implements Runnable {
		String snrQueuePath = "snrQueue\\";

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				File dir = new File(snrQueuePath);
				try {
					if (!dir.isDirectory())
						throw new Exception();
					File[] flist = dir.listFiles();
					for(File f: flist){
						String filename = f.getName();
						String[] segs = filename.split("_");
						System.out.println(segs[0]+" "+segs[1]+" "+segs[2]);
						this.addSnr(segs[1], segs[2]);
						f.delete();
					}
				} catch (Exception e) {
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

		private void addSnr(String usr, String tick) {
			String[] snr = new String[2];
			snr[0] = usr;
			snr[1] = tick;
			ClientMgr.snrs.add(snr);
		}
	}

	public static void main(String[] args) {
		new ClientMgr();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				String[] oneSnr = ClientMgr.snrs.take();
				Client c = new Client(oneSnr[0], oneSnr[1]);
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
}
