package simulation.runtime;

/*服务器端接收文件*/
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

public class GetFile extends Thread {
	private ServerSocket serSocket;
	int defaultBindPort = 10000;
	int tryBindTimes = 0;

	// int currentBindPort = defaultBindPort + tryBindTimes;

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
			serSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			try {
				//System.out.println("&&&&&&&&&&&&& Accept New File &&&&&&&&&&&&&&&&&");
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
	InputStream is;
	RandomAccessFile inFile = null;
	byte byteBuffer[] = new byte[1024];
	private final String IN_AGENT_FOLDER = "agentsIn//";
	private static int filecounter;

	public DealWithReq(Socket s) throws IOException {
		this.tempSocket = s;
	}

	public void run() {
		File f;
		try {
			f = new File(this.IN_AGENT_FOLDER + filecounter++);
			this.inFile = new RandomAccessFile(f, "rw");
			//System.out.println("wait for..." + '\n' + "等待对方接入");
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		int amount;
		try {
			this.is = this.tempSocket.getInputStream();
			while ((amount = is.read(byteBuffer)) != -1) {
				inFile.write(byteBuffer, 0, amount);
			}
			is.close();
			inFile.close();
			tempSocket.close();
			//f.delete();
			//System.out.println("接收完毕!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}