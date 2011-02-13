package simulation.modeling;

import java.io.Serializable;
import java.util.*;

public class MessageInfo implements Serializable
{
	private int snd, rcv;
	private boolean sFlag, rFlag;
	private String content;
	private String ip;
	
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
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public String getIp(){
		return this.ip;
	}

	public String toString()
	{
		String s = "" + this.snd + " said to " + this.rcv + "£º" + this.content;
		return s + "\n";
	}
}