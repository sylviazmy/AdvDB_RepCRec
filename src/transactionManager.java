import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
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
	private LinkedList<String[]> waitList=new LinkedList<String[]>();
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
				if(!transactionList.get(node).isCommited&&!transactionList.get(node).isAborted) {
					graph.put(node,new HashSet<String>());
				}
			}
			
			for(String edge:GraphEdge) {//insert edge
				String head=edge.split("-")[0];
				String tail=edge.split("-")[1];
				if(!graph.containsKey(tail)||transactionList.get(tail).isAborted||transactionList.get(tail).isCommited) {
					continue;
				}
				else if((!transactionList.get(head).isAborted)&&(!transactionList.get(head).isCommited)){
					System.out.print("\ntransaction head:"+head+" status is "+transactionList.get(head).isCommited+"  "+transactionList.get(head).isAborted);
					graph.get(head).add(tail);
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
//				System.out.println("\nedge:"+edge);
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
//		System.out.println("\nthe youngest transaction "+youngestTrct+" is aborted.");
//		waitList.remove(youngestTrct);
		for(String[] op:waitList) {
			if(op[1].contains(youngestTrct)) {
				waitList.remove(op);
			}
		}
		System.out.printf("transaction %s aborted becuase it's youngest transaction in deadlock.",youngestTrct);
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
			else {
				waitList.add(op);
				Iterator<String[]> iter=waitList.iterator();
				while(iter.hasNext()) {
					String[] operation=iter.next();
					if(operation[0].equals("R")){
						System.out.printf("\nR:%s\n",operation[1]);
						String[] details=operation[1].split(",");
						String transaction=details[0];
						String variable=details[1].substring(1);
						int vId=Integer.parseInt(variable);
						if(vId%2==1){//odd variable
							Site s=getSite(1+vId%10);
							if(!s.isFailed()) {
//								System.out.println("\nthe lock status is:"+s.getLockStatus(variable));
								if(transactionList.get(transaction).isReadonly()) {
									System.out.println("\nreadonly transaction readed successfully");
//									waitList.remove(operation);
									iter.remove();
									System.out.printf("\n%s,%s",variable,s.variableList.get(variable).getValueOntick(transactionList.get(transaction).getIniTime()));
									}
								else if(s.getLockStatus(variable).equals("RL")||s.getLockStatus(variable).equals("NoLock")) {//if the variable has already been locked on this site
									s.setReadLock(variable,transaction);
									System.out.printf("\n%s,%s",variable,s.variableList.get(variable).getValue());
									iter.remove();
									
									}
								else if(s.getLockStatus(variable).equals("WL")){//or it's writelock, wait
									GraphNode.add(transaction);
									GraphEdge.add(transaction+"-"+s.lockTable.get(s.variableList.get(variable)).wlockHolder);
									System.out.printf("\ntransaction %s wait, because variable %s on site %s is writelocked by transaction %s.",transaction,variable,s.getSiteAddr(),s.lockTable.get(s.variableList.get(variable)).wlockHolder);
									}
							}
							else {//the site is failed
								System.out.printf("\nTransaction %s waited due to Site %s failed",transaction,(1+vId%10));//no need to add to deadlock detection
//								waitList.put(transaction, op);
							}
						}else {//even variable
							if(transactionList.get(transaction).isReadonly()) {
								for(Site s:siteList) {//randomly read one site
									if((!s.isFailed())&&s.hasVariable(variable)) {
//										System.out.print("\nreadonly transaction readed successfully\n");
//										waitList.remove(operation);
										iter.remove();
										System.out.printf("\n%s,%s",variable,s.variableList.get(variable).getValueOntick(transactionList.get(transaction).getIniTime()));
										break;
									}
								}
								
							}
							else {
								for(Site s:siteList) {//randomly read one site
//									System.out.println(s.getSiteAddr()+" "+variable);
									if((!s.isFailed())&&	(!s.lockTable.get(s.variableList.get(variable)).getLockStatus().equals("WL"))) {
//										System.out.print("\nReaded successfully");
										iter.remove();
										s.setReadLock(variable,transaction);
										System.out.printf("\n%s,%s",variable,s.getVariable(variable));
										break;
									}
								}
							}
						}
					}else if(operation[0].equals("W")) {
						System.out.printf("\nW:%s:",operation[1]);
						String[] details=operation[1].split(",");
						String transaction=details[0];
						String variable=details[1].substring(1);
						int value=Integer.parseInt(details[2]);
//						System.out.println("\n--"+variable);
						int vId=Integer.parseInt(variable);
						if(vId%2==1){//odd variable write to one site
							Site s=getSite(1+vId%10);
							if(!s.isFailed()) {
								if(s.getLockStatus(variable).equals("NoLock")){	
									s.setWriteLock(variable, transaction,value);
//									waitList.remove(operation);
									iter.remove();
									System.out.printf("\nTransaction %s writes variable %s on Site %s,value is:%d",transaction,variable,s.getSiteAddr(),value);
									System.out.println("\nthe lock is "+s.getLockStatus(variable));
								}
								else if(s.getLockStatus(variable).equals("RL")){
									if(s.lockTable.get(s.variableList.get(variable)).ExistsInRlock(transaction)&&s.lockTable.get(s.variableList.get(variable)).getRlockLength()>1)
									{
//										waitList.add(op);
//										waitList.put(transaction, op);
										for(String ts:s.lockTable.get(variable).rlockSet) {
											if(!ts.equals(transaction)) {
//												GraphNode.add(ts);	
												GraphEdge.add(transaction+"-"+ts);
											}
										}
										GraphNode.add(transaction);
										System.out.printf("\nTransaction %s waits because variable %s on Site %s has been readlocked by other transactions",transaction,variable,s.getSiteAddr());
									}
									else if(!s.lockTable.get(s.variableList.get(variable)).ExistsInRlock(transaction)&&s.lockTable.get(s.variableList.get(variable)).getRlockLength()>0){
//										waitList.add(op);
//										waitList.put(transaction, op);
										for(String ts:s.lockTable.get(s.variableList.get(variable)).rlockSet) {
											if(!ts.equals(transaction)) {
//												GraphNode.add(ts);	
												GraphEdge.add(transaction+"-"+ts);
											}
										}
										GraphNode.add(transaction);
										System.out.printf("\nTransaction %s wait because variable %s on Site %s has been readlocked by other transactions",transaction,variable,s.getSiteAddr());
									}
									
								}else if (s.getLockStatus(variable).equals("WL")){
//									waitList.add(op);
//									waitList.put(transaction, op);
									GraphNode.add(transaction);
//									GraphNode.add(s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());	
//									System.out.print("held by:"+s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
									GraphEdge.add(transaction+"-"+s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
									System.out.printf("\nTransaction %s waits because variable %s on Site %s has been writelocked by transaction %s",transaction,variable,s.getSiteAddr(),s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
									
								}
							}
							else {
								//site failed
								System.out.printf("\nTransaction %s wait due to Site %s failed",transaction,(1+vId%10));
//								waitList.add(op);
//								waitList.put(transaction, op);//no need to do deadlock transaction, if the site fails forever,than abort transaction
								
							}
							//odd variable write to one site	
						}
						else {//even variable write to all sites, if one site is failed:abort, or can't write to the site because of existed lock: wait
							boolean canWrite=true;
							for(Site s:siteList) {
								if(s.isFailed()) {
									System.out.printf("\nTransaction %s can't write variable to Site %s because site failure ",transaction,s.getSiteAddr());
									continue;

								}else if(s.getLockStatus(variable).equals("RL")){
//									System.out.printf("\n%s",variable);
									if(s.lockTable.get(s.variableList.get(variable)).ExistsInRlock(transaction)&&s.lockTable.get(s.variableList.get(variable)).getRlockLength()>1) {
										System.out.printf("\nTransaction %s waits because can't write to Site %s with readlock held by other transaction",transaction,s.getSiteAddr());
//										waitList.add(op);
//										waitList.put(transaction, op);
										for(String ts:s.lockTable.get(s.variableList.get(variable)).rlockSet) {
											if(!ts.equals(transaction)) {
//												GraphNode.add(ts);	
												GraphEdge.add(transaction+"-"+ts);
											}
										}
										canWrite=false;
//										break;
									}else if(!s.lockTable.get(s.variableList.get(variable)).ExistsInRlock(transaction)&&s.lockTable.get(s.variableList.get(variable)).getRlockLength()>0){
										System.out.printf("Transaction %s waits because can't write to Site %s with readlock held by other transaction\n",transaction,s.getSiteAddr());
//										waitList.put(transaction, op);
										for(String ts:s.lockTable.get(s.variableList.get(variable)).rlockSet) {
											if(!ts.equals(transaction)) {
//												GraphNode.add(ts);	
												GraphEdge.add(transaction+"-"+ts);
											}
										}
										canWrite=false;
//										break;
									}
									
								}else if (s.getLockStatus(variable).equals("WL")){
									System.out.printf("\nTransaction %s waits because can't write to Site %s with writelock held by other transaction %s",transaction,s.getSiteAddr(),s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
//									waitList.add(op);
//									waitList.put(transaction, op);
//									GraphNode.add(s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());	
									GraphEdge.add(transaction+"-"+s.lockTable.get(s.variableList.get(variable)).getWLockTransaction());
									canWrite=false;
//									break;
								}

							}
							
							if(canWrite) {
								for(Site s:siteList) {
									if(!s.isFailed()) {
										s.setWriteLock(variable, transaction,value);
										System.out.printf("\ntransaction %s write to temp variable %s at site %s, the value:%d",transaction,variable,s.getSiteAddr(),value);
										}
								}
//								waitList.remove(operation);
								iter.remove();
							
							}else {
//								waitList.put(transaction, op);
								GraphNode.add(transaction);
							}
						}//write to all sites
					}
					else if(operation[0].equals("end")) {
						System.out.printf("\nend:%s:",operation[1]);
//						System.out.println("aborted?"+transactionList.get(operation[1]).isAborted);
						String transaction=operation[1];
						if(transactionList.get(transaction).isReadonly()){
//							waitList.remove(operation);
							iter.remove();
							transactionList.get(transaction).isCommited=true;
							System.out.printf("\nreadonly transaction %s committed",transaction);
						}
						else if(transactionList.get(transaction).isAborted) {
//							waitList.remove(operation);
							iter.remove();
							System.out.printf("\ntransaction %s can't commit because aborted",transaction);
						}
						else {
							for(Site s:siteList) {//commit
								for(Variable v:s.lockTable.keySet()) {
									if(s.lockTable.get(v).getWLockTransaction().equals(transaction)) {
										v.commit(time);
										s.lockTable.get(v).releaseWlock();
										System.out.printf("\ntransaction %s commited successfully on site %s,the value of variable %s is:%d",transaction,s.getSiteAddr(),v.getName(),s.variableList.get(v.getName().substring(1)).getValue());
									}									
									if(s.lockTable.get(v).ExistsInRlock(transaction)) {
										s.lockTable.get(v).releaseRlock(transaction);
									}
								}
							}
							System.out.printf("\ntransaction %s committed",transaction);
//							waitList.remove(operation);
							transactionList.get(transaction).isCommited=true;
							iter.remove();
						}
						
						
					}else if(operation[0].equals("dump")) {
						System.out.printf("\ndump:",operation);
//						waitList.remove(operation);
						iter.remove();
						
					}else if(operation[0].equals("fail")) {
//						waitList.remove(operation);
						iter.remove();
						System.out.printf("\nfail:",operation);
						String siteAddr=operation[1];
						dm_abortTransactionsOnSite(siteAddr);
						
					}else if(operation[0].equals("recover")) {
//						System.out.println("\nrecover:"+operation[1]);
						String site=operation[1];
//						waitList.remove(operation);
						iter.remove();
						siteList.get(Integer.parseInt(site)-1).recover();
						
					}
				}
			}
			time+=1;
		}
		
	}
	
	private void dm_abortTransactionsOnSite(String siteAddr) {
		// TODO Auto-generated method stub
		Site s=siteList.get(Integer.parseInt(siteAddr)-1);
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
//		System.out.println("\nabort transactions on site."+siteAddr);
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
					transactionList.get(tn).isAborted=true;
//					System.out.println("set transaction aborted:"+tn+" "+transactionList.get(tn).isAborted);
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
