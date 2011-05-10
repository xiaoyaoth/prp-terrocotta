package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ScenarioGen {
	public static void main(String[] args) {
		String filename = "snr.txt";
		int num = 1000;
		try {
			File file = new File(filename);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			String temp = "<?xml version=\"1.0\"?>\n" + "<snr>\n"
					+ "<sln>sln02</sln>\n" + "<Type val=\"scenario\"/>\n"
					+ "<Description val=\"\"/>\n" + "<IsTree val=\"false\"/>\n"
					+ "<RoleInfos>\n" + "<RoleInfo name=\"Bank\"/>\n"
					+ "<RoleInfo name=\"Customer\"/>\n" + "</RoleInfos>\n"
					+ "<Parts>\n" + "<Part>\n"
					+ "<Info name=\"Pt\" description=\"\"/>\n" + "<Roles>\n"
					+ "<Role index=\"0\">\n"
					+ "<Instances instanceNum=\"2\">\n";
			bw.write(temp);
			bw.flush();
			for (int i = 0; i < num; i++) {
				temp = "<Instance id=\"" + i + "\">\n"
						+ "<cash value=\"1000\"/>\n" + "</Instance>\n";
				bw.write(temp);
				bw.flush();
			}
			temp = "</Instances>\n" + "</Role>\n" + "<Role index=\"1\">\n"
					+ "<Instances instanceNum=\"4\">\n";
			bw.write(temp);
			bw.flush();
			for (int i = 0; i < num; i++) {
				int j = i + num;
				temp = "<Instance id=\"" + j + "\">\n"
						+ "<cash value=\"100\"/>\n" + "</Instance>\n";
				bw.write(temp);
				bw.flush();
			}
			temp = "</Instances>\n" + "</Role>\n" + "</Roles>\n" + "<Sons>\n"
					+ "</Sons>\n" + "</Part>\n" + "</Parts>\n" + "<Groups>\n"
					+ "</Groups>\n" + "<Influences></Influences></snr>";
			bw.write(temp);
			bw.flush();
			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
