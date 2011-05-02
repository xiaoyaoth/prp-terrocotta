/*Agent名称：Restaurant，创建者：匿名用户，创建时间：Sun Feb 13 14:16:17 CST 2011
 *
 */



import java.util.ArrayList;
import java.io.Serializable;
import simulation.modeling.*;

public class Restaurant extends DefaultBelief implements Serializable
{
	private String name;
	private int cash;
	private ArrayList<Order> orderList = new ArrayList<Order>();

    public Restaurant(Integer cash/**/) //用户自定义的构造函数
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

    public String getString()
    {
        return "Restaurant" + this.getID();
    }

    public void addCash(Integer incr/**/)
    {
        this.cash += incr;
        System.out.println(this + " 's current cash is " + this.getCash());
    }

    public void add(Integer pid/**/, Integer price/**/)
    {
        this.orderList.add(new Order(price));
        this.addCash(-price);
        System.out.println(pid + " sent an order, value is " + price);
    }

    public Order remove(Integer cid/**/)
    {
        Order o = this.orderList.remove(0);
        this.addCash(o.getPrice());
        System.out.println(cid + " fetch an order, value is " + o.getPrice());
        return o;
    }

    /* Plan */
}