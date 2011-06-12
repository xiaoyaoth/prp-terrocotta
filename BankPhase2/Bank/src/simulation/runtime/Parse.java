package simulation.runtime;

import simulation.modeling.Path;
import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Parse implements Serializable {
	transient private static String root = "config\\xx\\";

	transient private ArrayList<Tuple> table = new ArrayList<Tuple>();
	transient private String slnPath;
	transient private Map<Integer, String> roleTy = new HashMap<Integer, String>();
	transient private int agTy = -1, roletyc = 0; // roleTypeCount
	transient private Tuple tp = new Tuple();
	transient private Integer hostId;
	
	private int totalAgentNum;
	private String usr;
	private int tick;
	private int prior;
	private long parseStart;

	public Parse(String usr, String tick, String prior) {
		this(usr, Integer.parseInt(tick), Integer.parseInt(prior));
	}

	public Parse(String usr, int tick, int prior) {
		this.hostId = -1;
		this.usr = usr;
		this.tick = tick;
		this.prior = prior;
		try {
			String configPath = root + "USER\\" + usr + "\\snr.xml";
			System.out.println(configPath);
			DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			InputStream is = new FileInputStream(configPath);
			Document doc = dombuilder.parse(is);
			printNode(doc, 0, new Path(0));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setHostId(int hostId){
		this.hostId = hostId;
	}
	
	public int getHostId(){
		return this.hostId;
	}
	
	public int getPrior(){
		return this.prior;
	}

	public ArrayList<Tuple> getTable() {
		return this.table;
	}

	public String getSlnPath() {
		return this.slnPath;
	}

	public String getUsr() {
		return this.usr;
	}

	public int getTick() {
		return this.tick;
	}
	
	public int getAgentTotalNum(){
		return this.totalAgentNum;
	}

	public void printNode(Node node, int count, Path p) {
		Path tempP = new Path(p);
		String tmp = "" + count;
		for (int i = 0; i < count; i++)
			tmp += "  ";

		if (node != null) {
			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node n = childNodes.item(i);
				NamedNodeMap attrs = n.getAttributes();
				if (n.getNodeName().equals("sln")) {
					this.slnPath = "sln\\" + n.getFirstChild().getNodeValue();
				}
				if (n.getNodeName().equals("RoleInfo")) {
					roleTy.put(roletyc, n.getAttributes().item(0)
							.getNodeValue());
					roletyc++;
				}
				if (n.getNodeName().equals("Role")) {
					agTy = Integer.parseInt(attrs.item(0).getNodeValue());
				}
				if (n.getNodeName().equals("Instance")) {
					tp.id = Integer.parseInt(attrs.item(0).getNodeValue());
				}
				if (n.getParentNode().getNodeName().equals("Instance")
						&& attrs != null) {
					for (int j = 0; j < attrs.getLength(); j++) {
						tp.args.add(attrs.item(j).getNodeValue());
					}
					tp.path = tempP;
					tp.agTy = roleTy.get(agTy);
					table.add(tp);
					tp = new Tuple();
					this.totalAgentNum++;
				}
				if (n.getNodeName().equals("Sons")) {
					tempP = new Path(tempP, 0);
				}
				if (n.getNodeName().equals("Part")) {
					int index = tempP.getSize() - 1;
					tempP.set(index, tempP.get(index) + 1);
				}
				if (n.getNodeName().equals("Instances")) {
//					this.totalAgentNum += Integer.parseInt(attrs.item(0)
//							.getNodeValue());
				}
				printNode(n, count + 1, tempP);
			}
		}
	}
	
	public String toString(){
		String res = "#"+this.tick+"_"+this.totalAgentNum+"_"+this.prior+"#";
		return res;
	}

	public static void main(String[] args) {

		Parse p = new Parse("test1_50", 100, 0);
		System.out.println(p.totalAgentNum);
		System.out.println(p.table);
		System.out.println(p.slnPath);
	}
}

class Tuple implements Serializable {
	String agTy;
	Path path;
	int JVM_id, id;
	ArrayList<String> args = new ArrayList<String>();
	Integer nopt;

	Tuple() {
		agTy = "~";
		path = new Path(0);
		JVM_id = -1;
		id = -1;
		args = new ArrayList<String>();
		nopt = -1;
	}

	public String toString() {
		String s = path + " " + id + " " + nopt + " " + agTy + " [";
		for (Object o : args)
			s += o.toString() + " ";
		s += "]\n";
		return s;
	}
}
