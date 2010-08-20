package simulation.modeling;

import java.io.Serializable;
import java.util.*;

public class PlanManager implements Serializable
{
	private ArrayList<PlanInstance> plans = new ArrayList<PlanInstance>();
	private DefaultBelief sub;

	public void setSub(DefaultBelief sub)
	{
		this.sub = sub;
	}

	/* Default Action */
	public void addPlanInstance(PlanInstance pi)
	{
		this.plans.add(pi);
	}

	public void submitPlans()
	{
		int i = 0;
		while (i < this.plans.size())
		{
			PlanInstance pi = this.plans.get(i);
			if (pi.decTickLeft() < 0)
			{
				pi.invoke(this.sub);
				this.plans.remove(i);
			}
			else i++;
		}
	}
	/* End of Default Action */
}