
public class Variable {
	private String index;
	private int value;
	private int tempValue;
	private int OldestRLT;//readlock time
	private int OldestWLT;//writelock time
	
	public Variable(String index) {
		this.index=index;
		//each variable xi is initialized to the value 10i(10 times i)
		this.value=Integer.parseInt(index.substring(1))*10;
		this.tempValue=this.value;
	}
	public int getValue() {
		return this.value;
	}
	public int getLastCommit() {
		return this.value;
	}
	public void editValue(int v) {
		this.tempValue=v;
		
	}
	public void commit() {
		this.value=this.tempValue;
	}
}
