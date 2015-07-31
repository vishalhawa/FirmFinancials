package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import dao.PortfolioDAO;
import jsc.util.*;
import jsc.descriptive.*;
import execution.OptionPricing;
import processing.NormalCurve;



public class YahooHistoryValues extends DataParser {

	public Map toParse(StringReader page) {return new Hashtable();};
	
	String[] historyData = {"Adj Close",null};

	String stock ;
	ArrayList<String> valuelist ;
	ArrayList<String> volumelist;

	double minBin = -0.10; // in percentages
	double maxBin = 0.10;  // in percentages
	double binWidth = 0.005; //in percentages
	public double volatility20 ;  // Short term volatility of past 15 days or 2 weeks
	public double volatility ;  // from daily close values of 90 days
	public double yearlyVol ;
	public double meanChange;   // over the Quarter 65 observations
	public double rollingAvg;
	public double rollingAvg20;
	public double avgVolume;
	public double volumeSpike = 1.0;   // Threshold for volume
	public double adjVolatilityCoeff = 2;   // Filter for outliers (percent Change values)for Adjusted Volatility
	public double kurtosis;
	public String currentPrice; // Looks its added separately to the datalist
	public double prevClose;
	public double highValue; // High in past 90 days
	public double lowValue; // Lowest in past 90 days
	
	public double RSI; // Relative Strength Index
	private int RSIdays=6; // Number of days for RSI calculation
	
	//Call this constructor instead
	public YahooHistoryValues(String asset, String Price){
	/*	if(assest.startsWith("MF-")){
			//assest = assest.replaceAll("MF-", "");
			volatility =0;
			rollingAvg =0;
			avgVolume=0;
		}
		else {
			*/
//		System.out.println("Values->"+currentPrice);
			stock = asset;
			if(Price != null)currentPrice = Price;  
			getVolatility(stock);
//		}
		
	}


	private String getIndex(String qry){
		return qry.substring(2);
	}

	public ArrayList parseHistory(String stk){
		int daysofHistory = 100;
		valuelist = new ArrayList<String>();
		volumelist = new ArrayList<String>();
		volumelist.add("0"); //padding to align volume to stk quote for each day
		URL url = null;
	  SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	  Calendar c = Calendar.getInstance();
	  c.setTime(new Date()); // Now use today date.
	  int emonth = c.get(Calendar.MONTH);
	  int eday = c.get(Calendar.DATE);
	  int eyear = c.get(Calendar.YEAR);
	  c.add(Calendar.DATE, -daysofHistory); // 			 
	  int smonth = c.get(Calendar.MONTH);
	  int sday = c.get(Calendar.DATE);
	  int syear = c.get(Calendar.YEAR);
	  
/*
	  String output = sdf.format(c.getTime());
	  System.out.println(c.get(Calendar.MONTH));
	  System.out.println(c.get(Calendar.DATE));
*/		  
	  try {
		url = new URL("http://real-chart.finance.yahoo.com/table.csv?s="+stk+"&a="+smonth+"&b="+sday+"&c="+syear+"&d="+emonth+"&e="+eday+"&f="+eyear+"&g=d&ignore=.csv") ;
	} catch (MalformedURLException e1) {
		 
		e1.printStackTrace();
	}
	  // Parse the file
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
	 
		try {
	 
			br = new BufferedReader(new InputStreamReader(url.openStream()));
			line = br.readLine();
//			System.out.println(line = br.readLine());
			while ((line = br.readLine()) != null) {
 
			        // use comma as separator
				String[] stkHist = line.split(cvsSplitBy);
//				al.add(Double.valueOf(stkHist[6]).doubleValue());
				valuelist.add(stkHist[6]);
				volumelist.add(stkHist[5]);

			}
//			System.out.println("Values List ->"+valuelist);
//			System.out.println("Volume List ->"+volumelist);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	  
	  try {
		br.close();
	  } catch (IOException e) {
	
		e.printStackTrace();
	  }
	  
	  return valuelist;
	}

	// parseHistory2() - DEPRECATED
	public ArrayList parseHistory2(String stk){
		StringReader sreader = null;
		//STK not used taken from stock class variable
	try{
			
			URL url = new URL("http://finance.yahoo.com/q/hp?s="+stock+"+Historical+Prices");
			 sreader = DataParser.getPage(url);
			} // try
			
			catch(Exception e){
				e.printStackTrace();
				System.out.println("error message "+e.getMessage());
				}
		toParse1(sreader);
 
		ArrayList prop = (ArrayList)valuelist;
		System.out.println("Values List ->"+valuelist);
		System.out.println("Volume List ->"+volumelist);
		
		if (prop ==null){ System.out.println("Page sent does not contain Hist values"); }
		return prop;
	}
	// end Parse History OLD
	
	// toParse() - DEPRECATED 
	public  void toParse1(StringReader page){
		// Value List and Volume List will be populated
		try{
			page.reset();
			} catch(IOException e){}
			
		Scanner scan = new Scanner(page);
		int i=0;
		StringBuilder sbuild = new StringBuilder();
		while(historyData[i]!=null){
			
			sbuild = sbuild.append(historyData[i]+"|");
			i++;
		}
		sbuild.deleteCharAt(sbuild.length()-1);
	//		ArrayList  list = new Properties();
		valuelist = new ArrayList<String>();
		 volumelist = new ArrayList<String>();
		 volumelist.add("0"); //padding to align volume to stk quote for each day
	 
		String val;
		String volume;
		String key= scan.findWithinHorizon(sbuild.toString(), 0);
		while((  volume = scan.findWithinHorizon("((\\d+\\,\\d+)+(\\d+))(</td>)", 0)
)!=null){
			if ((val = scan.findWithinHorizon("(-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td></tr>)", 0))!=null){
			val=val.replaceAll("</td>", "");
			val=val.replaceAll("</tr>", "");
			val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
			valuelist.add(val);
			} // avoid null pointers
//			System.out.println("Volume->"+volume);
			volume=volume.replaceAll("</td>", "");
			volume=volume.replaceAll(",", ""); // to avoid DB field(Number)format error 
			volumelist.add(volume);
		
		}
	
//		Stockprop.setProperty(key, valuelist);
//		stockValueMap.put(stock, valuelist);
//		stockValueMap.put(stock, volumelist);

		scan.close();
		
	}  // End toParse()
	
	private  double getVolatility(String stock){
		
		ArrayList<String> datalist = parseHistory(stock);	

		calculateRollingAvg(datalist);
		calculateRSI(datalist);
		 calculateReturns(datalist);
		 return volatility;
	}
	private void calculateRSI(ArrayList<String> datalist){
		 double sumGain =0;
		 double sumLoss =0;
		 double valChange=0;

		 if(datalist.size()>0 && datalist != null ){
			 datalist.add(0, currentPrice);
		while (RSIdays>0){
//			 System.out.println( datalist.get(RSIdays-1));
			valChange=Double.valueOf(datalist.get(RSIdays-1)) - Double.valueOf(datalist.get(RSIdays));
			if(valChange<0){
				sumGain=sumGain+Math.abs(valChange);
			}else{
				sumLoss=sumLoss+Math.abs(valChange);
			}
			RSIdays--;
		} //while
		 }//if
//		System.out.println("SumGain: "+sumGain);
//		System.out.println("SLoss: "+sumLoss);
		RSI = 100-(100/(1+sumGain/sumLoss));
	}
	
	private void calculateRollingAvg(ArrayList datalist)
	{
		try{
		double retval;
		String oldest = (String)datalist.get(datalist.size()-1);
		String latestVal = (String)datalist.get(0);
		rollingAvg = (Double.valueOf(latestVal) - Double.valueOf(oldest))/Double.valueOf(oldest);
		
 
	
		}catch(Exception e){
			System.out.println(" Data list: "+datalist);
			e.printStackTrace();
			rollingAvg = 0;
		}
	}

	private ArrayList calculateReturns(ArrayList datalist){
		// *** AFTER THIS METHOD THE DATALIST IS INVALID TO USE ***
//		System.out.println("Values->"+datalist);
		prevClose= Double.valueOf((String)datalist.get(0));
		ArrayList arrayChange = new ArrayList();
		double sum=0;
		highValue = prevClose;
		lowValue = prevClose;
		String lastval=null;
//		System.out.println("last: "+datalist.get(0));
		try{
			rollingAvg20= (Double.valueOf((String)datalist.get(0))- Double.valueOf((String)datalist.get(datalist.size()/2))) /Double.valueOf((String)datalist.get(datalist.size()/2))  ;
		while(datalist.size()!=1){

		 lastval = (String)datalist.get(datalist.size()-2);
		String preval = (String)datalist.remove(datalist.size()-1);
		double preVal = Double.valueOf(preval);
	
		if(preVal > highValue) {highValue = preVal; }
		if(preVal < lowValue) lowValue = preVal;
		
		double change= (Double.valueOf(lastval) - Double.valueOf(preval))/preVal;
 
		sum = sum + Math.abs(change);
		arrayChange.add(change);
	//	 System.out.println("Change: "+change);
		} // while
//			 System.out.println("Last Value: "+lastval);
		meanChange = sum/arrayChange.size();
		getStats( arrayChange);
		// arrayChange will be invalid now ***
		}catch(Exception e){
			meanChange = 0;
			volatility = 0;
			yearlyVol = 0;
			e.printStackTrace();
		}
		return arrayChange;
	}
	private void getStats(ArrayList perChange){
		 
//		System.out.println("Change: "+perChange.);
		double[] da = new double[perChange.size()];
		Double[] Da = new Double[perChange.size()];
		int i=0;
		
		//making Copy from Da to da
		for(Double D:(Double[])perChange.toArray(Da)){
			da[i]=D.doubleValue();
			i++;
		}
		FrequencyTable ft = new FrequencyTable("Percent Change",minBin,maxBin,binWidth,da);
//		 System.out.println(": "+ft);

	
			MeanVar mv = new MeanVar(da);

			volatility = mv.getSd(); // Non Adjusted
			calculateKurtosis(da,mv.getN(),mv.getMean(),mv.getSd());
			runAlerts(volatility,da); // ALerts must run on pristine data
		
	// OUTLIERS will be trimmed off after this point		
		/*	i=0;
			double[] XX = new double[perChange.size()];
			for(Double D:(Double[])Da){
				if(Math.abs(D.doubleValue())>adjVolatilityCoeff*volatility) { }
				else{XX[i]=D.doubleValue();
				i++;} // if else
			}
			*/
		
			for(Object d:perChange.toArray()){ 	if(Math.abs((Double)d)>adjVolatilityCoeff*volatility)  perChange.remove(d); 	}
			
			i=0;
			double[] VV = new double[perChange.size()];
			for(Object d:perChange.toArray()){VV[i]= (Double)d;i++; }

			
			 mv = new MeanVar(VV);
			 volatility = mv.getSd(); // This should be adjusted Volatility
			yearlyVol = volatility*Math.sqrt(OptionPricing.tradingDays);
			
			// SHort Term Volatility *** last 10 trading days ***
			i=0;
		 	for(Object d:perChange.toArray()){if(perChange.size()>55 && i>55) {perChange.remove(d);i++;} }
			
			//Create and Copy
			double[] VV20 = new double[perChange.size()];
			i=0;
			for(Object d:perChange.toArray()){VV20[i]= (Double)d;i++; 
	//		System.out.println(""+ d);
			}
	//	 	System.out.println(" LEN"+ perChange.size());
			volatility20 = new MeanVar(VV20).getSd();

//			System.out.println("20 AVG: "+ rollingAvg20);
					
//			System.out.println("XX-Len: "+XX.length);
//			System.out.println("XX: "+XX[XX.length-0]);
	//		System.out.println("XX: "+XX[XX.length-1]);
		//	System.out.println("XX: "+XX[XX.length-2]);

	}
	
	private double calculateKurtosis(double[] array,double N, double mean,double stdev){
		double sum=0;
		for(int i=0;i<N;i++){
			sum = sum + Math.pow(array[i]-mean, 4);
		}
		kurtosis = sum/(N-1)/Math.pow(stdev, 4);
		return kurtosis;
		
	}
	private void runAlerts(Double volatility, double[] da){
		// checking if there are any spikes
	
		PortfolioDAO dao = new PortfolioDAO();
		double price = Double.valueOf(currentPrice)/(1+rollingAvg);
//		System.out.println("Oldest Price: "+price);
		//int i=65;
		int i=volumelist.size()-2;
		for(double change:da){
			price = price*(1+change);
			if(Math.abs(change) > NormalCurve.confidence*volatility && Double.valueOf(volumelist.get(i))> (1+volumeSpike)*Double.valueOf(volumelist.get(i+1))){
				dao.doAlerts(price,stock,change,i,-1+Double.valueOf(volumelist.get(i))/Double.valueOf((volumelist.get(i+1))));
				System.out.println(" Spike: "+Math.round(100*change)+"% days "+i+" Volume: "+volumelist.get(i)+" @Price Level "+ Math.round(price));
			}
			i--;
		}
//		System.out.println("Latest Price "+price);
		dao.releaseConnection();

	}

	/**
	@Override
	public Map toParse(StringReader page) {
		// TODO Auto-generated method stub
		// This should have been used instead of parseHistory()
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		String scrip = "TWTR";
		String CurrentPrice = "527.68";
 
		System.out.println(" HISTORY OF: "+scrip);
			YahooHistoryValues par = new YahooHistoryValues(scrip,CurrentPrice);
			par.parseHistory(scrip);
			System.out.println(scrip+" Daily Vlty: "+par.volatility);
			System.out.println(scrip+" Yearly Vlty: "+par.yearlyVol);
			System.out.println(scrip+" Daily Vlty20: "+par.volatility20);
			System.out.println(" RSI: "+par.RSI);
	}
}
