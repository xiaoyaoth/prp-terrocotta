import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ServerMachine {

	/**** 构建分布式系统需要的成员 ****/
	public boolean conv_fini = false;
	public Map<Integer, Agent> map;
	public ArrayList<ArrayList<Integer>> caseTable;
	/**** 构建分布式系统需要的成员 ****/
	
	ServerMachine(){
		caseTable = new ArrayList<ArrayList<Integer>>();
		map = new HashMap<Integer, Agent>();
		conv_fini = false;
	}

}
