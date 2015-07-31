package parser;
import java.net.*;
import java.io.*;
import java.util.*;


public class FirmDataParser extends DataParser{
	

	String[] companyData = {"Market Capitalization","Open Price","Sales Income","Book Value","P/E","Return on Net Worth","Dividend Yield %","Close Price","EPS2","EPS1","Actual","Debt / Equity","GPM \\(\\%\\)","NPM \\(\\%\\)","OPM \\(\\%\\)",null};


	String sectID =null;
	String icicicode ;
	
	public FirmDataParser() {
		// TODO Auto-generat 
		
	}
	
	FirmDataParser(String host, String path, String qry){
		
		String pquery = null;
		 icicicode = getICICIcode( qry);
//		if (purl != null)  {  pquery = purl.getQuery();}
		if (icicicode == null){
			
			 sectID = getSectorID( qry);	
		}else{
			 sectID = getSectorID( pquery);  //to get SectorID: query will either have icicicode or sectorID hence using pquery 	
		}
	}
	
	public Map toParse(StringReader page){
		
		
		return toParse(page,sectID,icicicode);
	}
	private Map toParse(StringReader page,String sectorID, String icicicode){
		
		Map firmDataMap=null;
		if(icicicode == null ){
			firmDataMap=getSectorOutlook(page,sectorID);
		}else
		{
			firmDataMap=getCompanySnapshot(page,icicicode,sectorID);
		}
		
		return firmDataMap;
	}
	
	
	
	private Map getSectorOutlook(StringReader page,String sectorID){
		try{
		page.reset();
		} catch(IOException e){}
		
		Scanner scan = new Scanner(page);
		Map sectorMap = new Hashtable();
		
//	print(page);
		sectorMap.put("pagetype", "sector");
		String icicicode;
		while((icicicode = scan.findWithinHorizon("icicicode=\\p{Alpha}+", 0)) != null){
//			System.out.println("icici->"+icicicode);
			List valuelist = new ArrayList();
			valuelist.add(sectorID);
// adding FirmName
			valuelist.add(getCellValue((scan.findInLine("color=\"#000000\"> </font> | color=\"#000000\">(\\p{Punct}|\\p{Blank}|\\p{Alpha})+</font>"))).replace("'", ""));
		
			
			for(int x=0;x<13;x++){
				// Numbers seperated by Whitespace may not be picked up eg: color="#000000"> 16.29<
			
				valuelist.add(getCellValue(scan.findInLine("color=\"#000000\"> </font> | color=\"#000000\">\\p{Graph}+</font>")));
			}
			// for rating /Weights
			valuelist.add(getCellValue(scan.findInLine("color=\"#000000\">(\\p{Alpha}+\\p{Blank}\\p{Alpha}+)</font>")));

			sectorMap.put(getICICICode(icicicode), valuelist);
		}
//		System.out.println("sectorMap->"+sectorMap);
		scan.close();
		return sectorMap;
	}
	
	
	private Map getCompanySnapshot(StringReader page,String icicicode, String sectorID){
		
		try{
			page.reset();
			} catch(IOException e){}
			
		Scanner scan = new Scanner(page);
		Map companyMap = new Hashtable();
		companyMap.put("pagetype", "company");
		int i=0;
		StringBuilder sbuild = new StringBuilder();
		while(companyData[i]!=null){
			
			sbuild = sbuild.append(companyData[i]+"|");
			i++;
		}
		sbuild.deleteCharAt(sbuild.length()-1);
		
		
		Properties compprop = new Properties();
		

//		System.out.println("Name-> "+scan.findWithinHorizon("\\p{Alpha}+(\\p{Punct}?)",0));
		scan.findWithinHorizon("Industry",0);
		scan.next();
		compprop.setProperty("Industry", scan.next());
		String key;
		while(( key= scan.findWithinHorizon(sbuild.toString(), 0))!=null){
//			System.out.println("Scanval->"+key);
			//scanner should not go pass </td>
			String val = scan.findWithinHorizon("(-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))</td>|<tr>", 0);
			val=val.replaceAll("</td>", "");
			val=val.replaceAll("<tr>", "null");
			val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
//			System.out.println("Patt->"+val);			
			compprop.setProperty(key, val);
		}
		
		companyMap.put(icicicode, compprop);
//		System.out.println("CompProp->"+companyMap);
		scan.close();
		return companyMap;
	}
	
	static String getCellValue(String row){
	
//		System.out.println("row->"+row);

		String val = row.replaceAll("color=\"#000000\">", "").replaceAll("</font>", "").replace("&nbsp;", "");

//		String val = row.replaceAll("color=\"#000000\">", "").replaceAll("\\p{Alpha}|&|;|<|/|>", ""); //row.substring(16);
//		String val = s.findWithinHorizon("</td> | -?(\\d+\\,\\d+)*(\\d+\\.?\\d*)", 0);
		if(val==null){
			val = "";
		}

//		System.out.println("val->"+val);

		return val;
	}
	
	private String getICICICode(String icicicode){ // This is original of this class
		
//		System.out.println("getICICIcode.. Original");	
		return icicicode.substring(10);
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
	
private String getICICIcode(String query){ // copied from firmdata collector

	if(query != null && query.startsWith("icicicode")){
		
		query = query.substring(10);
	}
	else {
		query = null;	
	}
//		System.out.println("getICICIcode.. copied from FDC");	
		return query;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		try{
			FirmDataParser par = new FirmDataParser();

			BufferedReader in
			   = new BufferedReader(new InputStreamReader(new URL("http://content.icicidirect.com/research/companysnapshot.asp?icicicode=aardru").openStream()));
//			   = new BufferedReader(new FileReader("companysnapshot.txt"));
			StringBuilder sbuild = new StringBuilder();
			String s;
			while (( s = in.readLine())!= null){
				
				sbuild.append(s);
			}
			
			StringReader sreader = new StringReader(sbuild.toString());
//			FirmDataParser.getCellValue("color=\"#000000\"> 638.98&nbsp;</font></td>  ");

			Map dataMap = par.toParse(sreader,"5","XXX");	
			
		
					Properties compprop = (Properties)dataMap.get("XXX");
					
					 System.out.println("compprop: "+compprop);//compprop.propertyNames()); //
			
				
			} // try
			
			catch(Exception e){
				e.printStackTrace();
				System.out.println("error message "+e.getMessage());
				
			}


	}// main
	
	static void print(StringReader page){
		try{
			char[] ch={};
			page.reset();
			while (( page.read(ch,0,100)) != -1){
				System.out.println("::"+ch);	
				
			}
			}
		catch(Exception ex){
			
		}
	}

}
