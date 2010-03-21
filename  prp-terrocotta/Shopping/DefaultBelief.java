import java.util.*;

public class DefaultBelief
{
	private int id, tick, lifeCycle;
	
	public DefaultBelief(int id, int tick, int lifeCycle)
	{
		this.id = id;
		this.tick = tick;
		this.lifeCycle = lifeCycle;
	}

	public int getID()
	{
		return this.id;
	}

	public void addTick()
	{
		this.tick++;
	}

	public int getTick()
	{
		return this.tick;
	}
	
	public int getLifeCycle()
	{
		return this.lifeCycle;
	}

	public boolean isNoLife()
	{
		return --this.lifeCycle >= 0;
	}
}