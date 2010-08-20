import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.sun.java_cup.internal.runtime.Scanner;

import simulation.modeling.DefaultBelief;


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
	}
}
