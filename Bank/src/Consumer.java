/*Agent名称：Consumer，创建者：匿名用户，创建时间：Sun Feb 13 14:16:17 CST 2011
 *
 */


import java.util.ArrayList;
import java.io.Serializable;
import simulation.modeling.*;

public class Consumer extends DefaultBelief implements Serializable
{
    private String name;
    private int cash;

    public Consumer(Integer cash/**/) //用户自定义的构造函数
    {
    	this.cash = cash;
    	this.initAgent(); //OtherOperation
    }

    public void initAgent() //OtherOperation
    {
    	this.setName(this.toString());
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name/**/)
    {
        this.name = name;
        System.out.println(this + " 's current name is " + this.getName());
    }

    public double getCash()
    {
        return this.cash;
    }

    public void setCash(Integer cash/**/)
    {
        this.cash = cash;
    }

    public String toString()
    {
        return "Consumer" + this.getID();
    }

    private Restaurant selectRestaurant()
    {
        ArrayList<Restaurant> rList = this.main.getAgentList(Restaurant.class);
        return rList.get((int)(Math.random() * rList.size()));
    }

    public synchronized void fetchOrderDirectly()
    {
        double temp = 0.0;
        Order o = this.selectRestaurant().remove(this.getID());
        this.cash -= o.getPrice();
        System.out.println(this + " 's current cash is " + this.getCash());
    }

    public synchronized void fetchOrderByMessage()
    {
        Restaurant rest = this.selectRestaurant();
        this.addMess(true, new MessageInfo(this.getID(), rest.getID(), "remove(" + this.getID() + ")", this.getIp()));
        this.cash -= 1.0;
        System.out.println(this + " 's current cash is " + this.getCash());
    }

    public synchronized void addCashByMessageRepeatly()
    {
        Restaurant rest = this.selectRestaurant();
        for (int i=0; i<9999; i++)
            this.addMess(true, new MessageInfo(this.getID(), rest.getID(), "addCash(1)", this.getIp()));
    }

    /* Plan */
}