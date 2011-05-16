package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import simulation.runtime.SendFile;

public class SnrQueueGen{
	
	public static void main(String[] args){
		new SnrQueueGen(args[0], args[1], args[2], args[3]);
	}
	
	public SnrQueueGen(String snrAmt, String usr, String tick, String dest){
		new SnrQueueGen(Integer.parseInt(snrAmt), usr, Integer.parseInt(tick), dest);
	}
	
	public SnrQueueGen(int snrAmt, String usr, int tick, String dest){
		Date d = new Date();
		for(int i = 0; i<snrAmt; i++){
			File f = new File("snrQueue\\temp\\"+d.getTime()+i+"_"+usr+"_"+tick+"_"+dest);
			try {
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(d.getTime()+i+"_"+usr+"_"+tick+"_"+dest);
				new Thread(new SendFile(dest, 10001, f)).start();
				System.out.println(f.getName());
				bw.close();
				fw.close();
				f.delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
