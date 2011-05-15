

public class Order
{
    private static int counter = 0;

    private int id;
    private int price;

    public Order(Integer price/**/)
    {
        this.id = ++counter;
        this.setPrice(price);
    }

    public int getPrice()
    {
        return this.price;
    }

    public void setPrice(Integer price/**/)
    {
        this.price = price;
    }

    public String toString()
    {
        return "Order" + this.id;
    }
}