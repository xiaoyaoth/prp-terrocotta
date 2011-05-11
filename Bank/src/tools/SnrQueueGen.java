package tools;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class SnrQueueGen {
	
	public static void main(String[] args){
		Date d = new Date();
		int count = Integer.parseInt(args[0]);
		for(int i = 0; i<count; i++){
			File f = new File("snrQueue\\"+d.getTime()+i+"_"+args[1]+"_100");
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
