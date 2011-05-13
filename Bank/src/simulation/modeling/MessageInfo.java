package simulation.modeling;

import java.io.Serializable;
import java.util.*;

public class MessageInfo implements Serializable
{
	private int snd, rcv;
	private boolean sFlag, rFlag;
	private String content;
	
	public MessageInfo(int snd, int rcv, String content)
	{
		this.snd = snd;
		this.rcv = rcv;
		this.content = content;
		this.sFlag = false;
		this.rFlag = false;
	}

	public int getSnd()
	{
		return this.snd;
	}

	public int getRcv()
	{
		return this.rcv;
	}

	public synchronized void setSFlag()
	{
		this.sFlag = true;
	}

	public boolean getSFlag()
	{
		return this.sFlag;
	}

	public synchronized void setRFlag()
	{
		this.rFlag = true;
	}

	public boolean getRFlag()
	{
		return this.rFlag;
	}

	public String getContent()
	{
		return this.content;
	}

	public String toString()
	{
		String s = "" + this.snd + " said to " + this.rcv + "£º" + this.content;
		return s + "\n";
	}
}