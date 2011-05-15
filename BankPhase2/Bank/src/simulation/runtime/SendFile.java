package simulation.runtime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Scanner;

/**
 * 在服务器端开启的情况下 实例化套接字 并发送文件
 * 
 * @author
 */
public class SendFile extends Thread {

	String remoteIPString = null;
	int port;
	//Socket tempSocket;
	//OutputStream os;
	//RandomAccessFile outFile;
	File file;
	byte byteBuffer[] = new byte[1024];

	public SendFile(String remoteIPString, int port, File file) {
		try {
			//System.out.println(remoteIPString + port);
			this.remoteIPString = remoteIPString;
			this.port = port;
			this.file = file;
			//outFile = new RandomAccessFile(file, "r");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			RandomAccessFile outFile = new RandomAccessFile(this.file, "r");
			System.out.println("sendfile " + this.file.getName());
			Socket tempSocket = new Socket(this.remoteIPString, this.port);
			//System.out.println("与服务器连接成功!");
			OutputStream os = tempSocket.getOutputStream();

			int amount;
			//System.out.println("开始发送文件...");
			while ((amount = outFile.read(byteBuffer)) != -1) {
				os.write(byteBuffer, 0, amount);
				//System.out.println("文件发送中...");
			}
			//System.out.println("Send File complete");
			outFile.close();
			os.close();
			tempSocket.close();
			this.file.delete();
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		String ip = in.next();
		SendFile sf1 = new SendFile(ip, 10000, new File("agentsOut//a"));
		SendFile sf2 = new SendFile(ip, 10000, new File("agentsOut//b"));
		SendFile sf3 = new SendFile(ip, 10000, new File("agentsOut//c"));
		SendFile sf4 = new SendFile(ip, 10000, new File("agentsOut//d"));
		sf1.start();
		sf2.start();
		sf3.start();
		sf4.start();
	}
}