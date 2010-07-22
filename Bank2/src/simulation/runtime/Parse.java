package simulation.runtime;

import simulation.modeling.Path;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
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

public class Parse {
	ArrayList<Tuple> table = new ArrayList<Tuple>();
	private String slnPath;
	private Map<Integer, String> roleTy = new HashMap<Integer, String>();
	private int agTy = -1, roletyc = 0; //roleTypeCount
	Tuple tp = new Tuple();

	public Parse(String path) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = domfac.newDocumentBuilder();
		InputStream is  = new FileInputStream(path);
		Document doc = dombuilder.parse(is);
		printNode(doc, 0, new Path(0));
	}
	
	public String getSlnPath()
	{
		return this.slnPath;
	}

	public void printNode(Node node, int count, Path p) 
	{
		Path tempP = new Path(p);
		String tmp = "" + count;
		for (int i = 0; i < count; i++) tmp += "  ";

		if (node != null) {
			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node n = childNodes.item(i);
				NamedNodeMap attrs = n.getAttributes();
				if (n.getNodeName().equals("sln")){
					this.slnPath = "sln\\" + n.getFirstChild().getNodeValue();
				}
				if (n.getNodeName().equals("RoleInfo")) {
					roleTy.put(roletyc, n.getAttributes().item(0).getNodeValue());
					roletyc ++;
				}
				if (n.getNodeName().equals("Role")){
					agTy = Integer.parseInt(attrs.item(0).getNodeValue());
				}
				if (n.getNodeName().equals("Instance")){
					tp.id = Integer.parseInt(attrs.item(0).getNodeValue());
				}
				if (n.getParentNode().getNodeName().equals("Instance")&& attrs != null)
				{
					for(int j = 0; j< attrs.getLength(); j++)
					{
						tp.args.add(attrs.item(j).getNodeValue());
					}
					tp.path = tempP;
					tp.agTy = roleTy.get(agTy);
					table.add(tp);
					tp = new Tuple();
				}
				if (n.getNodeName().equals("Sons")){
					tempP = new Path(tempP,0);
				}
				if (n.getNodeName().equals("Part")){
					int index = tempP.getSize()-1;
					tempP.set(index, tempP.get(index)+1);
				}
				printNode(n, count + 1, tempP);
			}
		}
	}

	/*public static void main(String[] args) {
		try {
			Parse p = new Parse("C:\\Users\\xiaoyaoth\\Desktop\\新建文件夹 (2)\\USER\\usr1\\snr.xml");
			System.out.println(p.table);
			System.out.println(p.sln_path);
//			new Parse();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}

class Tuple {
	String agTy;
	Path path;
	int JVM_id, id;
	ArrayList<String> args = new ArrayList<String>();
	Integer nopt;

	Tuple()
	{
		agTy = "~";
		path = new Path(0);
		JVM_id = -1;
		id = -1;
		args = new ArrayList<String>();
		nopt = -1;
	}
	
	public String toString() 
	{
		String s = path + " " + id + " " + nopt + " " + agTy + " [";
		for (Object o : args) s += o.toString() + " ";
		s += "]\n";
		return s;
	}
}
