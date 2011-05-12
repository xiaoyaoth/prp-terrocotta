package tools;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class SnrQueueGen {
	
	public static void main(String[] args){
		new SnrQueueGen(args[0], args[1], args[2]);
	}
	
	public SnrQueueGen(String snrAmt, String usr, String tick){
		Date d = new Date();
		int count = Integer.parseInt(snrAmt);
		for(int i = 0; i<count; i++){
			File f = new File("snrQueue\\"+d.getTime()+i+"_"+usr+"_"+tick);
			try {
				f.createNewFile();
				System.out.println(f.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
