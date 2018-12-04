import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class transactionManager {
	static private final int numOfSites=10;
	static private final int numOfVariables=20;
	private ArrayList<Site> siteList=new ArrayList<Site>();
	private ArrayList<Variable> variableList=new ArrayList<Variable>();
	private HashMap<String,Transaction> transactionList=new HashMap<String,Transaction>();
	private static ArrayList<String[]> OpsList= new ArrayList<String[]>();
	private ArrayList<String[]> waitList;
	
	public Site getSite(int index) {
		return siteList.get(index-1);
	}
	public Transaction getTransaction(String name) {
		return transactionList.get(name);
	}
	public Variable getVariable(int index) {
		return variableList.get(index-1);
	}
	
	public  void init() {
		//initiate site
		for(int i=1;i<=numOfSites;i++) {
			Site site=new Site(i);
			siteList.add(site);
		}
		//initiate variables in each site
		for(int i=1;i<=numOfVariables;i++) {
			String index="x"+String.valueOf(i);
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
		int time=1;
		for(String[] op:OpsList) {
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
				System.out.printf("\nR:",op[1]);
				String[] details=op[1].split(",");
				String transaction=details[0];
				String variable=details[1];
				int vId=Integer.parseInt(variable.substring(1));
				if(vId%2==1){//odd variable
					Site s=getSite(1+vId%10);
					if(transactionList.get(transaction).isReadonly()) {
						System.out.printf("%s,%s",variable,s.getOldVariable(variable));
					}
					else if(s.lockTable.containsKey(s.getVariable(variable))) {//if the variable has already been locked on this site
//					if(s.lockTable.containsKey(variable)) {//if the variable has already been locked on this site
						if(s.lockTable.get(s.getVariable(variable)).getLockStatus().equals("RL")||s.lockTable.get(s.getVariable(variable)).getLockStatus().equals("NoLock")){//check the lock if it's a readlock
//						if(s.lockTable.get(variable)=="RL") {//check the lock if it's a readlock
							System.out.printf(variable,s.getVariable(variable));
							
						}else {//or it's writelock, wait
							waitList.add(op);
							System.out.printf("Trasaction %s waits for lock release on variable %s",transaction,variable);
						}
					}
					else{
						s.setReadLock(variable,transaction);
//						s.setVariableLock(variable, "RL");
						System.out.print("Readed successfully  ");
						System.out.printf("%s,%s",variable,s.getVariable(variable));
					}
				}else {//even variable
					if(transactionList.get(transaction).isReadonly()) {
						for(Site s:siteList) {//randomly read one site
							if(s.hasVariable(variable)) {
								System.out.print("readonly transaction readed successfully\n");
								s.setReadLock(variable,transaction);
								System.out.printf("%s,%s",variable,s.getOldVariable(variable));
								break;
							}
						}
						
					}
					else {
						for(Site s:siteList) {//randomly read one site
							if(s.hasVariable(variable)&&(!s.lockTable.get(s.getVariable(variable)).getLockStatus().equals("WL"))) {
								System.out.print("Readed successfully\n");
								s.setReadLock(variable,transaction);
								System.out.printf("%s,%s",variable,s.getVariable(variable));
								break;
							}
						}
					}
				}
				
				
			}else if(op[0].equals("W")) {
				System.out.printf("\nW%s:",op[1]);
				String[] details=op[1].split(",");
				String transaction=details[0];
				String variable=details[1];
				int value=Integer.parseInt(details[2]);
				int vId=Integer.parseInt(variable.substring(1));
				if(vId%2==1){//odd variable
					Site s=getSite(1+vId%10);
					
					}
				else {
					for(Site s:siteList) {
					}
					
				}
				
			}else if(op[0].equals("end")) {
				System.out.printf("\nend:",op);
				
			}else if(op[0].equals("dump")) {
				System.out.printf("\ndump:",op);
				
			}else if(op[0].equals("fail")) {
				System.out.printf("\nfail:",op);
				
			}else if(op[0].equals("recover")) {
				System.out.printf("\nrecover:",op);
				
			}
		}
		
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
