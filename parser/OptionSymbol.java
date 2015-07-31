package parser;

import java.util.GregorianCalendar;
import java.util.Calendar;
import execution.OptionPricing;

public final class OptionSymbol {

	public static String getMoneyness(String optionSymbol, double price){
		String ot = getOptionType(optionSymbol);
		double XP = getStrikePrice(optionSymbol);
		String moneyness = "ATM";
		if( XP > (1+OptionPricing.atMoney)*price )moneyness = "OTM";
			
		else{
			if (XP < (1-OptionPricing.atMoney)*price) moneyness = "ITM";
		}
		
		if (ot.equalsIgnoreCase("P") && moneyness.equals("OTM")) moneyness = "ITM";// reverse moneyness
		else {if(ot.equalsIgnoreCase("P") && moneyness.equals("ITM"))moneyness = "OTM";}
			
		return moneyness;
	}
	public static String getStockSymbol(String optionSymbol){
		String sym;
		if(optionSymbol.endsWith("0")){ // BUG: if option price does not end with 0 then its a toast!
			 sym = (String) optionSymbol.subSequence(0, optionSymbol.length()-15).toString();
		}
		else {
			sym = optionSymbol; // else return as if its a Stock symbol
		}
		return sym;
	}
	public static String getOptionType(String optionSymbol){
		
		String ot = (String) optionSymbol.subSequence(optionSymbol.length()-9, optionSymbol.length()-8).toString();
		return ot;
	}
	
public static double getStrikePrice(String optionSymbol){
		
		StringBuffer op = new StringBuffer( optionSymbol.subSequence(optionSymbol.length()-8, optionSymbol.length()).toString());
		op = op.insert(5, ".");
		return Double.valueOf(op.toString());
	}
public static GregorianCalendar getOptionDate(String optionSymbol){
	
	String date = (String)( optionSymbol.subSequence(optionSymbol.length()-15, optionSymbol.length()-9).toString());
	GregorianCalendar calendar = new GregorianCalendar(Integer.valueOf((String)"20"+date.subSequence(0, 2)),Integer.valueOf((String)date.subSequence(2, 4))-1,Integer.valueOf((String)date.subSequence(4, 6)));
	return calendar ; 
}
public static double getDaysToExpiry(String optionSymbol){
	//from today
	
	GregorianCalendar expirydate=getOptionDate(optionSymbol);
	GregorianCalendar today=new GregorianCalendar();
//	System.out.println("Today: "+today);
	  long daysBetween = 0;  
	  while (today.before(expirydate)) {  
		  today.add(Calendar.DAY_OF_MONTH, 1);  
	    daysBetween++;  
	  }  
	  return daysBetween;  
}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Op Price "+getStrikePrice("RIMM110319P00047550"));
		System.out.println("OT "+getOptionType("EJ110521P00019000"));
		System.out.println("Op Date "+getOptionDate("EJ110128P00019000"));
		System.out.println("Days to Exp: "+getDaysToExpiry("RIMM110319P00047550"));
		System.out.println("STK: "+getStockSymbol("LXK120121P00030000"));
	}

}
