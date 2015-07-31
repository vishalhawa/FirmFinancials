package processing;
 

import java.net.*;
import java.io.*;
import java.util.*;

import dao.InPortfolioDAO;

import parser.DataParser;


public class FirmDataCollector {

	InPortfolioDAO portfoliodao;
	public static GregorianCalendar calendar = new GregorianCalendar();
	public static String mmdd = ""+(1+calendar.get(Calendar.MONTH))+"-"+calendar.get(Calendar.DATE);
	
	public FirmDataCollector() {
		// TODO Auto-generated constructor stub
		 portfoliodao = new InPortfolioDAO();
//		 new yahooDataParser().parseSensex(page);
	}

	public void callback(URL url, StringReader page,URL purl){
		
		String path = url.getPath();
		String query = url.getQuery();
		String host = url.getHost();
		String pquery = null;
		
		DataParser par = DataParser.getParser(host, path, query);
		if (purl != null)  {  pquery = purl.getQuery();}
	
		
		if(page != null){Map firmDataMap=par.toParse(page);
		
//		fdpar.toParse(page, sectID, icicicode);

		
		new EvaluateCompanyProp(firmDataMap).evaluateVolatility();
		new EvaluateCompanyProp(firmDataMap).evaluateBeta();
//		System.out.println("firmDataMap->"+firmDataMap);
		if(firmDataMap !=null){ portfoliodao.persistData(firmDataMap); }
		} // if
		
//		showSystemStat();
		
	}
	
	private String getSectorID(String query){
	
		if(query != null && query.startsWith("SectorXid")){
		
			query = query.substring(10); 
			query = query.replaceAll("\\p{Alpha}|\\p{Punct}", "");
		}
		else {
			query = null;	
		}
		
//		System.out.println("getSectorID.."+ query);	
		return query;
	}
	
private String getICICIcode(String query){

	if(query != null && query.startsWith("icicicode")){
		
		query = query.substring(10);
	}
	else {
		query = null;	
	}
//		System.out.println("getICICIcode.."+ query);	
		return query;
	}
	
public void showSystemStat(){
	
	
	Runtime runtime = Runtime.getRuntime();
	
	System.out.println("JVM: Total memory: "+runtime.totalMemory());
	System.out.println("JVM: Max memory  : "+runtime.maxMemory());
	System.out.println("JVM: Free memory : "+runtime.freeMemory());
	Long i = (runtime.freeMemory()-runtime.totalMemory());
	System.out.println("JVM: Consumed memory is : "+i+" of Total Mem");
//	if(i<0.2){ 	System.out.println("JVM: Initiating GC...");System.gc();}
}




protected void finalize() {
	

}

/**
	 * @param args
	 */
	public static void main(String[] args)  {
		// TODO Auto-generated method stub


		
		try{
			System.setErr( new PrintStream("ErrorLog.txt"));
			System.setOut( new PrintStream("OutLog.txt"));
/*			
			URL url = new URL("http://content.icicidirect.com/research/SectorWatchDatail.asp?SectorXid=6");
			BufferedReader in
			   = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder sbuild = new StringBuilder();
			String s;
			while (( s = in.readLine())!= null){
				
				sbuild.append(s);
			}
			
			StringReader sreader = new StringReader(sbuild.toString());
			
			 //.callback(url, sreader);
			  
			  */
		//	new Spider(new FirmDataCollector(),"SectorWatch.asp");
			//			FirmDataParser.getCellValue("color=\"#000000\"> 638.98&nbsp;</font></td>  ");

			//throw new Exception("Hello Exception1");
	
			} // try
			
			catch(Exception e) {
				e.printStackTrace();
				System.out.println("error message "+e.getMessage());
			//	throw new Exception("Nested Exception2");
				
			}

	}

}
