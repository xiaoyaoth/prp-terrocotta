/*Agent名称：Bank，创建者：匿名用户，创建时间：Sun May 16 21:26:03 CST 2010
 *
 */

import java.util.*;
import simulation.modeling.*;

public class Bank extends DefaultBelief
{
    public static ArrayList<PlanCondition> pc = new ArrayList<PlanCondition>(); // | pre
    private static int pCounter = 0; // | pre
    private int cash;

    public Bank(Integer cash/**/)
    {
        this.cash = cash;
    }

    public static void addPC(PlanCondition newPC/**/)/*pre*/
    {
        newPC.setID(++pCounter);
        pc.add(newPC);
    }

    public static int getPCIndex(int pcID/**/)/*pre*/
    {
        for (int i=0; i<pc.size(); i++) if (pc.get(i).getID() == pcID) return i;
        return -1;
    }

    public static int getPCIndex(String pn/**/)/*pre*/
    {
        for (int i=0; i<pc.size(); i++) if (pc.get(i).getPlanName().equals(pn)) return i;
        return -1;
    }

    public PlanCondition getPC(int index/**/)/*pre*/
    {
        return pc.get(index);
    }

    public void run()/*pre*/
    {
        while (this.getLifeCycle() == -1 || this.isNoLife())
        {
            synchronized (ClockTick.nowLock)
            {
                try {
                    while (this.getTick() >= ClockTick.getTick() || ClockTick.getNow() == 0) ClockTick.nowLock.wait();
                    this.addTick();
                    this.createPlans();
                    this.submitPlans();
                    ClockTick.decNow();
                }
                catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    private void createPlans()/*pre*/
    {
        this.receiveMessages();
        this.sendMessages();
        for (int i=0; i<pc.size(); i++)
        {
            PlanCondition tpc = pc.get(i);
            if (tpc.getInterval() > 0 && this.getOwnTick() % tpc.getInterval() == 0)
            {
                PlanInstance pi = new PlanInstance(this.getID(), tpc.getID(), tpc.getNeedTicks(), tpc.getPlanName());
                pi.setSize(0);
                this.addPlanInstance(pi);
            }
        }
    }

    private void receiveMessages()/*pre*/
    {
        for (int i=0; i<this.rcvMessageBox.size(); i++)
        {
            MessageInfo mi = this.rcvMessageBox.get(i);
            if (!mi.getRFlag())
            {
                mi.setRFlag();
                String temp = mi.getContent();
                int kh = temp.indexOf("(");
                if (kh > -1)
                {
                    String pn = temp.substring(0, kh);
                    int pIndex = getPCIndex(pn);
                    if (pIndex > -1)
                    {
                        PlanCondition tpc = pc.get(pIndex);
                        PlanInstance pi = new PlanInstance(this.getID(), tpc.getID(), tpc.getNeedTicks(), pn);
                        temp = temp.substring(1 + kh);
                        int dh = temp.indexOf(", "), yh = temp.indexOf(")");
                        if (yh != kh + 1)
                        {
                            ArrayList<String> al = new ArrayList<String>();
                            while (dh > -1)
                            {
                                al.add(temp.substring(0, dh));
                                temp = temp.substring(dh + 2);
                                dh = temp.indexOf(", ");
                            }
                            al.add(temp.substring(0, temp.indexOf(")")));
                            pi.setSize(al.size());
                            for (int j=0; j<al.size(); j++) pi.setPara(j, Integer.parseInt(al.get(j)));
                        }
                        else pi.setSize(0);
                        this.addPlanInstance(pi);
                    }
                }
            }
        }
    }

    private void sendMessages()/*pre*/
    {
        for (int i=0; i<this.sndMessageBox.size(); i++)
        {
            MessageInfo mi = this.sndMessageBox.get(i);
            if (!mi.getSFlag())
            {
                mi.setSFlag();
                mi.getRcv().addMess(false, mi);
            }
        }
    }

    public String toString()
    {
        return "Bank" + this.getID() + "（Cash：$" + this.cash + "）";
    }

    public void myPrint()
    {
        System.out.println(this);
    }

    /* Plan */
}