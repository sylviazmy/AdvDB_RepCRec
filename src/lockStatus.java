
public class lockStatus {
	private Variable variable;
	private Types lockType;
	public enum Types{
		Read,
		Write,
		ReadOnly
	}
	public lockStatus(Variable v,Types lt){
		this.variable=v;
		this.lockType=lt;
	}

	
}
