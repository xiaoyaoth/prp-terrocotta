package simulation.modeling;

import java.util.*;

public interface MainInterface
{
	int getTotal();

	<T> ArrayList<T> getAgentList(Class<T> targetClass);
	<T> ArrayList<T> getAgentList(Class<T> targetClass, Path path);
	DefaultBelief getAgent(int id);
	public Map<Integer, DefaultBelief> getAgentList();
	public Integer assign();
	
	ClockTick getClock();
	int getCaseID();
}