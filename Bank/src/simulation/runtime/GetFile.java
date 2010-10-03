package simulation.runtime;

/*服务器端接收文件*/
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

public class GetFile extends Thread {
	private static ServerSocket serSocket;
	int defaultBindPort = 10000;
	int tryBindTimes = 0;
	//int currentBindPort = defaultBindPort + tryBindTimes;

	public static void main(String args[]) {
		GetFile getFile = null;
		try {
			getFile = new GetFile(10000);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("无法传送文件!");
			System.exit(1);
		}
		getFile.start();
	}

	public GetFile(int port) {
		try {
			//this.currentBindPort = port;
			serSocket = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
			//throw new Exception("绑定端口不成功!");
		}
	}

	public void run() {
		while (true) {
			try {
				Socket tempSocket = serSocket.accept();
				new Thread(new DealWithReq(tempSocket)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

final class DealWithReq implements Runnable {
	Socket tempSocket;
	InputStream inSocket;
	RandomAccessFile inFile = null;
	byte byteBuffer[] = new byte[1024];
	private final String IN_AGENT_FOLDER = "agentsIn//";
	private static int filecounter;

	public DealWithReq(Socket s) throws IOException {
		this.tempSocket = s;
		this.inSocket = s.getInputStream();
	}

	public void run() {
		try {
			File f = new File(this.IN_AGENT_FOLDER + filecounter++);
			this.inFile = new RandomAccessFile(f, "rw");
			System.out.println("wait for..." + '\n' + "等待对方接入");
		} catch (Exception ex) {
			System.out.println(ex.toString());
			ex.printStackTrace();
			return;
		}

		int amount;
		try {
			while ((amount = inSocket.read(byteBuffer)) != -1) {
				inFile.write(byteBuffer, 0, amount);
			}
			inSocket.close();
			System.out.println("Get OK");
			System.out.println("接收完毕!");
			inFile.close();
			tempSocket.close();
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
}