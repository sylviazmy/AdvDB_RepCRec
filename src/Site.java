import java.util.HashMap;

public class Site {
	private int siteAddr;
	
	
	private boolean isFailed;
	private HashMap<String,Variable> variableList;
	//each site has an independent lock table(to store the variable and lock status)
	public HashMap<String,String> lockTable;
	
	public Site(int address){
		this.siteAddr=address;
	}
	public void failSite(){
		//if the site fails, the lock table is erased
		this.isFailed=true;
		this.lockTable.clear();
	}
	public void addVariable(String key,Variable value) {
		this.variableList.put(key, value);
	}
	public boolean hasVariable(String key) {
		if(variableList.containsKey(key)) {
			return true;
		}
		return false;
	}
	public int getVariable(String key) {
		return variableList.get(key).getValue();
	}
	
	public void setVariableLock(String variableName,String lockType) {
		Variable vb=variableList.get(variableName);
		lockTable.put(variableName, lockType);
	}
}
