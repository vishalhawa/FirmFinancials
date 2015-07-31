package parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;
import java.net.URL;

public abstract class DataParser {

	
	
	
	public static DataParser getParser(String host, String path, String qry){
		
		DataParser par = null ; //= new DataParser();
		if(host.contains("icicidirect")){
			par = new FirmDataParser( host,  path,  qry);
		}
		if(host.contains("yahoo")){
			par = new yahooDataParser( host,  path,  qry);
		}
		
		return par;
	}
	public static String getPageString(URL url){
		StringBuilder sbuild=new StringBuilder("");
		try{
//			 url = new URL("http://finance.yahoo.com/q?s="+index);
//		System.out.println(url);
			if(url != null){
			BufferedReader in
			   = new BufferedReader(new InputStreamReader(url.openStream()));

//			 sbuild = new StringBuilder();
			String s;
			while (( s = in.readLine())!= null){
				
				sbuild.append(s);
			}
			}// if
		}//try
		catch(Exception e){
			e.printStackTrace();
		//	System.out.println("error message "+e.getMessage());
			
		}
		return sbuild.toString();
	}
	
	public static StringReader getPage(URL url){
		StringReader sreader = null ;
		
				sreader = new StringReader(getPageString(url).toString());

			return sreader ;
	}

	public static StringReader getPage(File file) throws IOException{
		StringReader sreader = null ;
		StringBuilder sbuild = new StringBuilder();
						
					BufferedReader in
					   = new BufferedReader(new BufferedReader(new FileReader(file)));
					String s;
					while (( s = in.readLine())!= null){
						
						sbuild.append(s);
					}
					sreader = new StringReader(sbuild.toString());
			return sreader ;
	}
	
	public abstract Map toParse(StringReader page) ;
	/**
	 * @param args
	 * @throws IOException 
	 */
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
	StringReader sr =	DataParser.getPage(new File("parser\\samplepage.html"));
	System.out.print(sr);
	}

}
