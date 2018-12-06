import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class transactionManager {
	static private final int numOfSites=10;
	static private final int numOfVariables=20;
	private ArrayList<Site> siteList=new ArrayList<Site>();
	private ArrayList<Variable> variableList=new ArrayList<Variable>();
	private HashMap<String,Transaction> transactionList=new HashMap<String,Transaction>();
	private static ArrayList<String[]> OpsList= new ArrayList<String[]>();
	private HashMap<String,String[]> waitList=new HashMap<String,String[]>();
	public Set<String> GraphNode=new HashSet<String>();
	public Set<String> GraphEdge=new HashSet<String>();
	private String startOfCycle="";
	
	public Site getSite(int index) {
		return siteList.get(index-1);
	}
	public Transaction getTransaction(String name) {
		return transactionList.get(name);
	}
	public Variable getVariable(int index) {
		return variableList.get(index-1);
	}
	
	public void deadlockDetecter() {
		//DFS
		
		if(GraphNode.size()>1) {//create graph
			HashMap<String,Set<String>> graph= new HashMap<String,Set<String>>();
			String root="";
			for (String node:GraphNode) {//insert node
				graph.put(node,new HashSet<String>());
//				root=node;
			}
			
			for(String edge:GraphEdge) {//insert edge
				String head=edge.split("-")[0];
				String tail=edge.split("-")[1];
				if(!graph.containsKey(tail)) {
					continue;
				}
				else{
					graph.get(head).add(tail);
				}
			}
			for(String head:graph.keySet()) {
				if(graph.get(head).size()==0) {//remove those useless node
					graph.remove(head);
				}
			}
			isDAG(graph);
		}
	}
	public void isDAG(HashMap<String,Set<String>> graph) {
		Set<String> toCheck=graph.keySet();
		boolean cleared=false;
		for(String head:toCheck) {
//			Set<String> visited=new HashSet<String>();
			LinkedHashSet<String> visited=new LinkedHashSet<String>();
			visited.add(head);
			for(String tail:graph.get(head)){
//				System.out.println("\ncontinue check "+tail);
//				String startOfCycle="";
				if(cleared) {
					break;
				}
				if (detectDeadlock(tail,visited,graph)) {
					
					clearYoungest(visited,this.startOfCycle);
					this.startOfCycle="";
					cleared=true;
					break;
				}
			}
		}
//		return false;
	}
	private void clearYoungest(LinkedHashSet<String> visited,String startOfCycle) {
		// TODO Auto-generated method stub
		boolean found=false;
		String YoungestTrct=startOfCycle;
		
		System.out.println("\n visited transctions are: "+visited.toString());
		for(String node:visited){
			System.out.println("\n the age of transaction "+transactionList.get(node).getIniTime()+"----"+node);
			if(node.equals(startOfCycle)) {
				found=true;
			}
			if(found){
				if(transactionList.get(node).getIniTime()>transactionList.get(YoungestTrct).getIniTime()) {
					YoungestTrct=node;
				}
			}
		}
		System.out.println("\n the youngest is "+YoungestTrct);
		GraphNode.remove(YoungestTrct);
		int lY=YoungestTrct.length();
		Iterator it = GraphEdge.iterator(); 
		while(it.hasNext()) {
			try{
				String edge=(String)it.next();
				int lE=edge.length();
				System.out.println("\nedge:"+edge);
				if(edge.startsWith(YoungestTrct)||edge.endsWith(YoungestTrct)) {
					GraphEdge.remove(edge);
				}
			}
			finally{
				break;
			}
		}
		dm_abortTransaction(YoungestTrct);
		
	}
	private void dm_abortTransaction(String youngestTrct) {
		// TODO Auto-generated method stub
		Set<String> abortedTransactions=new HashSet<String>();
		for(Site os:siteList) {
			for(Variable v:os.lockTable.keySet()){
					if(os.lockTable.get(v).rlockSet.contains(youngestTrct)) {//readlock restore
						os.lockTable.get(v).releaseRlock(youngestTrct);
					}
					if(os.lockTable.get(v).wlockSet.contains(youngestTrct)) {//writelock restore
						os.lockTable.get(v).wlockSet.remove(youngestTrct);
						os.lockTable.get(v).releaseWlock();
					}	
				}
			}
		transactionList.get(youngestTrct).isAborted=true;
		waitList.remove(youngestTrct);
		System.out.printf("transaction %s aborted becuase it's yongest transaction in deadlock.",youngestTrct);
//		transactionList.remove(youngestTrct);
	}
	private boolean detectDeadlock(String head, LinkedHashSet<String> visited, HashMap<String, Set<String>> graph) {
		// TODO Auto-generated method stub
		if(visited.contains(head)) {
			System.out.println("\nhere's cycle, begins at "+head);
			this.startOfCycle=head;
			return true; 
		}
		visited.add(head);
		for(String tail:graph.get(head)) {
			return detectDeadlock(tail,visited,graph);
			
		}
//		return false;
		return false;
	}
	
	public void init() {
		//initiate site
		for(int i=1;i<=numOfSites;i++) {
			Site site=new Site(i);
			siteList.add(site);
		}
		//initiate variables in each site
		for(int i=1;i<=numOfVariables;i++) {
			String index=String.valueOf(i);
			Variable variable=new Variable(index);
			variableList.add(variable);
			if(i%2==0) {
				for(Site site:siteList) {
					site.addVariable(index, variable);
				}
			}else {
				getSite(1+i%10).addVariable(index, variable);;
			}
		}
		//read operations
		int time=1;//time tick
		for(String[] op:OpsList) {
			deadlockDetecter();
			if(op[0].equals("begin")) {
				System.out.printf("\nbegin:%s",op[1]);
				Transaction trct=new Transaction(op[1],time);
				System.out.printf("\nTransaction %s is initiated",op[1]);
				transactionList.put(op[1], trct);
				
			}else if(op[0].equals("beginRO")) {
				System.out.printf("\nbeginRO:%s",op[1]);
				Transaction trct=new Transaction(op[1],time);
				trct.setReadOnly();
				System.out.printf("\nTransaction %s is initiated as readonly",op[1]);
				transactionList.put(op[1], trct);
				
			}
			else if(op[0].equals("R")){
				System.out.printf("R:%s\n",op[1]);
				String[] details=op[1].split(",");
				String transaction=details[0];
				String variable=details[1].substring(1);
				int vId=Integer.parseInt(variable);
				if(vId%2==1){//odd variable
					Site s=getSite(1+vId%10);
					if(!s.isFailed()) {
					if(transactionList.get(transaction).isReadonly()) {
						System.out.println("\nreadonly transaction readed successfully");
						System.out.printf("%s,%s",variable,s.getOldVariable(variable));
						}
					else if(s.lockTable.containsKey(s.getVariable(variable))) {//if the variable has already been locked on this site
//				
						if(s.lockTable.get(s.getVariable(variable)).getLockStatus().equals("RL")||s.lockTable.get(s.getVariable(variable)).getLockStatus().equals("NoLock")){//check the lock if it's a readlock
//						if(s.lockTable.get(variable)=="RL") {//check the lock if it's a readlock
							System.out.printf(variable,s.getVariable(variable));
							
						}else {//or it's writelock, wait
//				
							waitList.put(transaction, op);
//							GraphNode.add(s.lockTable.get(s.getVariable(variable)).getWLockTransaction());
							GraphNode.add(transaction);
							GraphEdge.add(transaction+"-"+s.lockTable.get(s.getVariable(variable)).getWLockTransaction());
							System.out.printf("transaction %s wait, because variable %s on site % is writelocked by transaction %s.",transaction,variable,s.getSiteAddr(),s.lockTable.get(s.getVariable(variable)).getWLockTransaction());
						}
					}
					else{
						s.setReadLock(variable,transaction);
						System.out.print("Readed successfully");
						System.out.printf("%s,%s",variable,s.getVariable(variable));
					}
					}
					else {//the site is failed
						System.out.printf("Transaction %s waited due to Site %s failed",transaction,(1+vId%10));//no need to add to deadlock detection
						waitList.put(transaction, op);
					}
				}else {//even variable
					if(transactionList.get(transaction).isReadonly()) {
						for(Site s:siteList) {//randomly read one site
							if((!s.isFailed())&&s.hasVariable(variable)) {
								System.out.print("readonly transaction readed successfully\n");
								System.out.printf("%s,%s",variable,s.getOldVariable(variable));
								break;
							}
						}
						
					}
					else {
						for(Site s:siteList) {//randomly read one site
							if((!s.isFailed())&&s.hasVariable(variable)&&(!s.lockTable.get(s.getVariable(variable)).getLockStatus().equals("WL"))) {
								System.out.print("Readed successfully\n");
								s.setReadLock(variable,transaction);
								System.out.printf("%s,%s",variable,s.getVariable(variable));
								break;
							}
						}
					}
				}
			}else if(op[0].equals("W")) {
				System.out.printf("\nW:%s:",op[1]);
				String[] details=op[1].split(",");
				String transaction=details[0];
				String variable=details[1].substring(1);
				int value=Integer.parseInt(details[2]);
				int vId=Integer.parseInt(variable);
				if(vId%2==1){//odd variable write to one site
					Site s=getSite(1+vId%10);
					if(!s.isFailed()) {
						if(s.getLockStatus(variable).equals("NoLock")){	
							s.setWriteLock(variable, transaction,value);
							System.out.printf("\nTransaction %s writes variable %s on Site %s,value is:%d",transaction,variable,s.getSiteAddr(),value);
						}
						else if(s.getLockStatus(variable).equals("RL")){
							if(s.lockTable.get(variable).ExistsInRlock(transaction)&&s.lockTable.get(variable).getRlockLength()>1)
							{
//								waitList.add(op);
								waitList.put(transaction, op);
								for(String ts:s.lockTable.get(variable).rlockSet) {
									if(!ts.equals(transaction)) {
//										GraphNode.add(ts);	
										GraphEdge.add(transaction+"-"+ts);
									}
								}
								GraphNode.add(transaction);
								System.out.printf("\nTransaction %s waits because variable %s on Site %s has been readlocked by other transactions",transaction,variable,s.getSiteAddr());
							}
							else if(!s.lockTable.get(variable).ExistsInRlock(transaction)&&s.lockTable.get(variable).getRlockLength()>0){
//								waitList.add(op);
								waitList.put(transaction, op);
								for(String ts:s.lockTable.get(variable).rlockSet) {
									if(!ts.equals(transaction)) {
//										GraphNode.add(ts);	
										GraphEdge.add(transaction+"-"+ts);
									}
								}
								GraphNode.add(transaction);
								System.out.printf("\nTransaction %s wait because variable %s on Site %s has been readlocked by other transactions",transaction,variable,s.getSiteAddr());
							}
							
						}else if (s.getLockStatus(variable).equals("WL")){
//							waitList.add(op);
							waitList.put(transaction, op);
							GraphNode.add(transaction);
//							GraphNode.add(s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());	
//							System.out.print("held by:"+s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
							GraphEdge.add(transaction+"-"+s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
							System.out.printf("\nTransaction %s waits because variable %s on Site %s has been writelocked by transaction %s",transaction,variable,s.getSiteAddr(),s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
							
						}
					}
					else {
						//site failed
						System.out.printf("\nTransaction %s wait due to Site %s failed",transaction,(1+vId%10));
//						waitList.add(op);
						waitList.put(transaction, op);//no need to do deadlock transaction, if the site fails forever,than abort transaction
						
					}
					//odd variable write to one site	
				}
				else {//even variable write to all sites, if one site is failed:abort, or can't write to the site because of existed lock: wait
					boolean canWrite=true;
					for(Site s:siteList) {
						if(s.isFailed()) {
							System.out.printf("\nTransaction %s can't write variable to Site %s because site failure ",transaction,variable,s.getSiteAddr());
							continue;

						}else if(s.getLockStatus(variable).equals("RL")){
//							System.out.printf("\n%s",variable);
							if(s.lockTable.get(s.variableList.get(variable)).ExistsInRlock(transaction)&&s.lockTable.get(s.variableList.get(variable)).getRlockLength()>1) {
								System.out.printf("\nTransaction %s waits because can't write to Site %s with readlock held by other transaction",transaction,s.getSiteAddr());
//								waitList.add(op);
//								waitList.put(transaction, op);
								for(String ts:s.lockTable.get(variable).rlockSet) {
									if(!ts.equals(transaction)) {
//										GraphNode.add(ts);	
										GraphEdge.add(transaction+"-"+ts);
									}
								}
								canWrite=false;
//								break;
							}else if(!s.lockTable.get(s.variableList.get(variable)).ExistsInRlock(transaction)&&s.lockTable.get(s.variableList.get(variable)).getRlockLength()>0){
								System.out.printf("Transaction %s waits because can't write to Site %s with readlock held by other transaction\n",transaction,s.getSiteAddr());
//								waitList.put(transaction, op);
								for(String ts:s.lockTable.get(variable).rlockSet) {
									if(!ts.equals(transaction)) {
//										GraphNode.add(ts);	
										GraphEdge.add(transaction+"-"+ts);
									}
								}
								canWrite=false;
//								break;
							}
							
						}else if (s.getLockStatus(variable).equals("WL")){
							System.out.printf("\nTransaction %s waits because can't write to Site %s with writelock held by other transaction %s",transaction,s.getSiteAddr(),s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
//							waitList.add(op);
//							waitList.put(transaction, op);
//							GraphNode.add(s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());	
							GraphEdge.add(transaction+"-"+s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
							canWrite=false;
//							break;
						}

					}
					
					if(canWrite) {
						for(Site s:siteList) {
							if(!s.isFailed()) {
								s.setWriteLock(variable, transaction,value);
								System.out.printf("\ntransaction %s write to temp variable %s at site %s, the value:%d",transaction,variable,s.getSiteAddr(),value);
								}
						}
					}else {
						waitList.put(transaction, op);
						GraphNode.add(transaction);
					}
				}//write to all sites
				
			}else if(op[0].equals("end")) {
				System.out.printf("\nend:%s:",op[1]);
				String transaction=op[1];
				if(transactionList.get(transaction).isReadonly()){
					System.out.printf("\nreadonly transaction %s committed",transaction);
				}
				else if(transactionList.get(transaction).isAborted) {
					System.out.printf("\ntransaction %s can't commit because aborted",transaction);
				}
				else {
					for(Site s:siteList) {//commit
						for(Variable v:s.lockTable.keySet()) {
							if(s.lockTable.get(v).getWLockTransaction().equals(transaction)) {
								v.commit();
								s.lockTable.get(v).releaseWlock();
								System.out.printf("\ntransaction %s commited successfully on site %s,the value of variable %s is:%d",transaction,s.getSiteAddr(),v.getName(),v.getValue());
							}
							if(s.lockTable.get(v).ExistsInRlock(transaction)) {
								s.lockTable.get(v).releaseRlock(transaction);
							}
						}
					}
				}
				
				
			}else if(op[0].equals("dump")) {
				System.out.printf("\ndump:",op);
				
			}else if(op[0].equals("fail")) {
				System.out.printf("\nfail:",op);
				String siteAddr=op[1];
				dm_abortTransactionsOnSite(siteAddr);
				
			}else if(op[0].equals("recover")) {
				System.out.printf("\nrecover:",op);
				
			}
			time+=1;
		}
		
	}
	
	private void dm_abortTransactionsOnSite(String siteAddr) {
		// TODO Auto-generated method stub
		Site s=siteList.get(Integer.parseInt(siteAddr));
		Set<String> abortedTransactions=new HashSet<String>();
		
		//find the transactions needed to be aborted
		for(Variable v:s.lockTable.keySet()) {
			for(String tn:s.lockTable.get(v).rlockSet) {
				abortedTransactions.add(tn);
				s.lockTable.get(v).releaseRlock(tn);
			}
			
		}
		for(Variable v:s.lockTable.keySet()) {
			for(String tn:s.lockTable.get(v).wlockSet) {
				abortedTransactions.add(tn);
			}
			s.lockTable.get(v).releaseWlock();
		}
		//restore all variables in all sites that has been modified by these aborted transactions
		//readlock restore
		for(Site os:siteList) {
			for(Variable v:os.lockTable.keySet()){
				for(String tn:abortedTransactions) {
					if(os.lockTable.get(v).rlockSet.contains(tn)) {//readlock restore
//						os.lockTable.get(v).rlockSet.remove(ts);
						os.lockTable.get(v).releaseRlock(tn);
					}
					if(os.lockTable.get(v).wlockSet.contains(tn)) {//writelock restore
						os.lockTable.get(v).wlockSet.remove(tn);
						os.lockTable.get(v).releaseWlock();
					}
					
				}
				
			}
		}
		
		//fail site
		s.failSite();
	}
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner input = new Scanner(System.in);
//		String path = input.next();
		String path="test/2";
		FileReader fr = new FileReader(path);
		Scanner scanner = new Scanner(fr);
		while (scanner.hasNext()) {
			String[] ops=scanner.nextLine().split("\\(|\\)");
			
			OpsList.add(ops);
		}
		transactionManager tm=new transactionManager();
		tm.init();
	}

}
