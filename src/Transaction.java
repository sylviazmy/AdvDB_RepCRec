
public class Transaction {
	private String TName;
	public boolean isAborted;
	public boolean isCommited;
	private int TId;
	private int time;
	private boolean isReadOnly;
	
	public Transaction(String Tname,int id) {
		this.TName=Tname;
		this.TId=id;
		this.isAborted=false;
		this.isCommited=false;
		this.isReadOnly=false;
	}
	public void setReadOnly() {
		this.isReadOnly=true;
	}
	public boolean isReadonly() {
		return this.isReadOnly;
	}
}
