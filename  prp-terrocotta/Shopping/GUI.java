import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

//import Parse.Agent;

//import Parse.Client;

import java.util.*;

/*
 *       <additional-boot-jar-classes>
 <include>javax.swing.JButton</include>
 </additional-boot-jar-classes>
 */
public class GUI extends JFrame implements ActionListener, CaretListener {
	JButton btnList[][];
	int incX = 26, incY = 26, x1 = 19, y1 = 19;
	JLabel lb1 = new JLabel("请输入仿真步数：");
	JLabel lb2 = new JLabel("Tick：0");
	JTextField tf = new JTextField();
	JButton startBtn = new JButton("开始仿真");
	String tfVal = "1000";
	ServerMachine oneCase;

	GUI(int agentNum) {
		btnList = new JButton[Main.maxRow + 2][Main.maxCol + 2];
		//clk = new ClockTick(one);
		Container c = this.getContentPane();
		c.setLayout(null);

		for (int i = 0; i <= Main.maxRow; i++) {
			for (int j = 0; j <= Main.maxCol; j++) {
				btnList[i][j] = new JButton();
				if (Main.a[i][j] == '~')
					btnList[i][j].setBackground(Color.white);
				else if (Main.a[i][j] == '*')
					btnList[i][j].setBackground(Color.black);
				else {
					btnList[i][j].setBackground(Color.lightGray);
					btnList[i][j].setText("" + Main.a[i][j]);
				}
				btnList[i][j].setBounds(y1 + j * incY, x1 + i * incX, incY,
						incX);
				btnList[i][j].setEnabled(false);
				btnList[i][j].setMargin(new java.awt.Insets(0, 0, 0, 0));
				btnList[i][j].addActionListener(this);
				c.add(btnList[i][j]);
			}
			btnList[i][Main.maxCol + 1] = new JButton();
			btnList[i][Main.maxCol + 1].setBackground(Color.black);
			btnList[i][Main.maxCol + 1].setBounds(
					y1 + (Main.maxCol + 1) * incY, x1 + i * incX, incY, incX);
			btnList[i][Main.maxCol + 1].setEnabled(false);
			btnList[i][Main.maxCol + 1].setMargin(new java.awt.Insets(0, 0, 0,
					0));
			btnList[i][Main.maxCol + 1].addActionListener(this);
			c.add(btnList[i][Main.maxCol + 1]);
		}
		for (int j = 0; j <= Main.maxCol + 1; j++) {
			btnList[Main.maxRow + 1][j] = new JButton();
			btnList[Main.maxRow + 1][j].setBackground(Color.black);
			btnList[Main.maxRow + 1][j].setBounds(y1 + j * incY, x1
					+ (Main.maxRow + 1) * incX, incY, incX);
			btnList[Main.maxRow + 1][j].setEnabled(false);
			btnList[Main.maxRow + 1][j].setMargin(new java.awt.Insets(0, 0, 0,
					0));
			btnList[Main.maxRow + 1][j].addActionListener(this);
			c.add(btnList[Main.maxRow + 1][j]);
		}

		lb1.setBounds(150, 610, 120, incX);
		this.add(lb1);
		tf.setBounds(280, 610, 120, incX);
		tf.setText("1000");
		tf.addCaretListener(this);
		this.add(tf);
		startBtn.addActionListener(this);
		startBtn.setBounds(430, 610, 100, incX);
		this.add(startBtn);
		lb2.setBounds(560, 610, 100, incX);
		this.add(lb2);

		this.setBounds(100, 20, 800, 700);
		this.setTitle("案例：Agent Shopping");
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.validate();
	}

	public void setBtnColor(int id, int x, int y, String status) {
		JButton btn = btnList[x][y];
		// System.out.println("id "+id+" x "+x+" y "+y+" status "+status);
		// System.out.println("btn " + btn);
		btn.setEnabled(true);
		btn.setText("" + id);
		if (status.equals("Browsing")) {
			btn.setBackground(Color.red);
			btn.setVisible(true);
		} else if (status.equals("Leaving"))
			btn.setBackground(Color.green);
		else if (status.equals("Checkout"))
			btn.setBackground(Color.blue);
		else if (status.equals("Found"))
			btn.setBackground(Color.yellow);
		else {
			btn.setText("");
			btn.setBackground(Color.white);
			btn.setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startBtn) {
			if (startBtn.getLabel().equals("开始仿真")) {
				System.out.println("\nSimulation Started...");
				oneCase.clk.incLeft(Integer.parseInt(tfVal));
				new Thread(oneCase.clk).start();
				startBtn.setLabel("暂停仿真");
			} else if (startBtn.getLabel().equals("暂停仿真")) {
				System.out.println("\nSimulation Paused...");
				oneCase.clk.goOn = false;
				startBtn.setLabel("继续仿真");
			} else if (startBtn.getLabel().equals("继续仿真")) {
				System.out.println("\nSimulation Continued...");
				oneCase.clk.goOn = true;
				startBtn.setLabel("暂停仿真");
			}
		} else
			for (int i = 1; i <= Main.maxRow; i++)
				for (int j = 1; j <= Main.maxCol; j++)
					if (e.getSource() == btnList[i][j])
						;
		// new ShowAgent(Agent.agentList.get(Integer
		// .parseInt(btnList[i][j].getText())));
	}

	public void caretUpdate(CaretEvent e) {
		if (e.getSource() == tf)
			tfVal = tf.getText();
	}

//	public static void setColor(ServerMachine one) {
//		System.out.println("one " + one);
//		for (int i = 0; i < 10; i++) {
//			Agent ag = one.map.get(i);
//			if (ag != null) {
////				GUI.setBtnColor(one.map.get(i).getID(), one.map.get(i).row,
////						one.map.get(i).col, one.map.get(i).status);
//			}
//		}
//	}

	public static void main(String[] args) {
		//Scanner input = new Scanner(System.in);
		System.out.println("Waiting for all clients to start");
		int agentNum = 10, frus = 25;
		if (args.length == 1)
			agentNum = Integer.parseInt(args[0]);
		if (args.length == 2)
			frus = Integer.parseInt(args[1]);
		if (args.length == 3)
			Agent.waiting = Integer.parseInt(args[2]);
		if (args.length == 4)
			Agent.memory = Integer.parseInt(args[3]);
		
		ServerMachine oneServer = new ServerMachine();		
		GUI gui = new GUI(agentNum);
		gui.oneCase = oneServer;
		oneServer.gui = gui;
		
		for (int i = 0; i < agentNum; i++) {
			ArrayList<Integer> oneTuple = new ArrayList<Integer>();
			Integer jvm_id = (int) (Math.random() * ClientMachine.JVM_counter);
			oneTuple.add(jvm_id);
			oneTuple.add(i);
			oneTuple.add(frus);
			oneServer.caseTable.add(oneTuple);
		}
		synchronized (ClientMachine.cases) {
			ClientMachine.cases.add(oneServer);
		}
		oneServer.conv_fini = true;
		// System.out.println("check the agent, print \'y\'");
		/*
		 * while (input.next().equals("y")) {
		 * System.out.println("Input the \'id\' to direct to an agent"); String
		 * id = input.next(); Agent ag = oneServer.map.get(id); try {
		 * System.out.println(ag + " HashCode:" + ag.hashCode()); } catch
		 * (Exception e) { e.printStackTrace(); }
		 * System.out.println("check the agent, print \'y\'"); }
		 */
		// for (int i=0; i<agentNum; i++) new Agent(i, frus);
		// for (int i=0; i<agentNum; i++) new
		// Thread(Agent.agentList.get(i)).start();
	}
}