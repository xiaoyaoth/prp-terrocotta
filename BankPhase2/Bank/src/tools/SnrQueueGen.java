package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class SnrQueueGen{
	
	public static void main(String[] args){
		new SnrQueueGen(args[0], args[1], args[2]);
	}
	
	public SnrQueueGen(String snrAmt, String usr, String tick){
		new SnrQueueGen(Integer.parseInt(snrAmt), usr, Integer.parseInt(tick));
	}
	
	public SnrQueueGen(int snrAmt, String usr, int tick){
		Date d = new Date();
		for(int i = 0; i<snrAmt; i++){
			File f = new File("snrQueue\\"+d.getTime()+i+"_"+usr+"_"+tick);
			try {
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(d.getTime()+i+"_"+usr+"_"+tick);
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
