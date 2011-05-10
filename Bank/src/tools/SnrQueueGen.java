package tools;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class SnrQueueGen {
	
	public static void main(String[] args){
		Date d = new Date();
		for(int i = 0; i<10; i++){
			File f = new File("snrQueue\\"+d.getTime()+i+"_"+args[0]+"_100");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
