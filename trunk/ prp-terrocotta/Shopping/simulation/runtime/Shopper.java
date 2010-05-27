package simulation.runtime;

import simulation.modeling.*;

public class Shopper extends DefaultBelief {
	
	public int cash = 0, col = -1;
	
	public void run(){}
	
	public Shopper(Integer col)
	{
		this.col = col;
	}
	
    public void addCash(int incr)
    {
    	this.cash += incr;
    	System.out.println(this.col+" "+this.cash);
    }
    
    public int getCol()
    {
    	return this.col;
    }
	
}
