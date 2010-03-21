import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ShowAgent extends JFrame implements ActionListener
{
	JLabel lb1, lb2, lb3;
	JButton btn = new JButton("关闭");

	public ShowAgent(Agent ag)
	{
		Container c = this.getContentPane();
		c.setLayout(null);
		int x1 = 50, y1 = 20, incY = 30, x2 = 260;
		
		lb1 = new JLabel("物品：" + ag.getRItem() + "，大致位置（" + ag.getRRow() + ", " + ag.getRCol() + "）");
		lb1.setBounds(x1, y1, x2, incY - 5);
		c.add(lb1);
		y1 += incY;
		lb2 = new JLabel("目标：" + ag.getTItem() + "，向着（" + ag.getTRow() + ", " + ag.getTCol() + "）前进");
		lb2.setBounds(x1, y1, x2, incY - 5);
		c.add(lb2);
		y1 += incY;
		lb3 = new JLabel("烦躁：" + ag.getFrus() + "，已购：" + ag.getItems() + "，出行：" + ag.getTrips());
		lb3.setBounds(x1, y1, x2, incY - 5);
		c.add(lb3);
		y1 += 2 * incY;
        btn.setBounds(100, y1, 80, incY - 5);
		btn.addActionListener(this);
		c.add(btn);

		this.setBounds(100, 100, 300, 240);
		this.setTitle("Agent" + ag.getID() + "：（" + ag.getRow() + ", " + ag.getCol() + "）");
		this.setVisible(true);
		this.validate();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == btn) this.setVisible(false);
	}
}