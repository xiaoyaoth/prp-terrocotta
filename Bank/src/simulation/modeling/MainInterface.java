package simulation.modeling;

import java.util.*;

public interface MainInterface
{
	int getTotal();

	<T> ArrayList<T> getAgentList(Class<T> targetClass);
	<T> ArrayList<T> getAgentList(Class<T> targetClass, Path path);
	DefaultBelief getAgent(int id);
	
	ClockTick getClock();
	int getCaseID();
}