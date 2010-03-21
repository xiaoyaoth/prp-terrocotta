import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class GUI extends JFrame implements ActionListener, CaretListener
{
	static JButton btnList[][] = new JButton[Main.maxRow + 2][Main.maxCol + 2];
	static int incX = 26, incY = 26, x1 = 19, y1 = 19;
	static JLabel lb1 = new JLabel("请输入仿真步数：");
	static JLabel lb2 = new JLabel("Tick：0");
	static JTextField tf = new JTextField();
	static JButton startBtn = new JButton("开始仿真");
	static ClockTick clk;
	static String tfVal = "1000";

	public GUI(int agentNum)
	{
		clk = new ClockTick();
		Container c = this.getContentPane();
		c.setLayout(null);

		for (int i=0; i<=Main.maxRow; i++)
		{
			for (int j=0; j<=Main.maxCol; j++)
			{
				btnList[i][j] = new JButton();
				if (Main.a[i][j] == '~') btnList[i][j].setBackground(Color.white);
				else if (Main.a[i][j] == '*') btnList[i][j].setBackground(Color.black);
				else {
					btnList[i][j].setBackground(Color.lightGray);
					btnList[i][j].setText("" + Main.a[i][j]);
				}
				btnList[i][j].setBounds(y1 + j * incY, x1 + i * incX, incY, incX);
				btnList[i][j].setEnabled(false);
				btnList[i][j].setMargin(new java.awt.Insets(0, 0, 0, 0)); 
				btnList[i][j].addActionListener(this);
				c.add(btnList[i][j]);
			}
			btnList[i][Main.maxCol + 1] = new JButton();
			btnList[i][Main.maxCol + 1].setBackground(Color.black);
			btnList[i][Main.maxCol + 1].setBounds(y1 + (Main.maxCol + 1) * incY, x1 + i * incX, incY, incX);
			btnList[i][Main.maxCol + 1].setEnabled(false);
			btnList[i][Main.maxCol + 1].setMargin(new java.awt.Insets(0, 0, 0, 0)); 
			btnList[i][Main.maxCol + 1].addActionListener(this);
			c.add(btnList[i][Main.maxCol + 1]);
		}
		for (int j=0; j<=Main.maxCol+1; j++) 
		{
			btnList[Main.maxRow + 1][j] = new JButton();
			btnList[Main.maxRow + 1][j].setBackground(Color.black);
			btnList[Main.maxRow + 1][j].setBounds(y1 + j * incY, x1 + (Main.maxRow + 1) * incX, incY, incX);
			btnList[Main.maxRow + 1][j].setEnabled(false);
			btnList[Main.maxRow + 1][j].setMargin(new java.awt.Insets(0, 0, 0, 0)); 
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

	public static void setBtnColor(int id, int x, int y, String status)
	{
		JButton btn = btnList[x][y];
		btn.setEnabled(true);
		btn.setText("" + id);
		if (status.equals("Browsing")) 
		{
			btn.setBackground(Color.red);
			btn.setVisible(true);
		}
		else if (status.equals("Leaving")) btn.setBackground(Color.green);
		else if (status.equals("Checkout")) btn.setBackground(Color.blue);
		else if (status.equals("Found")) btn.setBackground(Color.yellow);
		else {
			btn.setText("");
			btn.setBackground(Color.white);
			btn.setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == startBtn)
		{
			if (startBtn.getLabel().equals("开始仿真"))
			{
				System.out.println("\nSimulation Started...");
				clk.incLeft(Integer.parseInt(tfVal));
				new Thread(clk).start();
				startBtn.setLabel("暂停仿真");
			}
			else if (startBtn.getLabel().equals("暂停仿真"))
			{
				System.out.println("\nSimulation Paused...");
				clk.goOn = false;
				startBtn.setLabel("继续仿真");
			}
			else if (startBtn.getLabel().equals("继续仿真"))
			{
				System.out.println("\nSimulation Continued...");
				clk.goOn = true;
				startBtn.setLabel("暂停仿真");
			}
		}
		else for (int i=1; i<=Main.maxRow; i++) for (int j=1; j<=Main.maxCol; j++) if (e.getSource() == btnList[i][j])
			 new ShowAgent(Agent.agentList.get(Integer.parseInt(btnList[i][j].getText())));
	}

	public void caretUpdate(CaretEvent e)
	{
		if (e.getSource() == tf) tfVal = tf.getText();
	}
	
	public static void main(String[] args)
	{
		int agentNum = 10, frus = 25;
		if (args.length == 1) agentNum = Integer.parseInt(args[0]);
		if (args.length == 2) frus = Integer.parseInt(args[1]);
		if (args.length == 3) Agent.waiting = Integer.parseInt(args[2]);
		if (args.length == 4) Agent.memory = Integer.parseInt(args[3]);
		new GUI(agentNum);
		for (int i=0; i<agentNum; i++) new Agent(i, frus);
		for (int i=0; i<agentNum; i++) new Thread(Agent.agentList.get(i)).start();
	}
}