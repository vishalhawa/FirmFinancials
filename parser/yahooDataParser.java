package parser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;



public class yahooDataParser extends DataParser{

	
	String[] indexData = {"Index Value:","Open","Prev Close:","Change:",null};

	String index = "%5EBSESN";
	
	public yahooDataParser(){}
	yahooDataParser(String host,String  path, String qry){
		index=getIndex(qry);
//		System.out.println(index);
	}
	
	private String getIndex(String qry){
		return qry.substring(2);
	}
	
	public Properties parseSensex(StringReader page){
		
		Map sensexmap = toParse(page);
		System.out.println(sensexmap);
		Properties prop = (Properties)sensexmap.get("%5EBSESN");
		if (prop ==null){ System.out.println("Page sent does not contain BSE sensex"); }
		return prop;
	}
	public Map toParse(StringReader page){
		
		try{
			page.reset();
			} catch(IOException e){}
			
		Scanner scan = new Scanner(page);
		Map indexMap = new Hashtable();
		indexMap.put("pagetype", "index");
		int i=0;
		StringBuilder sbuild = new StringBuilder();
		while(indexData[i]!=null){
			
			sbuild = sbuild.append(indexData[i]+"|");
			i++;
		}
		sbuild.deleteCharAt(sbuild.length()-1);
		
		
		Properties indexprop = new Properties();

		String key;
		while(( key= scan.findWithinHorizon(sbuild.toString(), 0))!=null){
//			System.out.println("Scanval->"+key);
			//scanner should not go pass </td>
			String val = scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</span>|</td>| ))|<tr>", 0);
			val=val.replaceAll("</td>", "");
			val=val.replaceAll("</span>", "");
			val=val.replaceAll("<tr>", "null");
			val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
			indexprop.setProperty(key, val);
		}
		
		indexMap.put(index, indexprop);
//		System.out.println("CompProp->"+companyMap);
		scan.close();
		return indexMap;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		
		try{
			
			URL url = new URL("http://in.finance.yahoo.com/q?s=BSE-OILGAS.BO");
			BufferedReader in
			   = new BufferedReader(new InputStreamReader(url.openStream()));
//			   = new BufferedReader(new FileReader("companysnapshot.txt"));
			StringBuilder sbuild = new StringBuilder();
			String s;
			while (( s = in.readLine())!= null){
				
				sbuild.append(s);
			}
			
			StringReader sreader = new StringReader(sbuild.toString());
			yahooDataParser par = new yahooDataParser(url.getHost(),url.getPath(),url.getQuery());
			Map dataMap = par.parseSensex(sreader);	
			
					 System.out.println("dataMap: "+dataMap);//compprop.propertyNames()); //
//					 System.out.println("sensexMap: "+par.parseSensex(sreader));
				
			} // try
			
			catch(Exception e){
				e.printStackTrace();
				System.out.println("error message "+e.getMessage());
				
			}



	}

}
