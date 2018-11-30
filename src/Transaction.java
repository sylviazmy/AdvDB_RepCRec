
public class Transaction {
	private String TName;
	private boolean status;
	private int TId;
	public enum type{
		Read,
		Write,
		ReadOnly
	}
	public Transaction(String Tname,int id) {
		this.TName=Tname;
		this.TId=id;
		this.status=false;
		
	}
}
