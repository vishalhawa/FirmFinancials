package execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.*;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import dao.InPortfolioDAO;
import parser.*;


public class FundQuoter {

	
// Specifc to hdfcsec.com 	String[][] funds = {{"ICICI"},{"Maximiser Fund"}};
	InPortfolioDAO dao = new InPortfolioDAO();
	
	FundQuoter(){ }
	
	URL fetchURL(LinkedHashMap<String,Properties> funds,Object fund,short page) throws MalformedURLException{
		URL url=null;
		if (fund.toString().contentEquals("MF-ICICI PRU")){
//			ICICI-PRULIFE
//			System.out.println("Running ICICI..");
//			 url = new URL("https://www.iciciprulife.com/ipru/Nav_values.jsp");
			// IndiaIfoline parser
		}else{
			
			url = new URL("http://www.hdfcsec.com/Scheme/"+fund.toString().replace("MF-", "")+"-"+funds.get(fund).getProperty("FundName").replaceAll("- ","").replaceAll(" ", "-")+".html");
	 
		}	

		System.out.println("URL->"+url);
		return url;
	}
	
	public int getNAV(){

		LinkedHashMap<String,Properties> funds = dao.getMFListing();
		System.out.println("MF Set"+funds);
		int NAV =0;
		URL url;
		Properties prop = null;
		Map<String,Object> fundMap = null;
		short page = 1;
		
		for (Object fund:funds.keySet()){
		// loop thru Keys
			
		try{

			do {  // while
			url = fetchURL(funds,fund,page);
			page++;
			
	
			 fundMap = new Hashtable<String,Object>();
			fundMap.put("pagetype", "Funds");
		
			prop = getFundValue(DataParser.getPage(url),fund.toString(),funds.get(fund.toString()).getProperty("FundName"));
			
			} while(prop.isEmpty()&& page < 5);	// try 5 times
		
			 fundMap.put(fund.toString(), prop);
			//System.out.println("fundMap->"+fundMap);
		 dao.persistData(fundMap);
	
	}// try
	catch(Exception e) {
		e.printStackTrace();
		System.out.println("error message "+e.getMessage());
		
	}
	page = 1;
		} // for esch
			
		return NAV;
	}
	
	
private Properties getFundValue(StringReader page,String fundFamily, String fundName){
		

		try{
			page.reset();
			} catch(IOException e){}
			
		Scanner scan = new Scanner(page);

			
		Properties MFprop = new Properties();
		

		String sfundName=fundName.replace("(", "\\(").replace(")", "\\)");
		String key = scan.findWithinHorizon(sfundName,0);
		System.out.println("Name->"+sfundName);		
			if(key != null) {
		//	System.out.println("Scanval->"+key+"  for fundName: "+fundName);
			//scanner should not go pass </td>
			String val = scan.findWithinHorizon("(-?(\\d+\\,\\d+)*(\\d+\\.\\d*))(</div>|</td>|&#160;</span>)", 0);
			// /td is for ICICI maximiser fund icici site 
			//	System.out.println("Value->"+val);
			val=val.replace("&#160;</span>", "");
			val=val.replaceAll("</div>", "");
			val=val.replaceAll("</td>", "");
			val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
			key=key.replaceAll("\\(", "\\\\("); // to match up with primary key in DB
			key=key.replaceAll("\\)", "\\\\)"); // to match up with primary key in DB
			key=key.replaceAll("\\*", "\\\\*"); // to match up with primary key in DB
			MFprop.setProperty(fundName, val);
			}

		scan.close();
		return MFprop;
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		new FundQuoter().getNAV();

	}

}
