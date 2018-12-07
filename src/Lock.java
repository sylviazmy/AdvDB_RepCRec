import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Lock {
	public Set<String> rlockSet=new HashSet<>();
	public Set<String> wlockSet=new HashSet<>();
	public String wlockHolder;
	public String currentStatus="NoLock";
	public LinkedHashSet<String> lockQueue=new LinkedHashSet<String>();
	
	public Lock(){
		this.currentStatus="NoLock";
		this.wlockHolder="";
	}
	public String getLockStatus() {
//		System.out.println("current status");
		return this.currentStatus;
	}
	public void addRlock(String transactionName) {
		if(this.currentStatus.equals("NoLock")||this.currentStatus.equals("RL")) {
			this.rlockSet.add(transactionName);
			this.currentStatus="RL";
		}
	}
	public String firstinLockQueue() {
		if(this.lockQueue.size()==0) {
			return null;
		}
		else {
			Iterator it=lockQueue.iterator();
			return (String) it.next();
		}
	}
	public String lastinLockQueue() {
		if(this.lockQueue.size()==0) {
			return null;
		}
		else {
			String t="";
			Iterator it=lockQueue.iterator();
			while(it.hasNext()) {
				t=(String) it.next();
			}
			return t;
		}
	}
	public void addtoLockQueue(String trctName) {
//		System.out.print("\nadded to lock queue  "+trctName);
		this.lockQueue.add(trctName);
	}
	public void removeFromLockQueue(String trctName){
		this.lockQueue.remove(trctName);
	}
	
	public boolean addWlock(String transactionName) {
		if(this.wlockSet.size()==0 && this.wlockHolder=="") {
			this.wlockSet.add(transactionName);
			this.wlockHolder=transactionName;
			this.currentStatus="WL";
			return true;
		}
		else {
			System.out.printf("\nthe writelock has been held by %s",this.wlockHolder);
			return false;
		}
	}
	
	public boolean ExistsInRlock(String transactionName) {
		if(this.rlockSet.contains(transactionName)) {
			return true;
		}
		return false;
	}
	
	public String getWLockTransaction() {
		return this.wlockHolder;
	}
	
	public int getRlockLength() {
		return this.rlockSet.size();
	}
	
	public int getWlockLength() {
		return this.wlockSet.size();
	}
	
	public void releaseRlock(String tname){
		if(this.rlockSet.size()==1) {
			this.rlockSet.remove(tname);
			this.currentStatus="NoLock";
		}
		else {
			this.rlockSet.remove(tname);
			this.currentStatus="RL";
		}
//		this.rlockSet.clear();
	}
	
	public void releaseWlock(){
		this.wlockSet.clear();
		this.wlockHolder="";
		if(this.rlockSet.size()>0) {
			this.currentStatus="RL";
		}else if(this.rlockSet.size()==0) {
			this.currentStatus="NoLock";
		}
	}
}
