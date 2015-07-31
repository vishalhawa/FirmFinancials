package execution;

import java.util.*;
import java.net.*;
import java.io.*;

public class RollingAverage {

	 GregorianCalendar cal ;
	 int duration ;
	 String scrip = "subex.ns";
	 String hosturl = "http://in.finance.yahoo.com/q/hp?s=";
	
	RollingAverage(int days){
		duration = days;
		cal = new GregorianCalendar() ;
		System.out.println("Calculating Average for Scrip: "+ scrip);
		System.out.println("Calculating Average for Days: "+ days);
		double gain = calculateAverage(cal,duration,scrip);
		System.out.println(scrip+" GAIN%: " +gain*100);
		System.out.println("The Annualized GAIN%: " +(gain*100)*(365/duration)+"\n");
		 gain = calculateAverage(cal,duration,"%5EBSESN");
		System.out.println("Compared to BSE GAIN%: " +gain*100);
		System.out.println("The Annualized GAIN%: " +(gain*100)*(365/duration));
	}
	
	public double calculateAverage(GregorianCalendar todaydate,int duration,String scrip){
		
		GregorianCalendar pastdate = (GregorianCalendar)cal.clone();
		pastdate.add(Calendar.DAY_OF_MONTH, -duration);

//		System.out.println("day of week:"+pastdate.get(Calendar.DAY_OF_WEEK));
	//	System.out.println(todaydate.getTime()+" Calculating Avg-> "+pastdate.getTime());	
		Map<String,Double> Quotes = getValues(scrip,todaydate,pastdate); 
		
		System.out.println("Mape "+Quotes);
		double gain = (Quotes.get(String.format("%1$te-%1$tb-%1$ty", todaydate))- Quotes.get(String.format("%1$te-%1$tb-%1$ty", pastdate)))/Quotes.get(String.format("%1$te-%1$tb-%1$ty", pastdate)) ;
		return gain;
	}
	
	private HashMap<String,Double> getValues(String scrip,GregorianCalendar today,GregorianCalendar past){
		
		HashMap<String,Double> quoteMap = new HashMap<String,Double>();
		try{
		URL url = new URL(hosturl+scrip);
		BufferedReader in  = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder sbuild = new StringBuilder();
		String s;
		while (( s = in.readLine())!= null){
			
			sbuild.append(s);
		}
		
		while(quoteMap.size() != 2){
			quoteMap.clear();
			today.add(Calendar.DAY_OF_MONTH, -1);
			past.add(Calendar.DAY_OF_MONTH, -1);

		StringReader sreader = new StringReader(sbuild.toString());
		sreader.reset();
		Scanner scan = new Scanner(sreader);
		
		String key;
		String currdate = String.format("%1$te-%1$tb-%1$ty", today);
		String pastdate = String.format("%1$te-%1$tb-%1$ty", past);
	//	System.out.println("Lookig for Dates->"+currdate+" and past "+pastdate);
		while(( key= scan.findWithinHorizon(currdate+"|"+pastdate, 0))!=null){
		//	System.out.println("Scanval->"+key);
			//scanner should not go pass </td>
			String val;
			 scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
			 scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
			 scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
			scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
			 scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
			 val = scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
			
			val=val.replaceAll("</td>", "");
			val=val.replaceAll("<tr>", "null");
			val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
//			System.out.println("Val->"+val);
			//prop.entrySet(key,Double.parseDouble(val)); //setProperty(key, val);
			quoteMap.put(key, Double.parseDouble(val));
		}// inner while
		}//  outer while
		}// try
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return quoteMap;
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//  bugs  9-Mar-08 scans hits 19-Mar-08
		new RollingAverage(45);

	}

}
