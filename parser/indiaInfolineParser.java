package parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.net.*;
import dao.InPortfolioDAO;


public class indiaInfolineParser extends DataParser {

	String[] fundData = {"Highest NAV Guarantee Fund</a>","Maximiser Fund</a>",null};
	String HDFCCrestURL ="http://www.indiainfoline.com/Personalfinance/Insurance/Latest-Unit-Values.aspx?AMC=21997&plan=6164&Fund=6734" ;
	//http://www.indiainfoline.com/PersonalFinance/Insurance/HDFC-Standard-Life-Insurance-Company-Ltd/HDFC-SL-CREST/171420634/48287393
// http://www.indiainfoline.com/Personalfinance/Insurance/Latest-Unit-Values.aspx?AMC=21997&plan=6164&Fund=6734
//	String ICICILifeLinkURL ="http://www.indiainfoline.com/PersonalFinance/Insurance/ICICI-Prudential-Life-Insurance-Company-Ltd/ICICI-Pru-LifeLink/171482850/42711284" ;
// try this link instead  http://www.indiainfoline.com/Personalfinance/Insurance/Latest-Unit-Values.aspx?AMC=22005&plan=5447&Fund=3250
	String ICICILifeLinkURL ="http://www.indiainfoline.com/Personalfinance/Insurance/Latest-Unit-Values.aspx?AMC=22005&plan=5447&Fund=3250" ;
	
	String[] fundURL = {HDFCCrestURL,ICICILifeLinkURL};
	
	InPortfolioDAO dao = new InPortfolioDAO();
	public void parseFunds(){
		try{
		 
			for(String furl:fundURL){
		Map fundsMap = toParse(DataParser.getPage(new URL(furl)));
		System.out.println("fundsMap->"+fundsMap);
//		dao.persistData(fundsMap);
			}// for
		return;
		}// try
	 catch( Exception ex){
		 ex.printStackTrace();
	 }
		
	}
	
	@Override
	public Map toParse(StringReader page) {
		try{
			page.reset();
			} catch(IOException e){}
			
		Scanner scan = new Scanner(page);
		Map returnMap = new Hashtable();
		returnMap.put("pagetype", "Funds");
		int i=0;
		StringBuilder sbuild = new StringBuilder();
		while(fundData[i]!=null){  	sbuild = sbuild.append(fundData[i]+"|"); 	i++; 	}
		
		sbuild.deleteCharAt(sbuild.length()-1);
		
		
		Properties fundProp = new Properties();

		String key, index =null;
		while(( key= scan.findWithinHorizon(sbuild.toString(), 0))!=null){
			index = key;
			index=index.replaceAll("</a>", "");
		 /*
			String dateVal = scan.findWithinHorizon("(((\\d+-\\p{Alpha}{3}-\\d\\d))(&nbsp))", 0);
	//		System.out.println("Date Val->"+ dateVal);
			dateVal=dateVal.replaceAll("&nbsp", "");
		*/	
			String val = scan.findWithinHorizon("((\\d+\\,\\d+)*(\\d+\\.?\\d\\d))", 0);
			key=key.replaceAll("</a>", "");
			val=val.replaceAll("</td>", "");
			val=val.replaceAll("<tr>", "null");
			val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
			System.out.println("Scanval->"+key + " vlaue->"+val);
			fundProp.setProperty(key, val);
//			fundProp.setProperty("Date", dateVal);
		}
	 
		returnMap.put(index, fundProp);

		scan.close();
		return returnMap;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new indiaInfolineParser().parseFunds();
	}

}
