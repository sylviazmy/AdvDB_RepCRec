import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class transactionManager {
	static final int numOfSites=10;
	static final int numOfVariables=20;
	private ArrayList<Site> siteList=new ArrayList<Site>();
	private ArrayList<Variable> variableList=new ArrayList<Variable>();
	private static ArrayList<Transaction> transactionList=new ArrayList<Transaction>();
	private static ArrayList<String[]> OpsList=new ArrayList<String[]>();

	
	public void init() {
		for(int i=0;i<numOfSites;i++) {
			Site site=new Site(i);
			siteList.add(site);
		}
		for(int i=0;i<numOfVariables;i++) {
			String index="x"+String.valueOf(i);
			Variable variable=new Variable(index);
			variableList.add(variable);
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner input = new Scanner(System.in);
		String path = input.next();
		//FileReader fr = new FileR
		FileReader fr = new FileReader(path);
		Scanner scanner = new Scanner(fr);
		int time=1;
		while (scanner.hasNext()) {
			String[] line = scanner.nextLine().split("(|)");
			String cmd=line[0];
			String[] details=line[1].split(",");
			if(details.length==1 &&cmd=="begin") {
				Transaction trct=new Transaction(details[0],time);
				transactionList.add(trct);
			}
			else {
				OpsList.add(line);
			}
			
			
		}

	}

}
