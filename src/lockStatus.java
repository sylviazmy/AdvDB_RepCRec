
public class lockStatus {
	private Variable variable;
	private Types lockType;
	public enum Types{
		NoLock,
		Read,
		Write,
		ReadOnly
	}
	public lockStatus(Variable v,Types lt){
		this.variable=v;
		this.lockType=lt;
	}
	public void init() {
		this.lockType=Types.NoLock;
	}
	public void setReadLock() {
		
	}

	
}
