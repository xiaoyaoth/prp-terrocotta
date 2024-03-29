package simulation.runtime;

import simulation.modeling.*;

import java.util.*;

public class Agent extends DefaultBelief {
	
	public static int waiting = 15;
	public static int memory = 10;

	private int row, col, tRow, tCol, rRow, rCol;
	private char tItem = ' ', rItem = ' ';
	private String status = "";
	private int frus, items, trips;
	
	public ArrayList<PlanCondition> pc = new ArrayList<PlanCondition>();
	private int pCounter = 0;

	public Agent(Integer frus) {
		this.rItem = (char) (65 + (int) (Math.random() * 26));
		this.rRow = Map.maxRow - 1;
		this.rCol = Map.maxCol - 1;
		this.init(true);
		this.frus = frus;
		this.items = 0;
		this.trips = 0;
	}
	
	/* �Դ�Action */
	public void addPC(PlanCondition newPC)
	{
		newPC.setID(++pCounter);
		pc.add(newPC);
	}

	public int getPCIndex(int pcID)
	{
		for (int i=0; i<pc.size(); i++) if (pc.get(i).getID() == pcID) return i;
		return -1;
	}

	public int getPCIndex(String pn)
	{
		for (int i=0; i<pc.size(); i++) if (pc.get(i).getPlanName().equals(pn)) return i;
		return -1;
	}

	public PlanCondition getPC(int index)
	{
		return pc.get(index);
	}
	/* �Դ�Action */

	private void init(boolean b) {
		this.status = "Waiting";
		if (!b)
			this.clean();
		this.row = 1;
		this.col = 1 + (int) (Math.random() * 9);
		this.tItem = this.rItem;
		this.tRow = this.rRow;
		this.tCol = this.rCol;
		if (b)
			this.draw();
	}

	// private synchronized static void add(Agent agent)
	// {
	// agentList.add(agent);
	// }

	private synchronized void clean() {
		Map.a[this.row][this.col] = '~';
		// gui.setBtnColor(this.getID(), this.row, this.col, "");
	}

	private synchronized void draw() {
		Map.a[this.row][this.col] = (char) (48 + this.getID());
		// System.out.println(gui);
		// gui.setBtnColor(this.getID(), this.row, this.col, this.status);
	}

	public int getFrus() {
		return this.frus;
	}

	public int getItems() {
		return this.items;
	}

	public int getTrips() {
		return this.trips;
	}

	public int getRow() {
		return this.row;
	}

	public int getCol() {
		return this.col;
	}

	public int getTRow() {
		return this.tRow;
	}

	public int getTCol() {
		return this.tCol;
	}

	public int getRRow() {
		return this.rRow;
	}

	public int getRCol() {
		return this.rCol;
	}

	public char getTItem() {
		return this.tItem;
	}

	public char getRItem() {
		return this.rItem;
	}

	/* Kernel */
	public void run()
	{
		while (this.getLifeCycle() == -1 || this.isNoLife())
		{
			synchronized (this.main.getClock().nowLock)
			{
				try {
					while (this.getTick() >= this.main.getClock().getTick() || this.main.getClock().getNow() == 0)
						this.main.getClock().nowLock.wait();
					this.addTick();
					this.createPlans();
					this.submitPlans();
					this.main.getClock().decNow();
				}
				catch (Exception e) { e.printStackTrace(); }
			}
		}
	}

	private void createPlans()
	{
		this.receiveMessages();
		this.sendMessages();
		for (int i=0; i<pc.size(); i++)
		{
			PlanCondition tpc = pc.get(i);
			if (tpc.getInterval() > 0 && this.getOwnTick() % tpc.getInterval() == 0)
			{
				PlanInstance pi = new PlanInstance(this.getID(), tpc.getID(), tpc.getNeedTicks(), tpc.getPlanName());
				pi.setSize(0);
				this.addPlanInstance(pi);
			}
		}
	}

	private void receiveMessages()
	{
		for (int i=0; i<this.rcvMessageBox.size(); i++)
		{
			MessageInfo mi = this.rcvMessageBox.get(i);
			if (!mi.getRFlag())
			{
				mi.setRFlag();
				String temp = mi.getContent();
				int kh = temp.indexOf("(");
				if (kh > -1)
				{
					String pn = temp.substring(0, kh);
					int pIndex = getPCIndex(pn);
					if (pIndex > -1)
					{
						PlanCondition tpc = pc.get(pIndex);
						PlanInstance pi = new PlanInstance(this.getID(), tpc.getID(), tpc.getNeedTicks(), pn);
						temp = temp.substring(1 + kh);
						int dh = temp.indexOf(", "), yh = temp.indexOf(")");
						if (yh != kh + 1)
						{
							ArrayList<String> al = new ArrayList<String>();
							while (dh > -1)
							{
								al.add(temp.substring(0, dh));
								temp = temp.substring(dh + 2);
								dh = temp.indexOf(", ");
							}
							al.add(temp.substring(0, temp.indexOf(")")));
							pi.setSize(al.size());
							for (int j=0; j<al.size(); j++) pi.setPara(j, Integer.parseInt(al.get(j)));
						}
						else pi.setSize(0);
						this.addPlanInstance(pi);
					}
				}
			}
		}
	}

	private void sendMessages()
	{
		for (int i=0; i<this.sndMessageBox.size(); i++)
		{
			MessageInfo mi = this.sndMessageBox.get(i);
			if (!mi.getSFlag())
			{
				mi.setSFlag();
				mi.getRcv().addMess(false, mi);
			}
		}
	}
	/* Kernel */
	
	public void shop() {
//		frusList.add(this.getFrus());
		System.out.println("ID:" + this.getID() + " frustration:"
				+ this.getFrus() + " Item:" + this.getTItem() + " ");
		
		if (this.status.equals("Waiting"))
			this.checkWait();
		else if (this.row == 1 && this.col > 10)
			this.init(false);
		else if (this.status.equals("Found"))
			this.checkTarget();
		else if (this.status.equals("Leaving"))
			this.move(this.row - 1, this.col);
		else if (this.frus > (int) (Math.random() * 100))
			this.moveRandomly();
		else
			this.moveToTarget();
		this.lookAround();
	}

	private void checkWait() {
		if ((int) (Math.random() * 100) < waiting) {
			this.status = "Browsing";
			// gui.setBtnColor(this.getID(), this.row, this.col, this.status);
			this.trips++;
		}
	}

	private void checkTarget() {
		if (this.tItem == '$') {
			this.status = "Leaving";
			ArrayList<Shopper> spList = this.main.getAgentList(Shopper.class);
			for (int i=0; i<spList.size(); i++)
			{
				Shopper sp = spList.get(i);
				if (sp.getCol() == this.col)
				{
					sp.addCash((int)(Math.random() * 100));
				}
			}
			// gui.setBtnColor(this.getID(), this.row, this.col, this.status);
			this.tItem = ' ';
		} else {
			this.items++;
			if ((int) (Math.random() * 100) < memory) {
				this.rRow = this.row;
				this.rCol = this.col;
			}
			this.status = "Checkout";
			// gui.setBtnColor(this.getID(), this.row, this.col, this.status);
			this.tItem = '$';
			this.tRow = 2;
			this.tCol = Map.maxCol - 3 * (int) (Math.random() * 6);
		}
	}

	private void moveRandomly() {
		int dire[][] = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
		int k = (int) (Math.random() * 4);
		this.move(this.row + dire[k][0], this.col + dire[k][1]);
	}

	private void moveToTarget() {
		if (this.tCol == this.col && this.tRow == this.row
				&& this.col == Map.maxCol - 1 && this.row == Map.maxRow - 1) {
			this.tRow = 5;
			this.tCol = 2;
		} else if (this.tCol == this.col && this.tRow == this.row
				&& this.col == 2 && this.row == 5) {
			this.tRow = Map.maxRow - 1;
			this.tCol = Map.maxCol - 1;
		} else if (this.tRow == 2 && this.row < 4 && this.col < 10)
			this.move(this.row + 1, this.col);
		else if (this.tRow == 2 && this.row == 18 && this.col == this.tCol) {
			if (this.col == 24 || this.col == 15)
				this.move(this.row, this.col + 1);
			else
				this.move(this.row, this.col - 1);
		} else {
			int dta1 = this.tCol - this.col, dta2 = this.tRow - this.row, nextRow = this.row, nextCol = this.col;
			if (dta1 > 0)
				nextCol += 1;
			else if (dta1 < 0)
				nextCol -= 1;
			if (dta2 > 0)
				nextRow += 1;
			else if (dta2 < 0)
				nextRow -= 1;
			if (Math.random() < 0.75) {
				if (!this.move(this.row, nextCol))
					this.move(nextRow, this.col);
			} else if (!this.move(nextRow, this.col))
				this.move(this.row, nextCol);
		}
	}

	private void lookAround() {
		int dire[][] = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
		for (int k = 0; k < 4; k++) {
			int tempX = this.row + dire[k][0], tempY = this.col + dire[k][1];
			if (tempX > 0 && tempX <= Map.maxRow && tempY > 0
					&& tempY <= Map.maxCol
					&& Map.a[tempX][tempY] == this.tItem) {
				this.status = "Found";
				// gui.setBtnColor(this.getID(), this.row, this.col,
				// this.status);
			}
		}
	}

	private synchronized boolean move(int nextRow, int nextCol) {
		if (nextRow > 0 && nextRow <= Map.maxRow && nextCol > 0
				&& nextCol <= Map.maxCol && Map.a[nextRow][nextCol] == '~') {
			this.clean();
			this.row = nextRow;
			this.col = nextCol;
			this.draw();
			this.feelBetter();
			return true;
		} else {
			this.feelWorse();
			return false;
		}
	}

	private void feelBetter() {
		if (--this.frus < 3)
			this.frus = 3;
	}

	private void feelWorse() {
		if (++this.frus > 99) {
			if (this.status.equals("Browsing")) {
				this.status = "Checkout";
				// gui.setBtnColor(this.getID(), this.row, this.col,
				// this.status);
				this.tItem = '$';
				this.tRow = 2;
				this.tCol = Map.maxCol - 3 * (int) (Math.random() * 6);
			}
			this.frus = 100;
		}
	}
}