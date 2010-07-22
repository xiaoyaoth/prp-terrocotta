package simulation.modeling;

import java.util.*;

public class PlanCondition
{
	private int id, interval, needTicks;
	private String planName;
	private ArrayList<String> para = new ArrayList<String>();

	public PlanCondition(int interval, int needTicks, String planName)
	{
		this.planName = planName;
		this.needTicks = needTicks;
		this.interval = interval;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public int getID()
	{
		return this.id;
	}

	public void setNeedTicks(int needTicks)
	{
		this.needTicks = needTicks;
	}

	public int getNeedTicks()
	{
		return this.needTicks;
	}

	public void setInterval(int interval)
	{
		this.interval = interval;
	}

	public int getInterval()
	{
		return this.interval;
	}

	public void setPlanName(String planName)
	{
		this.planName = planName;
	}

	public String getPlanName()
	{
		return this.planName;
	}

	public void add(String beliefName)
	{
		this.para.add(beliefName);
	}

	public String get(int index)
	{
		return this.para.get(index);
	}

	public int getSize()
	{
		return this.para.size();
	}
}