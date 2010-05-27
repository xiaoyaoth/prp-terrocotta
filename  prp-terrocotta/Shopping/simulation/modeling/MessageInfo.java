package simulation.modeling;

import java.util.*;

public class MessageInfo
{
	private DefaultBelief snd, rcv;
	private boolean sFlag, rFlag;
	private String content;
	
	public MessageInfo(DefaultBelief snd, DefaultBelief rcv, String content)
	{
		this.snd = snd;
		this.rcv = rcv;
		this.content = content;
		this.sFlag = false;
		this.rFlag = false;
	}

	public DefaultBelief getSnd()
	{
		return this.snd;
	}

	public DefaultBelief getRcv()
	{
		return this.rcv;
	}

	public void setSFlag()
	{
		this.sFlag = true;
	}

	public boolean getSFlag()
	{
		return this.sFlag;
	}

	public void setRFlag()
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