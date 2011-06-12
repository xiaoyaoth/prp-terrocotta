package simulation.runtime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Scanner;

/**
 * �ڷ������˿���������� ʵ�����׽��� �������ļ�
 * 
 * @author
 */
public class SendFile extends Thread {

	String remoteIPString = null;
	File file;
	byte byteBuffer[] = new byte[1024];
	private static final int PORT=10000;
	private static final String OUTFOLDER = "agentsOut\\";

	public SendFile(String remoteIPString, File file) {
		try {
			this.remoteIPString = remoteIPString;
			this.file = file;
			this.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			RandomAccessFile outFile = new RandomAccessFile(this.file, "r");
			System.out.println("sendfile " + this.file.getName());
			Socket tempSocket = new Socket(this.remoteIPString, PORT);
			//System.out.println("����������ӳɹ�!");
			OutputStream os = tempSocket.getOutputStream();

			int amount;
			//System.out.println("��ʼ�����ļ�...");
			while ((amount = outFile.read(byteBuffer)) != -1) {
				os.write(byteBuffer, 0, amount);
				os.flush();
				//System.out.println("�ļ�������...");
			}
			//System.out.println("Send File complete");
			outFile.close();
			os.close();
			tempSocket.close();
			//this.file.delete();
			this.file.renameTo(new File(OUTFOLDER+"fini"+this.file.getName()));
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		String ip = in.next();
		SendFile sf1 = new SendFile(ip, new File("agentsOut//a"));
		SendFile sf2 = new SendFile(ip, new File("agentsOut//b"));
		SendFile sf3 = new SendFile(ip, new File("agentsOut//c"));
		SendFile sf4 = new SendFile(ip, new File("agentsOut//d"));
		sf1.start();
		sf2.start();
		sf3.start();
		sf4.start();
	}
}