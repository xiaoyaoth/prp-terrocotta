package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class NewExecSeq {
	public static void main(String[] args) throws InterruptedException, IOException{
//		String start = new Date().getTime()+"";
//		File fo = new File("statistics\\newSeqBegin_" + start + ".txt");
//		FileWriter fw = new FileWriter(fo);
//		BufferedWriter bw = new BufferedWriter(fw);
//		bw.write(start);
//		bw.flush();
//		bw.close();
//		fw.close();
		for(int i = 0; i<1; i++){
//			new SnrQueueGen(1, "test100", 15, 1);
			new SnrQueueGen(10, "test100", 20, 1);
		}
	}
}
