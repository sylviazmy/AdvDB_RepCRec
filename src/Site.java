import java.util.HashMap;
import java.util.Set;

public class Site {
	private int siteAddr;
	private boolean isFailed;
	private HashMap<String,Variable> variableList=new HashMap<String,Variable>();
	//each site has an independent lock table(to store the variable and lock status)
	public HashMap<Variable,Lock> lockTable=new HashMap<Variable,Lock>();
	
	public Site(int address){
		this.siteAddr=address;
	}
	public void failSite(){
		//if the site fails, the lock table is erased
		this.isFailed=true;
		this.lockTable.clear();
	}
	public void addVariable(String key,Variable v) {
		this.variableList.put(key, v);
		Lock lock= new Lock();
		this.lockTable.put(v,lock);
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
	public int getOldVariable(String key) {
		return variableList.get(key).getOldValue();
	}
//	public void setVariableLock(String variableName,String lockType) {
//		Variable vb=variableList.get(variableName);
//		lockTable.put(variableName, lockType);
//	}
	public void setReadLock(String variableName,String transactionName) {
		Variable vb=variableList.get(variableName);
		lockTable.get(vb).addRlock(transactionName);
	}
	public void setWriteLock(String variableName,String transactionName) {
		Variable vb=variableList.get(variableName);
		lockTable.get(variableName).addWlock(transactionName);
	}
	public void printStatus() {
		for(String key:variableList.keySet()) {
			System.out.print(key+" "+variableList.get(key).getValue()+" "+lockTable.get(variableList.get(key)).getLockStatus()+" \n");
		}
	}
}
