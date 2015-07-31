package execution;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;


 
public class PostTest {

	
	PostTest() {
		
		try {
			  System.out.print("Enter Number of Loops to Run "); 

		      //  open up standard input 
		      BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
		      String loops = br.readLine(); 
	        // Construct data
	        String data = URLEncoder.encode("startname", "UTF-8") + "=" + URLEncoder.encode("Tata", "UTF-8");
	        // data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" + URLEncoder.encode("value2", "UTF-8");	    
	        // Send data
	        for(int i=0;i<=Integer.parseInt(loops);i++){
	        URL url = new URL("http://content.icicidirect.com/mutualfund/getfundfamily.asp");
	        url = new URL("http://finance.yahoo.com/q/ks?s=WIN");
	        //URL url = new URL("http://content.icicidirect.com/research/companysnapshot.asp?icicicode=ABB");
	        URLConnection conn = url.openConnection();
	        conn.setDoOutput(true);
	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(data);
	        wr.flush();
	    
	        // Get the response
	        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    	StringBuilder sbuild = new StringBuilder();
	        String line;
	        while ((line = rd.readLine()) != null) {
	            // Process line...
	        	sbuild.append(line);
	        	//System.out.println(line);
	        }

	        System.out.println("Looping: "+i);
			StringReader sreader = new StringReader(sbuild.toString());
			getFundValue(sreader,"WIN","Beta:");
	        wr.close();
	        rd.close();
	        } // for loop
	    } catch (Exception e) {
	    	e.printStackTrace(); 
	    }

	}
	
	
	private Properties getFundValue(StringReader page,String fundFamily, String fundName){
		
		try{
			page.reset();
			} catch(IOException e){}
			
		Scanner scan = new Scanner(page);

		int i=0;
		StringBuilder sbuild = new StringBuilder();
				
		Properties MFprop = new Properties();
		

//		System.out.println("Name-> "+scan.findWithinHorizon("\\p{Alpha}+(\\p{Punct}?)",0));
		String key = scan.findWithinHorizon("Profit Margin (ttm):",0);
		
	//		System.out.println("Scanval->"+"Beta:");
			//scanner should not go pass </td>
			String val = scan.findWithinHorizon("(-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))</td>|<tr>", 0);
			val=val.replaceAll("</td>", "");
			val=val.replaceAll("<tr>", "null");
			val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
			MFprop.setProperty("pagetype", "MF");
	//	MFprop.setProperty(key, val);
		
		System.out.println("MFProp->"+MFprop);
		scan.close();
		return MFprop;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new PostTest();
	}

}
