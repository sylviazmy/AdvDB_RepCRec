import java.util.HashMap;
import java.util.Set;

public class Site {
	private int siteAddr;
	private boolean isFailed;
	public HashMap<String,Variable> variableList=new HashMap<String,Variable>();
	//each site has an independent lock table(to store the variable and lock status)
	public HashMap<String,Lock> lockTable=new HashMap<String,Lock>();
	
	public Site(int address){
		this.siteAddr=address;
	}
	public int getSiteAddr() {
		return this.siteAddr;
	}
	
	public void failSite(){
		//if the site fails, the lock table is erased
		this.isFailed=true;
//		this.lockTable.clear();
		for(String v:lockTable.keySet()) {
			lockTable.get(v).currentStatus="NoLock";
			lockTable.get(v).wlockHolder="";
		}
	}
	public void recover(){
		
		this.isFailed=false;
		for(String v:variableList.keySet()) {
			Variable vb;
			if(Integer.parseInt(v)%2==1) {
				vb=new Variable(v);
				variableList.put(v, vb);
			}else {
				vb=new Variable(v,Integer.MAX_VALUE);
				variableList.put(v, vb);
			}
//			System.out.println("\nsite recovered:"+this.siteAddr+"   "+v+"-"+vb.getValue());
//			variableList.get(v).
		}
	}
	public boolean isFailed() {
		return this.isFailed;
	}
	public void addVariable(String key,Variable v) {
		this.variableList.put(key, v);
		Lock lock= new Lock();
		this.lockTable.put(key,lock);
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
	public int getOldVariable(String key,int time) {
		return variableList.get(key).getValueOntick(time);
	}
	public void setReadLock(String variableName,String transactionName) {
		Variable vb=variableList.get(variableName);
		lockTable.get(variableName).addRlock(transactionName);
	}
	public void setWriteLock(String variableName,String transactionName,int value) {
		Variable vb=variableList.get(variableName);
		vb.editValue(value);
		lockTable.get(variableName).addWlock(transactionName);
	}
	
	public String getLockStatus(String variableName) {
//		System.out.println("x"+variableName+" at "+this.siteAddr+"   "+this.lockTable.size());
		return lockTable.get(variableName).getLockStatus();
	}
	public void printStatus() {
		for(String key:variableList.keySet()) {
			System.out.print(key+" "+variableList.get(key).getValue()+" "+lockTable.get(variableList.get(key)).getLockStatus()+" \n");
		}
	}
}
