/*Agent名称：Producer，创建者：匿名用户，创建时间：Sun Feb 13 14:16:17 CST 2011
 *
 */



import java.util.ArrayList;
import java.io.Serializable;
import simulation.modeling.*;

public class Producer extends DefaultBelief implements Serializable
{
    private String name;
    private Integer cash;

    public Producer(Integer cash/**/) //用户自定义的构造函数
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
        return "Producer" + this.getID();
    }

    private Restaurant selectRestaurant()
    {
        ArrayList<Restaurant> rList = this.main.getAgentList(Restaurant.class);
        return rList.get((int)(Math.random() * rList.size()));
    }

    public synchronized void sendOrderDirectly()
    {
        double temp = 0.0;
        for (int i=0; i<9999; i++) temp += Math.random() * Math.random();
        this.selectRestaurant().add(this.getID(), (int)temp / 9999);
        this.cash += (int)temp / 9999;
        System.out.println(this + " 's current cash is " + this.getCash());
    }

    public synchronized void sendOrderByMessage()
    {
        Restaurant rest = this.selectRestaurant();
        this.addMess(true, new MessageInfo(this.getID(), rest.getID(), "add(" + this.getID() + ", 1)", this.getIp()));
        this.cash += (int)1.0;
        System.out.println(this + " 's current cash is " + this.getCash());
    }

    public synchronized void addCashByMessageRepeatly()
    {
        Restaurant rest = this.selectRestaurant();
        for (int i=0; i<9999; i++)
            this.addMess(true, new MessageInfo(this.getID(), rest.getID(), "addCash(-1)", this.getIp()));
    }

    /* Plan */
}