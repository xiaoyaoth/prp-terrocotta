package simulation.runtime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

/**
 * �ڷ������˿���������� ʵ�����׽��� �������ļ�
 * 
 * @author
 */
public class SendFile extends Thread {

	String remoteIPString = null;
	int port;
	Socket tempSocket;
	OutputStream os;
	RandomAccessFile outFile;
	byte byteBuffer[] = new byte[1024];

	public SendFile(String remoteIPString, int port, File file) {
		try {
			this.remoteIPString = remoteIPString;
			this.port = port;
			outFile = new RandomAccessFile(file, "r");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			this.tempSocket = new Socket(this.remoteIPString, this.port);
			//System.out.println("����������ӳɹ�!");
			os = tempSocket.getOutputStream();

			int amount;
			//System.out.println("��ʼ�����ļ�...");
			while ((amount = outFile.read(byteBuffer)) != -1) {
				os.write(byteBuffer, 0, amount);
				//System.out.println("�ļ�������...");
			}
			//System.out.println("Send File complete");
			outFile.close();
			os.close();
			tempSocket.close();
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		SendFile sf1 = new SendFile("127.0.0.1", 10000, new File("agentsOut//a"));
		SendFile sf2 = new SendFile("127.0.0.1", 10000, new File("agentsOut//b"));
		SendFile sf3 = new SendFile("127.0.0.1", 10000, new File("agentsOut//c"));
		SendFile sf4 = new SendFile("127.0.0.1", 10000, new File("agentsOut//d"));
		sf1.start();
		sf2.start();
		sf3.start();
		sf4.start();
	}
}