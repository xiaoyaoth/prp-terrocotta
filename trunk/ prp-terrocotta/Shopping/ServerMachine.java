import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ServerMachine {

	/**** �����ֲ�ʽϵͳ��Ҫ�ĳ�Ա ****/
	public boolean conv_fini = false;
	public Map<Integer, Agent> map;
	public ArrayList<ArrayList<Integer>> caseTable;
	public ClockTick clk;
	public GUI gui;
	/**** �����ֲ�ʽϵͳ��Ҫ�ĳ�Ա ****/
	
	ServerMachine(){
		caseTable = new ArrayList<ArrayList<Integer>>();
		map = new HashMap<Integer, Agent>();
		conv_fini = false;
		clk = new ClockTick(this);
	}
	/*
	 *       <additional-boot-jar-classes>
        <include>java.lang.Thread</include>
        <include>javax.swing.JFrame</include>
        <include>java.awt.Frame</include>
        <include>java.awt.Window</include>
        <include>java.awt.Container</include>
        <include>java.awt.Component</include>
      </additional-boot-jar-classes>
	 */

}
