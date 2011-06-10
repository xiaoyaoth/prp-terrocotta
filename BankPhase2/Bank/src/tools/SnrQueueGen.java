package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class SnrQueueGen{
	private static int id;
	
	public static void main(String[] args){
		new SnrQueueGen(args[0], args[1], args[2], args[3]);
	}
	
	public SnrQueueGen(String snrAmt, String usr, String tick, String prio){
		new SnrQueueGen(Integer.parseInt(snrAmt), usr, Integer.parseInt(tick), Integer.parseInt(prio));
	}
	
	public SnrQueueGen(int snrAmt, String usr, int tick, int prio){
		Date d = new Date();
		for(int i = 0; i<snrAmt; i++){
			File f = new File("snrQueue\\"+d.getTime()+(id++)+"_"+usr+"_"+tick+"_"+prio);
			try {
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(d.getTime()+i+"_"+usr+"_"+tick+"_"+prio);
				bw.close();
				fw.close();
				System.out.println(f.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
