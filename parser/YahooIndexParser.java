package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.*;
import dao.PortfolioDAO;

public class YahooIndexParser extends DataParser {
	
	String[] indices = {"(^DJI)","(^GSPC)","(^TYX)","(^IXIC)",null};
	String USGDP = "2.0";
	String USInflation = "1.6";
	String MarketReturn = "8.73";
	
	public void getPages(){
		StringReader sreader = null;

		PortfolioDAO dao = new PortfolioDAO();
		HashMap idxlist = new HashMap(); 
//		idxlist.put("pagetype", "USINDICES");
		dao.getSetUSIndices(true,idxlist);
		Iterator it = idxlist.keySet().iterator();
		while(it.hasNext()){
	//	String index = (String)idxlist.get(idxlist.size()-1);	
			String index=	(String)it.next();
						
try{
					URL url = new URL("http://finance.yahoo.com/q?s="+index);
				System.out.println(url);
				sreader = DataParser.getPage(url);
				Map dataMap = toParse(index,sreader);	
				idxlist.put(index, dataMap.get(index));
	
					} // try
					
					catch(Exception e){
						e.printStackTrace();
						System.out.println("error message "+e.getMessage());
						
					}
		}// while
		// System.out.println(idxlist);
		Properties pp = new Properties();
		pp.setProperty("Index Value:", USGDP);
		idxlist.put("USGDP", pp);
		pp =  new Properties(); 
		pp.setProperty("Index Value:", USInflation);
		idxlist.put("USInflation", pp);
		pp = new Properties();
		pp.setProperty("Index Value:", MarketReturn);
		idxlist.put("MarketReturn", pp);
		 System.out.println("data map: "+idxlist);
		 dao.getSetUSIndices(false,idxlist);
	}

		
	
	@Override
	public Map toParse(StringReader page) {
		return toParse("^dji",page);
	}
	public Map toParse(String idx,StringReader page) {
		try{
			page.reset();
			} catch(IOException e){}
			
		Scanner scan = new Scanner(page);
		Map stockMap = new Hashtable();
	//	stockMap.put("pagetype", "USINDICES");
		int i=0;
		StringBuilder sbuild = new StringBuilder();
		while(indices[i]!=null){
			
			sbuild = sbuild.append(indices[i]+"|");
			i++;
		}
		sbuild.deleteCharAt(sbuild.length()-1);
		Properties parameters = new Properties();
		String key;
		String val;
//		while( (key= scan.findWithinHorizon(sbuild.toString(), 0))!=null){
			if( (key= scan.findWithinHorizon(sbuild.toString(), 0))!=null){
			val = scan.findWithinHorizon("(-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</span>)", 0);

			val=val.replaceAll("</span>", "");
			val=val.replaceAll("</tr>", "");
			val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
			key=key.replaceAll("(", "");
			key=key.replaceAll(")", "");
			System.out.println(key+" Index List: "+val);
			 parameters.setProperty(key, val);
			
		
//			sbuild=new StringBuilder(sbuild.toString().replace("|"+key,""));
//			System.out.println(sbuild);
		}
	
		stockMap.put(idx, parameters);

		scan.close();
		return stockMap;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 new YahooIndexParser().getPages();
}
}//
