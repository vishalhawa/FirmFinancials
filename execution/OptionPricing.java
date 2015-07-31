
/*
 * Option Pricing for US market
 * */
package execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.*;
import java.util.*;
import java.text.*;

import dao.PortfolioDAO;
import processing.FirmDataCollector;
import processing.NormalCurve;
import jsc.distributions.*;
import parser.*;
public class OptionPricing {

	double Rf = 0.0444/365;
	public static double tradingDays = 365 ;// one year
	public static double atMoney = 0.1 ; // ATM boundary
	PortfolioDAO dao = new PortfolioDAO();
	
	
	OptionPricing(){ 
		
		Rf = Double.parseDouble(dao.getIndexdata("^TYX").getProperty("Value1"))/tradingDays/100;
		
		System.out.println("Risk Free is: "+Rf);
	}
	
	public double getOptionPrice(String stock,String optionType, double spotPrice, double strike,String year,String month,String day){
//		String date = String.valueOf(year)+String.valueOf(month);
		String date = year+month+day		;
		System.out.println(constructOptionSymbol(stock,  optionType, strike,  date));
//		fetchURLbyStrikePrice(stock,Double.toString(strike),date);
		Properties opp = getOptionValue(DataParser.getPage(fetchURLbyStrikePrice(stock,Double.toString(strike),date)),constructOptionSymbol(stock,  optionType, strike,  date),"optionDate");
		double op = Double.parseDouble(opp.getProperty("Price" ));
		System.out.println(opp);
		return op;
	}
	
	private String getOptionDate(int year, int month){
		
		// to get third friday of month (Yahoo date will be one day after
		GregorianCalendar calendar = new GregorianCalendar( year, month, 1);
		calendar.set(calendar.WEEK_OF_MONTH,3);
		calendar.set(calendar.DAY_OF_WEEK,calendar.FRIDAY);
		calendar.add(calendar.DAY_OF_MONTH,1);
	//	Yahoo displays one day after date
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		
//		System.out.println("Date->"+ sdf.format(calendar.getTime()));
		return sdf.format(calendar.getTime());

		
	}
	
	private String constructOptionSymbol(String scrip, String optionType, double strikePrice, String date){
		//date eg: 110618 for Jun 18 2011
		String [] sa = Double.toString(strikePrice).split("\\.", 2);

		StringBuffer dollarbuff = new StringBuffer("00000");
	
		dollarbuff.append(sa[0]).reverse().setLength(5);
		dollarbuff.reverse();
		StringBuffer centbuff = new StringBuffer(sa[1]);
		 centbuff.append("000").setLength(3);
//		System.out.println(dollarbuff+"->"+centbuff);
		return scrip+date+optionType+dollarbuff+centbuff;
	}
	URL fetchURLbyMonth(String stocks,String yyyymm) {
		URL url = null;
		try{
//		String option = "test";
//		constructOptionSymbol(stocks.get(option).getProperty("test"),"C",strikePrice,optionDate);
			 url = new URL("http://finance.yahoo.com/q/op?s="+stocks+"&m="+yyyymm);

		System.out.println("URL->"+url);
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("error message "+e.getMessage());
			
		}
		return url;
	}
	URL fetchURLbyStrikePrice(String stocks,String strikePrice,String optionDate) {
		URL url = null;
		try{
//		String option = "test";
//		constructOptionSymbol(stocks.get(option).getProperty("test"),"C",strikePrice,optionDate);
			 url = new URL("http://finance.yahoo.com/q/op?s="+stocks+"&k="+strikePrice);

		System.out.println("URL->"+url);
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("error message "+e.getMessage());
			
		}
		return url;
	}
	private Set getExpirationMonths(String stock,StringReader page){
		String scanstart = "selectbox-link";
		String read = "value=\\p{Punct}\\d*|(</select>)" ;
		Set<String> mmmyy = new HashSet<String>();
	try{
//		StringReader page=DataParser.getPage(new URL("http://finance.yahoo.com/q/op?s="+stock));
		
		Scanner scan = new Scanner(page);
		
		String key = scan.findWithinHorizon(scanstart,0);
		
		if(key != null) {
			String val =null;
//		Scanner shold not go pass </select>
			while(!(val = scan.findWithinHorizon(read,0)).matches("</select>")){
				val = val.replaceAll("value=\"", "");
//				System.out.println("Expiry Months val: "+val);	
				mmmyy.add(val);
			}
			
		}//if
//		getSymbols(stock); // getting options symbols
	}	catch(Exception e) {
			e.printStackTrace();
			System.out.println("error message "+e.getMessage());
			
		} // try-catch
	System.out.println("Expiry Months "+mmmyy);
		return mmmyy;
	}
	
	private HashMap getSymbols(String stock){
		HashMap<String,Properties> sym = new HashMap<String,Properties>();
	
		
		try{
			String price = null;
			String priceBid = null;
			String priceAsk = null;
			String volume ;
			String openint;
			String ivlty;
			String symbol;
			StringReader	page;
			String url = "http://finance.yahoo.com/q/op?s="+stock ;
			System.out.println("URL-> "+url);
			StringReader page1=DataParser.getPage(new URL(url));
			Set<String> months = getExpirationMonths(stock,page1);
			months.add("initial");
			
			for(Object mm:months){ // for
				if(mm.equals("initial")){page = page1;}
				else{ String purl = "http://finance.yahoo.com/q/op?s="+stock+"&date="+mm;
//				System.out.println("Option page "+ purl);
					page = DataParser.getPage(new URL(purl));}
			page.reset();
			
			Scanner scan = new Scanner(page);
			String read = "("+stock+"\\d{6}\\p{Alpha}\\d{8})";
			read = read+"|(END td-applet-options-table)";
			
			String scanstart = "optionsCallsTable";
			String key = scan.findWithinHorizon(scanstart,0);
// 			System.out.println("Key to read: "+ read);
			
//					Scanner shold not go pass ...END td-applet-options-table			
			while(!(symbol = scan.findWithinHorizon(read,0)).matches("END td-applet-options-table")){

//				System.out.println("SYMBOL "+ symbol);

				price = scan.findWithinHorizon("(\\d+\\,\\d+)*(\\d+\\.?\\d*)</div>",0);

				priceBid = scan.findWithinHorizon("(N/A</div>)|((\\d+\\,\\d+)*(\\d+\\.?\\d*)</div>)",0);
				priceAsk = scan.findWithinHorizon("(N/A</div>)|((\\d+\\,\\d+)*(\\d+\\.?\\d*)</div>)",0);
//				System.out.println("ask price "+ priceAsk);
				if(priceBid.equalsIgnoreCase("N/A</div>") || priceAsk.equalsIgnoreCase("N/A</div>")){}
				else{
					// changing price to mid value between Bid and Ask
					priceBid = priceBid.replaceAll("</div>", "");
					priceAsk = priceAsk.replaceAll("</div>", "");
			//		System.out.println(priceBid+" Prices "+ priceAsk);
					price = Double.toString( ((Double.parseDouble(priceBid)+Double.parseDouble(priceAsk))/2));
				
				volume = scan.findWithinHorizon("(\\d+\\,\\d+)*(\\d+)</strong>",0);
				volume = volume.replaceAll("</strong>", "");	
				volume = volume.replaceAll(",", "");	
				
				openint = scan.findWithinHorizon("(\\d+\\,\\d+)*(\\d+)</div>",0);
				openint = openint.replaceAll("</div>", "");	
				openint = openint.replaceAll(",", "");	

				ivlty = scan.findWithinHorizon("(\\d+)*\\.(\\d\\d)%</div>",0);
				ivlty = ivlty.replaceAll("%</div>", "");	
				ivlty = ivlty.replaceAll(",", "");	

				Properties prop = new Properties();
				prop.setProperty("Price", price);
				prop.setProperty("openInt", openint);
				prop.setProperty("volume", volume);
				prop.setProperty("ImpliedVlty", ivlty);
				
				sym.put(symbol, prop);
				} // if -else

			}// while
//			System.out.println(" properties "+ sym);
		}  // for-New URL
			} catch(IOException e){}

			return sym;
	}
	
	
	public void updateOptionPricing(){

		dao.initializeOptions();
		LinkedHashMap<String,Properties> optionsL = dao.getOptionsList();
		getOptionQuotes(optionsL);
	//	System.out.println(optionsL);
	}
	public void  getOptionQuotes(LinkedHashMap<String,Properties> optionsL){
		
	int size = optionsL.size();	
	for(String stk1:optionsL.keySet()){
		
		try{ 
			System.out.println("Size of Options List: "+size);  size--;	
		// loop thru Option stocks
		NormalCurve nc = new NormalCurve();
		double spotP = Double.valueOf(optionsL.get(stk1).getProperty("StockPrice")) ;
		HashMap<String,Object> optionsMap = getSymbols(stk1);
		// Calculate Volatility and Option Price here	
	//	YahooHistoryValues hv  = new YahooHistoryValues(stk);
	 Properties stkdata = dao.getUSAssetdata(stk1);
	double volatility =  Double.parseDouble(stkdata.getProperty("Volatility"));
	double rollingAvg =  Double.parseDouble(stkdata.getProperty("RollingAVG"));
	double Kurtosis =  Double.parseDouble(stkdata.getProperty("Kurtosis"));
	
		System.out.println(stk1+ " Yearly Vol % "+Math.round(100*volatility*Math.sqrt(OptionPricing.tradingDays))+" Kurtosis:"+Kurtosis);
		System.out.println();
		for (String OS :optionsMap.keySet()) { // INNER FOR : All Options Map for the STK

			String ot= OptionSymbol.getOptionType(OS);  
			double XP = OptionSymbol.getStrikePrice(OS);
			
			double days = OptionSymbol.getDaysToExpiry(OS);
			double marketPrice = Double.valueOf(((Properties)optionsMap.get(OS)).getProperty("Price"));
			double IV = nc.NewtonRaphsonIVol(ot, spotP, XP, days,Rf, marketPrice);
			String moneyness = OptionSymbol.getMoneyness(OS, spotP);
			((Properties)optionsMap.get(OS)).setProperty("Moneyness",moneyness);
			((Properties)optionsMap.get(OS)).setProperty("IV",String.valueOf(IV));
			((Properties)optionsMap.get(OS)).setProperty("Volatility",String.valueOf(volatility));		
			((Properties)optionsMap.get(OS)).setProperty("RollingAVG",String.valueOf(rollingAvg));		
			((Properties)optionsMap.get(OS)).setProperty("StockPrice",String.valueOf(spotP));		
			((Properties)optionsMap.get(OS)).setProperty("ExpiryDays",String.valueOf(days));
			
			nc.calculateOptionPrice(spotP, XP, days, Rf, volatility);//calculateOptionPrice()
			
			if(ot.equalsIgnoreCase("C")){ // CALL Option
				((Properties)optionsMap.get(OS)).setProperty("OptionPrice",String.valueOf(nc.callPrice));
				((Properties)optionsMap.get(OS)).setProperty("Delta",String.valueOf(nc.callDelta));
				((Properties)optionsMap.get(OS)).setProperty("Theta",String.valueOf(nc.callTheta));
				((Properties)optionsMap.get(OS)).setProperty("OptionPremiumPer",String.valueOf(100*(XP+marketPrice-spotP)/spotP));		
//				((Properties)optionsMap.get(OS)).setProperty("odds",String.valueOf(nc.calculateOdds(XP+marketPrice,spotP,hv.volatility,days)));						
				((Properties)optionsMap.get(OS)).setProperty("odds",String.valueOf(1-nc.calculateOdds(XP,spotP,volatility,days)));						

			}
			else { // PUT
				((Properties)optionsMap.get(OS)).setProperty("OptionPrice",String.valueOf(nc.putPrice));		
				((Properties)optionsMap.get(OS)).setProperty("Delta",String.valueOf(nc.putDelta));
				((Properties)optionsMap.get(OS)).setProperty("Theta",String.valueOf(nc.putTheta));
				((Properties)optionsMap.get(OS)).setProperty("OptionPremiumPer",String.valueOf(100*(-XP+marketPrice+spotP)/XP));		
//				((Properties)optionsMap.get(OS)).setProperty("odds",String.valueOf(1-nc.calculateOdds(XP-marketPrice,spotP,hv.volatility,days)));		
				((Properties)optionsMap.get(OS)).setProperty("odds",String.valueOf(nc.calculateOdds(XP,spotP,volatility,days)));		
			}// else

			} // for Inner
//		System.out.println("SYM "+optionsMap);	
		optionsMap.put("pagetype", "Options");
						dao.persistUSStocks(optionsMap);
		
		}// try
		catch(Exception e){ System.out.println(stk1+" Error in Update Options: "+optionsL.get(stk1)); e.printStackTrace();}
		} // for esch outer


	} // *** END ***
	
	
	private Properties getOptionValue(StringReader page,String symbol, String fundName){
		// i should use this function

		try{
			page.reset();
			} catch(IOException e){}
			
		Scanner scan = new Scanner(page);

			
		Properties MFprop = new Properties();
		

//		String sfundName=fundName.replace("(", "\\(").replace(")", "\\)");
		String key = scan.findWithinHorizon(symbol,0);
	
			if(key != null) {
		//	System.out.println("Scanval->"+key+"  for fundName: "+fundName);
			//scanner should not go pass </td>
			String val = scan.findWithinHorizon("(-?(\\d+\\,\\d+)*(\\d+\\.\\d*))(</span>|</td>)", 0);
			val=val.replaceAll("</span>", "");
			val=val.replaceAll("</td>", "");
			val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
			key=key.replaceAll("\\(", "\\\\("); // to match up with primary key in DB
			key=key.replaceAll("\\)", "\\\\)"); // to match up with primary key in DB
			key=key.replaceAll("\\*", "\\\\*"); // to match up with primary key in DB
			MFprop.setProperty("Price", val);
//			MFprop.setProperty("fundName", fundName);
//			System.out.println(val+" Name-> "+key);	
			}

		scan.close();
		return MFprop;
	}
	
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		OptionPricing op = new OptionPricing();
		String sym = "AVNR";
		double SP = 5 ;
		double spot = 4.22 ;
		double days = 395;
		String ot = "C";

//		System.out.println("Current OPT Price: "+	op.getOptionPrice(sym, ot, spot, SP, "12", "01","21"));
//		System.out.println(	" Option Name-> "+op.constructOptionSymbol(sym,ot,SP,	op.getOptionDate(2011,Calendar.JUNE)));
//		op.getSymbols("BABA");
//		op.getExpirationMonths("BABA", DataParser.getPage(new File("parser\\samplepage.html")));
		op.updateOptionPricing(); // main Starting point
//		new NYSEQuoter().updatePortflio();
		
		System.out.println("*** END OF Option UPDATES ***"); 

	}

}
