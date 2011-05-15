package simulation.modeling;

import java.io.Serializable;
import java.lang.reflect.*;

public class InvokeMethod implements Serializable
{
	public static Object invokeMethod(Object owner, String methodName, Object[] args) throws Exception
	{
		Class ownerClass = owner.getClass();
		Class[] argsClass = new Class[args.length];
		for (int i=0; i<args.length; i++) argsClass[i] = args[i].getClass();
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(owner, args);
	}

	public static Object invokeStaticMethod(String className, String methodName, Object[] args) throws Exception
	{
		Class ownerClass = Class.forName(className);
		Class[] argsClass = new Class[args.length];
		for (int i=0; i<args.length; i++) argsClass[i] = args[i].getClass();
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(null, args);
	}

	public static Object newInstance(String className, Object[] args) throws Exception
	{
		Class newoneClass = Class.forName(className);
		Class[] argsClass = new Class[args.length];
		for (int i=0; i<args.length; i++) argsClass[i] = args[i].getClass();
		Constructor cons = newoneClass.getConstructor(argsClass);
		return cons.newInstance(args);
	}
}