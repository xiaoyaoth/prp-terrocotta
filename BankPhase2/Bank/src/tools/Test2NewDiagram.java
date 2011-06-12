package tools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;

public class Test2NewDiagram {

	ArrayList<Data> dlist = new ArrayList<Data>();
	private long start;
	private long end;
	private int duration;
	private int factor = 1000;
	private int unit1 = 100;
	private int unit2 = 10;
	private int anchor = 20;
	private String filename;
	private BufferedImage bi;
	private static final String ROOT = "statistics\\Test2\\";

	public static void main(String args[]) {
		File dir = new File("statistics\\Test2");
		File[] flist = dir.listFiles();
		for (File f : flist) {
			if (f.isDirectory() && !f.getName().equals(".svn"))
				new Test2NewDiagram(f);
		}
	}

	public Test2NewDiagram(File f) {
		this(f.getName());
	}

	public Test2NewDiagram(String filename) {
		super();
		this.filename = filename;
		this.start = Long.MAX_VALUE;
		this.end = 0;
		File dir = new File(ROOT + this.filename);
		try {
			this.readData(dir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i = this.dlist.size();
		this.duration = (int) (this.end - this.start);
		int width = 2 * anchor + (int) (this.duration / this.factor);
		int height = 3 * anchor + unit2 * (2 + i);
		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D ig2 = bi.createGraphics();
		Comparator<Data> comp = new Comparator<Data>() {
			public int compare(Data d1, Data d2) {
				if (d1.start > d2.start)
					return 1;
				else if (d1.start < d2.start)
					return -1;
				else
					return 0;
			}
		};
		Collections.sort(this.dlist, comp);
		ig2.setBackground(Color.WHITE);
		ig2.clearRect(0, 0, width, height);
		this.paint(ig2);
		try {
			ImageIO.write(bi, "png", new File("statistics\\Test2Res\\" + dir.getName() + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void paint(Graphics2D g) {
		int ypos = anchor + unit2 * (2 + this.dlist.size());
		g.setPaint(Color.BLACK);
		g.drawLine(anchor, anchor, anchor, ypos);
		int xpos = anchor + this.duration / factor;
		g.drawLine(xpos, anchor, xpos, ypos);
		g.drawString(this.dateFormat(this.start), anchor + 1, ypos);
		g.drawString(this.dateFormat(this.end), xpos - 50, ypos);
		xpos = anchor;
		int totalCost=0;
		for(Data d:this.dlist)
			totalCost+=d.duration;
		String printInfo ="AgNum:" + this.dlist.size() + ", duration:"
		+ this.duration / 1000 + "s, total cost:"+totalCost/1000+"s";
		g.drawString(printInfo, xpos + 1, ypos + 20);
		for (int i = 0; i < this.dlist.size(); i++)
			this.drawDataRect(g, i);
	}

	private void readData(File dir) throws IOException {
		System.out.println(dir);
		File[] flist = dir.listFiles();
		for (File f : flist) {
			if (!f.getName().equals(".svn")) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				Data d = new Data();
				d.readData(br);
				if (d.start < this.start)
					this.start = d.start;
				if (d.end > this.end)
					this.end = d.end;
				this.dlist.add(d);
			}
		}
	}

	private void drawDataRect(Graphics2D g, int i) {
		int s = (int) (this.dlist.get(i).start - this.start) / this.factor;
		int d = this.dlist.get(i).duration / this.factor;
		int ms = (int) (this.dlist.get(i).migStart - this.start) / this.factor;
		int mc = this.dlist.get(i).migCost / this.factor;

		//System.out.println(s + " " + d + " " + ms + " " + mc + " " + i + "\n");
		g.setPaint(Color.RED);
		g.fillRect(anchor + s, anchor + unit2 * (1 + i), d, (int) (unit2 * 0.8));
		if (ms >= 0) {
			g.setPaint(Color.YELLOW);
			g.fillRect(anchor + ms, anchor + unit2 * (1 + i), mc,
					(int) (unit2 * 0.8));
		}
	}

	private String dateFormat(long date) {
		java.util.Date currentTime = new java.util.Date(date);
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
				"HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}
}

class NewData {
	long start;
	long end;
	int duration;
	String usr;
	int tick;
	int prior;
	long migStart;
	long migEnd;
	int migCost;

	public NewData() {

	}

	public void readData(BufferedReader br) throws IOException {
		String line = br.readLine();
		// System.out.println(line);
		this.findStart(line);
		this.findEnd(line);
		this.findDuration(line);

		line = br.readLine();
		// System.out.println(line);
		this.findUsr(line);
		this.findTick(line);
		this.findPrior(line);

		line = br.readLine();
		// System.out.println(line);
		this.findMigStart(line);
		this.findMigEnd(line);
		this.findMigCost(line);
		// System.out.println();
	}

	public void findStart(String line) {
		int pos1 = line.indexOf("start:");
		int pos2 = line.indexOf(" end:");
		this.start = Long.parseLong(line.substring(pos1 + 6, pos2));
		// System.out.println(this.start);
	}

	public void findEnd(String line) {
		int pos1 = line.indexOf("end:");
		int pos2 = line.indexOf(" duration:");
		this.end = Long.parseLong(line.substring(pos1 + 4, pos2));
		// System.out.println(this.end);
	}

	public void findDuration(String line) {
		int pos1 = line.indexOf("duration:");
		int pos2 = line.length();
		this.duration = Integer.parseInt(line.substring(pos1 + 9, pos2));
		// System.out.println(this.duration);
	}

	public void findUsr(String line) {
		int pos1 = line.indexOf("usr:");
		int pos2 = line.indexOf(" tick:");
		this.usr = line.substring(pos1 + 4, pos2);
		// System.out.println(this.usr);
	}

	public void findTick(String line) {
		int pos1 = line.indexOf("tick:");
		int pos2 = line.indexOf(" prior:");
		this.tick = Integer.parseInt(line.substring(pos1 + 5, pos2));
		// System.out.println(this.tick);
	}

	public void findPrior(String line) {
		int pos1 = line.indexOf("prior:");
		int pos2 = line.length();
		this.prior = Integer.parseInt(line.substring(pos1 + 6, pos2));
		// System.out.println(this.prior);
	}

	public void findMigStart(String line) {
		int pos1 = line.indexOf("migStart:");
		int pos2 = line.indexOf(" migEnd:");
		this.migStart = Long.parseLong(line.substring(pos1 + 10, pos2));
		// System.out.println(this.migStart);
	}

	public void findMigEnd(String line) {
		int pos1 = line.indexOf("migEnd:");
		int pos2 = line.indexOf(" migCost:");
		this.migEnd = Long.parseLong(line.substring(pos1 + 7, pos2));
		// System.out.println(this.migEnd);
	}

	public void findMigCost(String line) {
		int pos1 = line.indexOf("migCost:");
		int pos2 = line.length();
		this.migCost = Integer.parseInt(line.substring(pos1 + 8, pos2));
		// System.out.println(this.migCost);
	}

	public String toString() {
		return this.start + " " + this.end + " " + this.duration + "\n"
				+ this.usr + " " + this.tick + " " + this.prior + "\n"
				+ this.migStart + " " + this.migEnd + " " + this.migCost;
	}

	public NewData(long s, long e, int d, String u, int t, int p, long ms,
			long me, int mc) {
		this.start = s;
		this.end = e;
		this.duration = d;
		this.usr = u;
		this.tick = t;
		this.prior = p;
		this.migStart = ms;
		this.migEnd = me;
		this.migCost = mc;
	}
}
/*
 * //change outline color Graphics.setColor(Color.white); Graphics.drawRect(int
 * x1,int y1,int x2,int y2);
 * 
 * //change fill color Graphics.setColor(Color.black); Graphics.fillRect(int
 * x1,int y1,int x2,int y2);
 */