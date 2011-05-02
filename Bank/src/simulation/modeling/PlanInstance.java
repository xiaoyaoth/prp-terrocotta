package simulation.modeling;

import java.io.Serializable;
import java.util.*;

public class PlanInstance implements Serializable
{
	private int tickLeft, pcID, agentID;
	private String planName;
    private Object[] para;

	public PlanInstance(int agentID, int pcID, int tickLeft, String planName)
	{
		this.agentID = agentID;
		this.pcID = pcID;
		this.tickLeft = tickLeft;
		this.planName = planName;
	}

	public int decTickLeft()
	{
		return --this.tickLeft;
	}

	public void setSize(int size)
	{
		this.para = new Object[size];
	}

	public void setPara(int index, Object obj)
	{
		this.para[index] = obj;
	}

	public void invoke(Object sub)
	{
		try {
			Object o = InvokeMethod.invokeMethod(sub, this.planName, this.para);
			this.para = null;
		}
		catch (Exception e) {}
	}
}