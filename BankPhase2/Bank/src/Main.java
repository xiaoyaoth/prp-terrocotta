import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.sun.java_cup.internal.runtime.Scanner;

import simulation.modeling.DefaultBelief;
import simulation.runtime.CPU;
import simulation.runtime.MEM;


public class Main {
	public static ArrayList<String> ss = new ArrayList<String>();
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		/*Bank b = new Bank();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(b);
		out.flush();


		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bin);
		Object newAg = in.readObject();
		DefaultBelief ag = (DefaultBelief) newAg;
		System.out.println(ag.getClass());*/
		System.out.println(System.getProperty("java.library.path"));
		int t = 1;
		int cpu = 0;
		int temp = 0;
		while(true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			temp = CPU.INSTANCE.getCpuUsage();
			if(temp<=100){
				cpu += temp;
				System.out.println("CPU Usage: "+cpu/t);
				t++;
			}			
		}
		
	}
}
