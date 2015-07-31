package execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.*;
import java.text.*;
import parser.*;

import processing.DCFProcessor;
import processing.FirmDataCollector;
import processing.NormalCurve;
import dao.PortfolioDAO;


public class NYSEQuoter {

	String parse  = "Yield:|50-Day Moving Average|Avg Vol \\(3 month\\)|Revenue \\(ttm\\):|Beta:|Forward Annual Dividend Rate|Forward P/E|PEG Ratio|Price/Sales|Price/Book|Diluted EPS|Forward Annual Dividend Yield|Mean Recommendation \\(this week\\):|Median Target:|No. of Brokers:";
//	String opinionparse  = "Mean Recommendation (this week):|Median Target:";
	String fundParse = ">Category:<|Yield:|Year-to-Date Return:|Fund Family:" ; //Morningstar Rating:";
	PortfolioDAO dao = new PortfolioDAO();
	DCFProcessor dcf = new DCFProcessor();
	
	NYSEQuoter(){  }

	public void  getStockQuotes(){
		dao.initializeStocks();
		LinkedHashMap<String,Properties> scrips = dao.getNYSEticker();
		getStockQuotes(scrips);
	}

		public void  getStockQuotes(LinkedHashMap<String,Properties> scrips){
				
		int counter =0;

		URL url,url2,url3=null;
		StringReader page = null;
		for (String fund:scrips.keySet()){
			System.out.println("NYSE Set "+(scrips.size()-(counter++))+" "+fund );	
		try{ 
			Map<String,Object> Map = new Hashtable<String,Object>();
			
			
		if(fund.startsWith("MF-")){	 
			fund = fund.replaceAll("MF-", "");
			url = new URL("http://finance.yahoo.com/q/pr?s="+fund);
			
			System.out.println("Fetching URL->"+url);
				page = getPage( url) ; //new StringReader(getPage( url));
				Map.put("pagetype", "funds");
		}
		else{	url = new URL("http://finance.yahoo.com/q/ks?s="+fund);

		 		url2 = new URL("http://finance.yahoo.com/q/ao?s="+fund);
	 
		 //		System.out.println(fund+" Sector->"+scrips.get(fund));
		 		if(scrips.get(fund).getProperty("Sector").startsWith("ETF-")){ // ETF
		 			url3 = new URL("http://finance.yahoo.com/q/pr?s="+fund);
		 		} // inner IF for ETF
		 		System.out.println("Fetching URL->"+url);
		 	page = new StringReader(DataParser.getPageString( url) + DataParser.getPageString( url2)+DataParser.getPageString( url3));
				Map.put("pagetype", "scrips");
//				System.out.println(" URL Done"+ Runtime.totalMemory());
		}
			 
		Properties prop;
		
		if(Map.get("pagetype").toString().equals("funds")){ 
			prop = getFundValue(page,fund.toString(),fund);
		}
		else {	//Stocks
			 prop = getStockValue(page,fund.toString(),fund);
//			 System.out.println("Prop->"+prop);
		YahooHistoryValues hv  = new YahooHistoryValues(fund,prop.getProperty("("+fund.toString()+")"));
		prop.setProperty("RollingAVG", String.valueOf(hv.rollingAvg));
		prop.setProperty("Volatility", String.valueOf(hv.volatility));		
		prop.setProperty("Volatility20", String.valueOf(hv.volatility20));
		prop.setProperty("rollingAvg20", String.valueOf(hv.rollingAvg20));
		prop.setProperty("Spike", String.valueOf(hv.volatility));	 // not sure if this is used
		prop.setProperty("Kurtosis", String.valueOf(hv.kurtosis));	
		prop.setProperty("prevClose", String.valueOf(hv.prevClose));	
		prop.setProperty("highValue", String.valueOf(hv.highValue));	
		prop.setProperty("lowValue", String.valueOf(hv.lowValue));	
		prop.setProperty("RSI", String.valueOf(hv.RSI));	
			if( ((String)scrips.get(fund).get("Sector")).startsWith("ETF-") && prop.get("Yield:")!=null){// ETF
			
				prop.put("Forward Annual Dividend Yield",prop.get("Yield:"));	
			} // ETF
 
		}//else
		Map.put(fund.toString(), prop);
//	System.out.println("calling DCF->");	
		dcf.calculateUSDCF(fund, Map);
//	System.out.println("NYSE MAP call DAO->"+Map);
		 dao.persistUSStocks( Map);
//	System.out.println("NYSE MAP->persisited");
	System.out.println("");
		
	}// try
	catch(Exception e) {
		e.printStackTrace();
		System.out.println(fund+" error message "+e.getMessage());
		
	}
	
		} // for esch

	}
	
	
private StringReader getPage(URL url){
return DataParser.getPage(url);
}


private	Properties getFundValue(StringReader page,String sector, String ticker){
	

	try{
		page.reset();
		} catch(IOException e){}
		
	Scanner scan = new Scanner(page);
		
	Properties fundprop = new Properties();
	String key="";
	String val="";
	String keys = "\\("+ticker+"\\)"+"|"+fundParse;
	while( (key = scan.findWithinHorizon(keys,0))!=null){
	
		//System.out.println("Scanval->"+key);
		//assuming all ratios have decimals
		if(key.equals("("+ticker+")")||key.equals("Yield:")||key.equals("Year-to-Date Return:")){
		 val = scan.findWithinHorizon("(>(-?(\\d+\\,\\d+)*(\\d+\\.\\d*))%?<)", 0);  //
		}
		else{
			val = scan.findWithinHorizon(">\\p{Alpha}(\\p{Blank}|\\p{Alnum}|-)+\\p{Alpha}<",0);
		}
//		System.out.println("Value: "+val);
		key=key.replaceAll(">", "");
		key=key.replaceAll("<", "");	key=key.replaceAll(":", "");
		val=val.replaceAll(">", "");
		val=val.replaceAll("<", "");	val=val.replaceAll("%", "");
		val=val.replaceAll("N/A", "0");
	
		fundprop.setProperty(key, val);
	} // while

	scan.close();
	return fundprop;
	
} // get Fnd Value
	
private Properties getStockValue(StringReader page,String sector, String ticker){
		

		try{
			page.reset();
			} catch(IOException e){}
			
		Scanner scan = new Scanner(page);
			
		Properties Stockprop = new Properties();
		String key="";
		String keys = "\\("+ticker+"\\)"+"|"+parse;
		while( (key = scan.findWithinHorizon(keys,0))!=null){
		
	//		System.out.println("Scanval->"+key);
			//assuming all ratios have decimals
			String val = scan.findWithinHorizon("(NaN)|(>N/A<)|(>(-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))%?(M|B)?(</td>|</span>))", 0);
			val=val.replaceAll("NaN", "0");
			val=val.replaceAll(">", "");
			val=val.replaceAll("<", "");
			val=val.replaceAll("N/A", "0");
			val=val.replaceAll("%", "");
			val=val.replaceAll("/td", "");
			val=val.replaceAll("/span", "");
			val=val.replaceAll(",", "");
			if (val.contains("B")) { val=val.replaceAll("B", "");  val=String.valueOf(Double.valueOf(val).doubleValue()*1000000000); }
			if (val.contains("M")) { val=val.replaceAll("M", "");  val=String.valueOf(Double.valueOf(val).doubleValue()*1000000); }
		 
	//		System.out.println("Value->"+val);
			Stockprop.setProperty(key, val);
		}

		scan.close();
		return Stockprop;
	}
	
	public void updatePortflio(){

		LinkedHashMap<String,Properties> stockmap = new LinkedHashMap<String,Properties>();
		NormalCurve nc = new NormalCurve();
		dao.getSetUSPortfolio( false, stockmap); //get scrips
//		System.out.println("Updating Portfolio..->"+stockmap);
		
		LinkedHashMap<String,Properties> portfolioStocks =new LinkedHashMap<String,Properties>();
		for (String asset:stockmap.keySet()){
			if(stockmap.get(asset).getProperty("Remarks").equalsIgnoreCase("S")||stockmap.get(asset).getProperty("Remarks").equalsIgnoreCase("MF")){
			 
			//	System.out.println(stockmap.get(asset));
				Properties pp = new Properties();
				pp.setProperty("StockPrice", String.valueOf(stockmap.get(asset).get("closeprice")));
				pp.setProperty("Sector", String.valueOf(stockmap.get(asset).get("Sector")));
				portfolioStocks.put(stockmap.get(asset).getProperty("scrip"), pp);
			}else{
				Properties stkdata=dao.getUSAssetdata(OptionSymbol.getStockSymbol(stockmap.get(asset).getProperty("scrip")));
				 portfolioStocks.put(OptionSymbol.getStockSymbol(stockmap.get(asset).getProperty("scrip")), stkdata);
			}//if-else
		
//			System.out.println("NYSE Set "+ portfolioStocks);
		}//for
		
		getStockQuotes(portfolioStocks);  // Run Stock Quotes
		new OptionPricing().getOptionQuotes(portfolioStocks);  // Run Options
		dao.getSetUSPortfolio( false, stockmap); //get scrips AGAIN

		
		Object[] keyset = stockmap.keySet().toArray();
		for(int x=0; x<keyset.length ;x++ ){
			Properties prop = (Properties)stockmap.get(keyset[x]);
	//		System.out.println("Prop Object..->"+prop);	
			
			double closeprice =Double.parseDouble(prop.get("closeprice").toString());
			double SellPrice =Double.parseDouble(prop.get("SellPrice").toString());
			double buyprice =Double.parseDouble(prop.get("buyprice").toString());
			double duration = Double.parseDouble(prop.get("durationdays").toString());
			
			double number = Double.parseDouble(prop.get("number").toString());

			double growthratepercent=0 ;
			double annualizedgrowthratepercent = 0;
			double profit = 0;
			double costBasis = 0;
			double commission = 7.95;
			double BEP = 0;
			double oddsBEP = 0;
			
			if (SellPrice==0.0) // Active Portfolio
			{
			  profit = number*(closeprice-buyprice);
			 growthratepercent = 100*(closeprice-buyprice)/buyprice;
			 annualizedgrowthratepercent = 100*(365.0/duration)*(Math.log(closeprice/buyprice));
			 costBasis = number*buyprice;
			// commission is 9.95 (until May 2011 and 7.95 /trade later
	
			//Note: SP trades are not implemented
			if(prop.get("Remarks").equals("SC")){  // additional for Options
				String stk = OptionSymbol.getStockSymbol(prop.get("scrip").toString());
				double Xprice = OptionSymbol.getStrikePrice(prop.get("scrip").toString());
				Properties stkdata = dao.getUSAssetdata(stk);
				double stkcloseprice = Double.parseDouble(stkdata.getProperty("StockPrice"));
				BEP = stkcloseprice+ (buyprice - closeprice-Double.parseDouble(prop.get("theta").toString()))/Double.parseDouble(prop.get("delta").toString());
				oddsBEP=nc.calculateOdds(BEP,stkcloseprice,Double.parseDouble( prop.get("Volatility").toString()),Double.parseDouble( prop.get("ExpiryDays").toString()));
				prop.put("StockPositionValue",number*(-buyprice-Xprice+stkcloseprice));
				prop.put("BlockedValue",number*Xprice);
				prop.put("oddsBEP",oddsBEP);	
				prop.put("BEP",BEP);
			}// if
			if(prop.get("Remarks").equals("BP")){
				String stk = OptionSymbol.getStockSymbol(prop.get("scrip").toString());
				double Xprice = OptionSymbol.getStrikePrice(prop.get("scrip").toString());
				Properties stkdata = dao.getUSAssetdata(stk);
				double stkcloseprice = Double.parseDouble(stkdata.getProperty("StockPrice"));
				BEP = stkcloseprice+ (buyprice - closeprice-Double.parseDouble(prop.get("theta").toString()))/Double.parseDouble(prop.get("delta").toString());
				oddsBEP=nc.calculateOdds(BEP,stkcloseprice,Double.parseDouble( prop.get("Volatility").toString()),Double.parseDouble( prop.get("ExpiryDays").toString()));
				prop.put("StockPositionValue",number*(-buyprice-Xprice+stkcloseprice));
				prop.put("BlockedValue",number*Xprice);
				prop.put("oddsBEP",oddsBEP);		
				prop.put("BEP",BEP);
			}
			if(prop.get("Remarks").equals("BC")){
				String stk = OptionSymbol.getStockSymbol(prop.get("scrip").toString());
				double Xprice = OptionSymbol.getStrikePrice(prop.get("scrip").toString());
			
				Properties stkdata = dao.getUSAssetdata(stk);
				double stkcloseprice = Double.parseDouble(stkdata.getProperty("StockPrice"));
				BEP = stkcloseprice+ (buyprice - closeprice-Double.parseDouble(prop.get("theta").toString()))/Double.parseDouble(prop.get("delta").toString());
				oddsBEP=1-nc.calculateOdds(BEP,stkcloseprice,Double.parseDouble( prop.get("Volatility").toString()),Double.parseDouble( prop.get("ExpiryDays").toString()));
				prop.put("StockPositionValue",number*(-buyprice-Xprice+stkcloseprice));
				prop.put("BlockedValue",number*Xprice);
				prop.put("oddsBEP",oddsBEP);	
				if (BEP == Double.POSITIVE_INFINITY){ prop.put("BEP",Double.MAX_VALUE);}
				else {prop.put("BEP",BEP);}
			}	
			}// if
			else {   // Executed Portfolio
				System.out.println("Entering Executed Portfolio..->");
				LinkedHashMap<String,Properties> scrip = new LinkedHashMap<String,Properties>();
	//			Properties prop = new Properties();
				 profit = number*(SellPrice-buyprice);
				 growthratepercent = 100*(SellPrice-buyprice)/buyprice;
				 double durationHeld = Double.parseDouble(prop.get("durationHeld").toString());
				 annualizedgrowthratepercent = 100*(365/durationHeld)*(Math.log(SellPrice/buyprice));
				 prop.put("Symbol", OptionSymbol.getStockSymbol(prop.get("scrip").toString()));
				 prop.put("Option", prop.get("scrip").toString());
				 prop.put("Type", prop.get("Remarks").toString());
				 scrip.put("NewRecord", prop);
				 dao.getSetUSExecutedPortfolio(true, scrip);
			}//else
		
			prop.put("commission",commission);
			prop.put("costBasis",costBasis);
			prop.put("profit",profit);
			prop.put("AbsoluteGrowthP",growthratepercent);
			prop.put("APY",annualizedgrowthratepercent);	
			System.out.println(prop.get("scrip")+" ->"+profit +" "+ growthratepercent +" "+ annualizedgrowthratepercent);
	//		resetPricingMark(closeprice,FirmDataCollector.mmdd,prop);
		} // for
		 

		dao.getSetUSPortfolio( true, stockmap); //Store
		updateExecutedPortflio();
	}

	public void updateExecutedPortflio(){
		LinkedHashMap<String,Properties> stockmap = new LinkedHashMap<String,Properties>();

		dao.getSetUSExecutedPortfolio( false, stockmap); //get scrips
		System.out.println("Updating Executed Portfolio..->");
		Object[] keyset = stockmap.keySet().toArray();
		for(int x=0; x<keyset.length ;x++ ){
			Properties prop = (Properties)stockmap.get(keyset[x]);
			
//			double closeprice =Double.parseDouble(prop.get("closeprice").toString());
			double SellPrice =Double.parseDouble(prop.get("SellPrice").toString());
			double buyprice =Double.parseDouble(prop.get("buyprice").toString());
			String type = (String)prop.get("Type");
			double durationHeld = Double.parseDouble(prop.get("durationHeld").toString());
			double number = Double.parseDouble(prop.get("number").toString());
			
			double commission = calCommission(type,number);
			double effectiveBuyprice = buyprice + commission/number;
			
			double growthratepercent=0 ;
			double annualizedgrowthratepercent = 0;
			double profit = 0;
			 profit = number*(SellPrice-effectiveBuyprice); // after taking out Commission
			 growthratepercent = 100*(SellPrice-effectiveBuyprice)/effectiveBuyprice;
			 annualizedgrowthratepercent = 100*(365/durationHeld)*(Math.log(SellPrice/effectiveBuyprice));
			 double CostBasis = number*effectiveBuyprice; // Cost Basis incudes commission
				 
				prop.put("profit",profit);
				prop.put("AbsoluteGrowthP",growthratepercent);
				prop.put("APY",annualizedgrowthratepercent);	
				prop.put("CostBasis",CostBasis);	
				prop.put("Commission",commission);	
			//	prop.put("buyprice",buyprice);	// Never Update the original Buy Price
		}// for
	//	System.out.println("Updating stockmap..->"+stockmap);
		dao.getSetUSExecutedPortfolio( true, stockmap); //Store
	}
	 public static double calCommission(String type, double number){
		double commission=7.95; // for Stk default - Simple Sell or sell thru options?
		 
	
		if(!type.equalsIgnoreCase("S")){ // Options
			
			commission = 2*(7.95+.75*number/100); // Options- ALL for sharebuilder buy + sell
		}
		if(type.endsWith("Z")){ // SCZ ; SPZ??
			 commission = commission-7.95-.75*number/100; // its only one trade
			  
			}
		if(type.endsWith("X")){
			if(type.startsWith("SC")){ 
				// SCX - Assigned
				commission = commission+20-7.95-0.75*number/100;
			 } // its only $20
			else{ commission = commission+30-7.95-0.75*number/100; }// its only $30 = BCX or BPX
			 
		}
		if(type.equalsIgnoreCase("MF")){
			commission =0;
		}
		return commission;
	}
	
	private void resetPricingMark(double todayprice,String todaydate,Properties stockDtls){
		double highprice = Double.parseDouble(stockDtls.getProperty("HighPrice"));
		double lowprice = Double.parseDouble(stockDtls.getProperty("LowPrice"));
		String highdate = (String)stockDtls.get("HighDate");
		String lowdate = (String)stockDtls.get("LowDate");
		
		if(todayprice >highprice ){ // we have a new high
	
			stockDtls.put("LowPrice",highprice);
			stockDtls.put("LowDate",highdate);
			stockDtls.put("HighPrice",todayprice);
			stockDtls.put("HighDate",todaydate);
			
		}
	if(todayprice <lowprice ){ // we have a new LOW

		stockDtls.put("HighPrice",lowprice);
		stockDtls.put("HighDate",lowdate);
		stockDtls.put("LowPrice",todayprice);
		stockDtls.put("LowDate",todaydate);
		}
	else{ // Mid price
		
	}
	
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
//		new YahooIndexParser().getPages();
		NYSEQuoter nyseQ = new NYSEQuoter();
		nyseQ.getStockQuotes();
// Bloc king for now..		new OptionPricing().updateOptionPricing();
		nyseQ.updatePortflio();
		System.out.println("*** END OF Quotes UPDATES ***"); 
	}

}
