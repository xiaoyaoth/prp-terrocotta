import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ServerMachine {

	/**** �����ֲ�ʽϵͳ��Ҫ�ĳ�Ա ****/
	public boolean conv_fini = false;
	public Map<Integer, Agent> map;
	public ArrayList<ArrayList<Integer>> caseTable;
	/**** �����ֲ�ʽϵͳ��Ҫ�ĳ�Ա ****/
	
	ServerMachine(){
		caseTable = new ArrayList<ArrayList<Integer>>();
		map = new HashMap<Integer, Agent>();
		conv_fini = false;
	}

}
