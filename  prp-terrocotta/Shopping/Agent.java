import java.util.*;

public class Agent extends DefaultBelief implements Runnable
{
	public static ArrayList<Agent> agentList = new ArrayList<Agent>();
	public static int waiting = 15;
	public static int memory = 10;

	private int row, col, tRow, tCol, rRow, rCol;
	private char tItem = ' ', rItem = ' ';
	private String status = "";
	private int frus, items, trips;

	public Agent(int id, int frus)
	{
		super(id, 0, -1);
		this.rItem = (char)(65 + (int)(Math.random() * 26));
		this.rRow = Main.maxRow - 1;
		this.rCol = Main.maxCol - 1;
		this.init(true);
		this.frus = frus;
		this.items = 0;
		this.trips = 0;
		add(this);
	}

	private void init(boolean b)
	{
		this.status = "Waiting";
		if (!b) this.clean();
		this.row = 1;
		this.col = 1 + (int)(Math.random() * 9);
		this.tItem = this.rItem;
		this.tRow = this.rRow;
		this.tCol = this.rCol;
		if (b) this.draw();
	}
	
	private synchronized static void add(Agent agent)
	{
		agentList.add(agent);
	}

	private synchronized void clean()
	{
		Main.a[this.row][this.col] = '~';
		GUI.setBtnColor(this.getID(), this.row, this.col, "");
	}

	private synchronized void draw()
	{
		Main.a[this.row][this.col] = (char)(48 + this.getID());
		GUI.setBtnColor(this.getID(), this.row, this.col, this.status);
	}

	public int getFrus()
	{
		return this.frus;
	}

	public int getItems()
	{
		return this.items;
	}

	public int getTrips()
	{
		return this.trips;
	}

	public int getRow()
	{
		return this.row;
	}

	public int getCol()
	{
		return this.col;
	}

	public int getTRow()
	{
		return this.tRow;
	}

	public int getTCol()
	{
		return this.tCol;
	}

	public int getRRow()
	{
		return this.rRow;
	}

	public int getRCol()
	{
		return this.rCol;
	}

	public char getTItem()
	{
		return this.tItem;
	}

	public char getRItem()
	{
		return this.rItem;
	}

	public void run()
	{
		while (this.getLifeCycle() == -1 || this.isNoLife())
		{
			synchronized (ClockTick.nowLock)
			{
				try {
					while (this.getTick() >= ClockTick.getTick() || ClockTick.getNow() == 0) ClockTick.nowLock.wait();
					this.addTick();
					this.shop();
					ClockTick.decNow();
				}
				catch (Exception e) { e.printStackTrace(); }
			}
		}
	}

	private void shop()
	{
		if (this.status.equals("Waiting")) this.checkWait();
		else if (this.row == 1 && this.col > 10) this.init(false);
		else if (this.status.equals("Found")) this.checkTarget();
		else if (this.status.equals("Leaving")) this.move(this.row - 1, this.col);
		else if (this.frus > (int)(Math.random() * 100)) this.moveRandomly();
		else this.moveToTarget();
		this.lookAround();
	}

	private void checkWait()
	{
		if ((int)(Math.random() * 100) < waiting) 
		{
			this.status = "Browsing";
			GUI.setBtnColor(this.getID(), this.row, this.col, this.status);
			this.trips++;
		}
	}

	private void checkTarget()
	{
		if (this.tItem == '$')
		{
			this.status = "Leaving";
			GUI.setBtnColor(this.getID(), this.row, this.col, this.status);
			this.tItem = ' ';
		}
		else {
			this.items++;
			if ((int)(Math.random() * 100) < memory)
			{
				this.rRow = this.row;
				this.rCol = this.col;
			}
			this.status = "Checkout";
			GUI.setBtnColor(this.getID(), this.row, this.col, this.status);
			this.tItem = '$';
			this.tRow = 2;
			this.tCol = Main.maxCol - 3 * (int)(Math.random() * 6);
		}
	}

	private void moveRandomly()
	{
		int dire[][] = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
		int k = (int)(Math.random() * 4);
		this.move(this.row + dire[k][0], this.col + dire[k][1]);
	}

	private void moveToTarget()
	{
		if (this.tCol == this.col && this.tRow == this.row && this.col == Main.maxCol - 1 && this.row == Main.maxRow - 1)
		{
			this.tRow = 5;
			this.tCol = 2;
		}
		else if (this.tCol == this.col && this.tRow == this.row && this.col == 2 && this.row == 5)
		{
			this.tRow = Main.maxRow - 1;
			this.tCol = Main.maxCol - 1;
		}
		else if (this.tRow == 2 && this.row < 4 && this.col < 10) this.move(this.row + 1, this.col);
		else if (this.tRow == 2 && this.row == 18 && this.col == this.tCol)
		{
			if (this.col == 24 || this.col == 15) this.move(this.row, this.col + 1);
			else this.move(this.row, this.col - 1);
		}
		else {
			int dta1 = this.tCol - this.col, dta2 = this.tRow - this.row, nextRow = this.row, nextCol = this.col;
			if (dta1 > 0) nextCol += 1;
			else if (dta1 < 0) nextCol -= 1;
			if (dta2 > 0) nextRow += 1;
			else if (dta2 < 0) nextRow -= 1;
			if (Math.random() < 0.75)
			{
				if (!this.move(this.row, nextCol)) this.move(nextRow, this.col);
			}
			else if (!this.move(nextRow, this.col)) this.move(this.row, nextCol);
		}
	}

	private void lookAround()
	{
		int dire[][] = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
		for (int k=0; k<4; k++)	
		{
			int tempX = this.row + dire[k][0], tempY = this.col + dire[k][1];
			if (tempX > 0 && tempX <= Main.maxRow && tempY > 0 && tempY <= Main.maxCol && Main.a[tempX][tempY] == this.tItem)
			{
				this.status = "Found";
				GUI.setBtnColor(this.getID(), this.row, this.col, this.status);
			}
		}
	}

	private synchronized boolean move(int nextRow, int nextCol)
	{
		if (nextRow > 0 && nextRow <= Main.maxRow && nextCol > 0 && nextCol <= Main.maxCol && Main.a[nextRow][nextCol] == '~')
		{
			this.clean();
			this.row = nextRow;
			this.col = nextCol;
			this.draw();
			this.feelBetter();
			return true;
		}
		else {
			this.feelWorse();
			return false;
		}
	}

	private void feelBetter()
	{
		if (--this.frus < 3) this.frus = 3;
	}

	private void feelWorse()
	{
		if (++this.frus > 99)
		{
			if (this.status.equals("Browsing"))
			{
				this.status = "Checkout";
				GUI.setBtnColor(this.getID(), this.row, this.col, this.status);
				this.tItem = '$';
				this.tRow = 2;
				this.tCol = Main.maxCol - 3 * (int)(Math.random() * 6);
			}
			this.frus = 100;
		}
	}
}