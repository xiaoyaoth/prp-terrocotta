package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Test3Analyzer {
	private ArrayList<Data> dlist = new ArrayList<Data>();
	private long start;
	private long end;
	private static final String ROOT = "statistics\\Test3\\part2";

	public Test3Analyzer(File f) {
		this(f.getName());
	}

	public Test3Analyzer(String filename) {
		this.start = Long.MAX_VALUE;
		this.end = 0;
		File dir = new File(ROOT + "\\" + filename);
		try {
			this.readData(dir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(this.dlist);
		// System.out.println(start);
		int totalValue = 0;
		for (Data d : dlist) {
			// System.out.println(d.start + " " + d.end + " " + d.prior + " "
			// + (d.start - this.start));
			int value = (int) (d.prior * (d.end - this.start));
			totalValue += value;
		}
		System.out.println(filename + " TV:" + totalValue + " D:"
				+ (this.end - this.start));
	}

	private void readData(File dir) throws IOException {
		// System.out.println(dir);
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

	public static void main(String[] args) {
		File dir = new File(ROOT);
		File[] flist = dir.listFiles();
		for (File f : flist) {
			if (f.isDirectory() && !f.getName().equals(".svn"))
				new Test3Analyzer(f);
		}
	}
}
