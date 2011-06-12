package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class NewExecSeq {
	public static void main(String[] args) throws InterruptedException,
			IOException {
		// String start = new Date().getTime()+"";
		// File fo = new File("statistics\\newSeqBegin_" + start + ".txt");
		// FileWriter fw = new FileWriter(fo);
		// BufferedWriter bw = new BufferedWriter(fw);
		// bw.write(start);
		// bw.flush();
		// bw.close();
		// fw.close();
		new File("statistics\\b"+Math.random()).createNewFile();
		java.util.Scanner in = new java.util.Scanner(System.in);
		int sel = Integer.parseInt(in.nextLine());
		if (sel == 0) {
			new SnrQueueGen(1, "test100", 30, 1);
			in.nextLine();
			for (int i = 1; i < 4; i++) {
				// new SnrQueueGen(1, "test100", 15, 1);
				new SnrQueueGen(1, "test100", 20, i);
				new SnrQueueGen(1, "test10", 100, i);
				new SnrQueueGen(1, "test50", 30, i);
			}
		}else if(sel == 1){
			for (int i = 1; i < 4; i++) {
				// new SnrQueueGen(1, "test100", 15, 1);
				new SnrQueueGen(20, "test10", 20, i);
			}
		}else if(sel == 2) {
			new SnrQueueGen(1, "test100",10,1);
		}
	}
}
