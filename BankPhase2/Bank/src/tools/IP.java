package tools;
/*
 * NetTools.java
 * Created on 2004-9-29
 * Copyright:
 */
import java.net.InetAddress;

/**
 * @author yanpeng
 * 
 * 
 */
public class IP {

	public static String getLocalHostIP() {
		String ip;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
		} catch (Exception ex) {
			ip = "";
		}
		return ip;
	}

	public static String getLocalHostName() {
		String hostName;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (Exception ex) {
			hostName = "";
		}
		return hostName;
	}

	public static String[] getAllLocalHostIP() {
		String[] ret = null;
		try {
			String hostName = getLocalHostName();
			if (hostName.length() > 0) {
				InetAddress[] addrs = InetAddress.getAllByName(hostName);
				if (addrs.length > 0) {
					ret = new String[addrs.length];
					for (int i = 0; i < addrs.length; i++) {
						ret[i] = addrs[i].getHostAddress();
					}
				}
			}

		} catch (Exception ex) {
			ret = null;
		}
		return ret;
	}

	public static String[] getAllHostIPByName(String hostName) {
		String[] ret = null;
		try {
			if (hostName.length() > 0) {
				InetAddress[] addrs = InetAddress.getAllByName(hostName);
				if (addrs.length > 0) {
					ret = new String[addrs.length];
					for (int i = 0; i < addrs.length; i++) {
						ret[i] = addrs[i].getHostAddress();
					}
				}
			}

		} catch (Exception ex) {
			ret = null;
		}
		return ret;
	}

	public static void main(String[] args) {
		// System.out.println(getLocalHostIP());
		System.out.println("Ö÷»úÃû£º" + getLocalHostName());
		System.out.println("first ip:" + getLocalHostIP());
		String[] localIP = getAllLocalHostIP();
		for (int i = 0; i < localIP.length; i++) {
			System.out.println(localIP[i]);
		}
	}
}