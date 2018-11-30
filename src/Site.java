import java.util.HashMap;

public class Site {
	private int siteAddr;
	//each site has an independent lock table
	private HashMap<Integer,String> lockTable;
	private boolean isFailed;
	
	public Site(int address){
		this.siteAddr=address;
	}
	public void failSite(){
		//if the site fails, the lock table is erased
		this.isFailed=true;
		this.lockTable.clear();
	}
}
