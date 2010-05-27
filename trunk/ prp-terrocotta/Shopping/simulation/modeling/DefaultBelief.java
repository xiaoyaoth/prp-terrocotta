package simulation.modeling;

import java.util.*;

public class DefaultBelief extends PlanManager implements Runnable
{
	private int id, tick = 0, lifeCycle = -1, ownTick = 0;
	protected MainInterface main;
	public ArrayList<MessageInfo> sndMessageBox = new ArrayList<MessageInfo>();
	public ArrayList<MessageInfo> rcvMessageBox = new ArrayList<MessageInfo>();
	private ArrayList<Integer> connectIDs = new ArrayList<Integer>();

	public DefaultBelief()
	{
		this.setSub(this);
	}
	
	public void addPC(PlanCondition newPC)
	{}

	/* Default Action */
	public void run() {}

	public void setMain(MainInterface main)
	{
		this.main = main;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public int getID()
	{
		return this.id;
	}

	public int getOwnTick()
	{
		return this.ownTick;
	}

	public void setLifeCycle(int lifeCycle)
	{
		this.lifeCycle = lifeCycle;
	}

	public int getLifeCycle()
	{
		return this.lifeCycle;
	}

	public boolean isNoLife()
	{
		return --this.lifeCycle >= 0;
	}

	public void addTick()
	{
		this.tick++;
		this.ownTick++;
	}

	public void setTick(int tick)
	{
		this.tick = tick;
	}

	public int getTick()
	{
		return this.tick;
	}

	public void addMess(boolean flag, MessageInfo mi)
	{
		if (flag) this.sndMessageBox.add(mi);
		else this.rcvMessageBox.add(mi);
	}

	public void removeMess(boolean flag, int index)
	{
		if (flag) this.sndMessageBox.remove(index);
		else this.rcvMessageBox.remove(index);
	}

	public void addConn(int id)
	{
		this.connectIDs.add(id);
	}

	public void removeConn(int index)
	{
		this.connectIDs.remove(index);
	}

	public int getConn(int index)
	{
		return this.connectIDs.get(index);
	}

	public int getConnIndex(int id)
	{
		for (int i=0; i<this.connectIDs.size(); i++) if (this.connectIDs.get(i) == id) return i;
		return -1;
	}

	public String toString()
	{
		return "Agent" + this.id;
	}
	/* End of Default Action */
}