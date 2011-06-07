package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class StatReader {
	public static void main(String[] args) throws IOException{
		File dir = new File("statistics\\¶à»útest1");
		if(!dir.isDirectory()){
			System.out.println("end");
			return;
		}else{
			File[] flist = dir.listFiles();
			for(File f:flist){
				FileReader fr = new FileReader(f);
				BufferedReader br = new BufferedReader(fr);
				System.out.println(br.readLine());
			}
		}
	}
}
