package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class NewExecSeq {
	public static void main(String[] args) throws InterruptedException, IOException{
		//String ip = "192.168.181.134";
		//String ip = "59.78.14.167";
		String ip = "localhost";
//		new SnrQueueGen(1, "usr7", 30, ip);
//		Thread.sleep((int)(10000*Math.random()+10000));
		String start = new Date().getTime()+"";
		File fo = new File("statistics\\newSeqBegin_" + start + ".txt");
		FileWriter fw = new FileWriter(fo);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(start);
		bw.flush();
		bw.close();
		fw.close();
		for(int i = 0; i<1; i++){
			new SnrQueueGen(5, "usr1", 100, ip);
			//Thread.sleep((int)(10000*Math.random()+10000));
		}
	}

}
