/*Agent名称：Agent，创建者：匿名用户，创建时间：Fri Feb 18 13:44:19 CST 2011
 *
 */

import java.io.Serializable;
import simulation.modeling.*;

public class Agent extends DefaultBelief implements Serializable
{
    private int row;
    private int col;
    private int tRow;
    private int tCol;
    private int rRow;
    private int rCol;
    private char tItem = ' ';
    private char rItem = ' ';
    private String status = "";
    public int frus;
    private int items;
    private int trips;

    public Agent(Integer frus/**/) //用户自定义的构造函数
    {
        this.frus = frus;
        this.initAgent(); //OtherOperation
    }

    public void initAgent() //OtherOperation
    {
        this.rItem = (char)(65 + (int)(Math.random() * 26));
        this.rRow = Environment.maxRow - 1;
        this.rCol = Environment.maxCol - 1;
        this.init(true);
        this.items = 0;
        this.trips = 0;
    }

    private void init(boolean b/**/)
    {
        this.status = "Waiting";
        if (!b) this.clean();
        this.row = 1;
        this.col = 1 + (int)(Math.random() * 9);
        this.tItem = this.rItem;
        this.tRow = this.rRow;
        this.tCol = this.rCol;
        if (b) this.draw();
    }

    private synchronized void clean()
    {
        Environment.a[this.row][this.col] = '~';
    }

    private synchronized void draw()
    {
        Environment.a[this.row][this.col] = (char)(48 + this.getID());
    }

    public int getFrus()
    {
        return this.frus;
    }

    public int getItems()
    {
        return this.items;
    }

    public int getTrips()
    {
        return this.trips;
    }

    public int getRow()
    {
        return this.row;
    }

    public int getCol()
    {
        return this.col;
    }

    public int getTRow()
    {
        return this.tRow;
    }

    public int getTCol()
    {
        return this.tCol;
    }

    public int getRRow()
    {
        return this.rRow;
    }

    public int getRCol()
    {
        return this.rCol;
    }

    public char getTItem()
    {
        return this.tItem;
    }

    public char getRItem()
    {
        return this.rItem;
    }

    public void shop() //Plan，用户自定义
    {
        if (this.status.equals("Waiting")) this.checkWait();
        else if (this.row == 1 && this.col > 10) this.init(false);
        else if (this.status.equals("Found")) this.checkTarget();
        else if (this.status.equals("Leaving")) this.move(this.row - 1, this.col);
        else if (this.frus > (int)(Math.random() * 100)) this.moveRandomly();
        else this.moveToTarget();
        this.lookAround();
    }

    private void checkWait()
    {
        if ((int)(Math.random() * 100) < Environment.waiting) 
        {
            this.status = "Browsing";
            this.trips++;
        }
    }

    private void checkTarget()
    {
        if (this.tItem == '$')
        {
            this.status = "Leaving";
            this.tItem = ' ';
        }
        else {
            this.items++;
            if ((int)(Math.random() * 100) < Environment.memory)
            {
                this.rRow = this.row;
                this.rCol = this.col;
            }
            this.status = "Checkout";
            this.tItem = '$';
            this.tRow = 2;
            this.tCol = Environment.maxCol - 3 * (int)(Math.random() * 6);
        }
    }

    private void moveRandomly()
    {
        int dire[][] = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        int k = (int)(Math.random() * 4);
        this.move(this.row + dire[k][0], this.col + dire[k][1]);
    }

    private void moveToTarget()
    {
        if (this.tCol == this.col && this.tRow == this.row && 
            this.col == Environment.maxCol - 1 && this.row == Environment.maxRow - 1)
        {
            this.tRow = 5;
            this.tCol = 2;
        }
        else if (this.tCol == this.col && this.tRow == this.row && this.col == 2 && this.row == 5)
        {
            this.tRow = Environment.maxRow - 1;
            this.tCol = Environment.maxCol - 1;
        }
        else if (this.tRow == 2 && this.row < 4 && this.col < 10) this.move(this.row + 1, this.col);
        else if (this.tRow == 2 && this.row == 18 && this.col == this.tCol)
        {
            if (this.col == 24 || this.col == 15) this.move(this.row, this.col + 1);
            else this.move(this.row, this.col - 1);
        }
        else {
            int dta1 = this.tCol - this.col, dta2 = this.tRow - this.row, nextRow = this.row, nextCol = this.col;
            if (dta1 > 0) nextCol += 1;
            else if (dta1 < 0) nextCol -= 1;
            if (dta2 > 0) nextRow += 1;
            else if (dta2 < 0) nextRow -= 1;
            if (Math.random() < 0.75)
            {
                if (!this.move(this.row, nextCol)) this.move(nextRow, this.col);
            }
            else if (!this.move(nextRow, this.col)) this.move(this.row, nextCol);
        }
    }

    private void lookAround()
    {
        int dire[][] = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int k=0; k<4; k++)    
        {
            int tempX = this.row + dire[k][0], tempY = this.col + dire[k][1];
            if (tempX > 0 && tempX <= Environment.maxRow && 
                tempY > 0 && tempY <= Environment.maxCol && Environment.a[tempX][tempY] == this.tItem)
            {
                this.status = "Found";
            }
        }
    }

    private synchronized boolean move(int nextRow/**/, int nextCol/**/)
    {
        if (nextRow > 0 && nextRow <= Environment.maxRow &&    
            nextCol > 0 && nextCol <= Environment.maxCol && Environment.a[nextRow][nextCol] == '~')
        {
            this.clean();
            this.row = nextRow;
            this.col = nextCol;
            this.draw();
            this.feelBetter();
            return true;
        }
        else {
            this.feelWorse();
            return false;
        }
    }

    private void feelBetter()
    {
        if (--this.frus < 3) this.frus = 3;
    }

    private void feelWorse()
    {
        if (++this.frus > 99)
        {
            if (this.status.equals("Browsing"))
            {
                this.status = "Checkout";
                this.tItem = '$';
                this.tRow = 2;
                this.tCol = Environment.maxCol - 3 * (int)(Math.random() * 6);
            }
            this.frus = 100;
        }
    }

    /* Plan */
}