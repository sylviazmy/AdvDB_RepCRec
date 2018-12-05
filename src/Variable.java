
public class Variable {
	private String index;
	private int value;
	private int tempValue;
	private int OldestRLT;//readlock time
	private int OldestWLT;//writelock time
	private int oldCopy;
	
	public Variable(String index) {
		this.index=index;
		//each variable xi is initialized to the value 10i(10 times i)
		
		this.value=Integer.parseInt(index)*10;
		this.tempValue=this.value;
		this.oldCopy=this.value;
	}
	public String getName() {
		return "x"+this.index;
	}
	public int getValue() {
		return this.value;
	}
	public int getOldValue() {
		return this.oldCopy;
	}
	public int getLastCommit() {
		return this.value;
	}
	public void editValue(int v) {
		this.tempValue=v;
		
	}
	public void commit() {
		this.oldCopy=this.value;
		this.value=this.tempValue;
	}
}
