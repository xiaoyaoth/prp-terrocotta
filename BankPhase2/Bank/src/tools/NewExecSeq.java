package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class NewExecSeq {
	public static void main(String[] args) throws InterruptedException, IOException{
		new SnrQueueGen(1, "usr7", 30, "59.78.14.167");
		Thread.sleep((int)(10000*Math.random()+10000));
		String start = new Date().getTime()+"";
		File fo = new File("statistics\\newSeqBegin_" + start + ".txt");
		FileWriter fw = new FileWriter(fo);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(start);
		bw.flush();
		bw.close();
		fw.close();
		for(int i = 0; i<10; i++){
			new SnrQueueGen(1, "usr1", 30, "59.78.14.167");
			Thread.sleep((int)(10000*Math.random()+10000));
		}
	}

}
