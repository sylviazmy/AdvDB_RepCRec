import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Variable {
	private String index;
	private int value;
	private int tempValue;
	private LinkedHashMap<Integer,Integer> oldCopy= new LinkedHashMap<Integer,Integer>();
	
	public Variable(String index) {
		this.index=index;
		//each variable xi is initialized to the value 10i(10 times i)
		
		this.value=Integer.parseInt(index)*10;
		this.tempValue=this.value;
		this.oldCopy.put(0,this.value);
	}
	public Variable(String index,int num) {
		this.index=index;
		//each variable xi is initialized to the value 10i(10 times i)
		
		this.value=num;
		this.tempValue=this.value;
		this.oldCopy.put(0,this.value);
	}
	public String getName() {
		return "x"+this.index;
	}
	public int getValue() {
		return this.value;
	}
	public int getValueOntick(int tick) {
		int tmp=0;
		for(int t:oldCopy.keySet()) {
			if(t<=tick) {
				tmp=oldCopy.get(t);
			}else {
				break;
			}
		}
		return tmp;
	}
//	public int getOldValue(int tick){
//		
//		return tick;
//		
//	}
	public int getLastCommit() {
		return this.value;
	}
	public void editValue(int v) {
		this.tempValue=v;
		
	}
	public void commit(int tick) {
		this.oldCopy.put(tick, this.tempValue);
		this.value=this.tempValue;
	}
}
