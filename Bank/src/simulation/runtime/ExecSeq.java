package simulation.runtime;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecSeq {
	public static void main(String args[]) {
		String[] clientsName = { "Client1.bat", "Client2.bat", "Client3.bat", "Client4.bat",
				"Client5.bat", "Client6.bat", "Clientn1.bat", "Clientn2.bat", "Clientn3.bat",
				"Clientn4.bat" };
		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep((int) Math.random() * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ClientBat cb = new ClientBat(clientsName[i]);
			new Thread(cb).run();
		}
	}
}

class ClientBat implements Runnable {
	private String name;

	ClientBat(String name) {
		this.name = name;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Process p = Runtime.getRuntime().exec(this.name);
			InputStream stderr = p.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
