package execution;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

import dao.PortfolioDAO;

import parser.OptionSymbol;
import processing.NormalCurve;
import java.sql.Date;

// This Class is for RSI(5) superlayered by Bollinger Bands ..mock porTfolio 
public class USPortfolio_Mock {
	
	double MaxGrowth = 50; // in Percentage terms
	double RSI_Puts = 60; // RSI threshold value for PUTS CLOSURE
	double MaxLoss = -30; // in Percentage terms, for the position to be closed
	double BDeviation = 1.5; //Bollinger Band deviation , Volatility is taken for 20 days
	double OptionPrice_T = 4.0 ; // Threshold for option Price

	PortfolioDAO dao = new PortfolioDAO();
	
	public LinkedHashMap selectOptions_Mock(){

		HashMap<String,Properties> optionsMap = new HashMap<String,Properties>();
		NormalCurve nc = new NormalCurve();
		dao.getSetUSPortfolio_Mock( false, optionsMap); //get Options- RSI(5) superlayered by Bollinger Bands
		
		System.out.println("RSI Filtered..->"+optionsMap.size());
		
		// Apply Business Logic to filter options
		LinkedHashMap<String,Properties> mockPortfolioStocks =new LinkedHashMap<String,Properties>();
		for (String asset:optionsMap.keySet()){
			double decay = 100*(Double)optionsMap.get(asset).get("theta")/ (Double)optionsMap.get(asset).get("price");
			//	System.out.println(stockmap.get(asset));
			if ( Math.abs(decay) < 1.0 && (Double)optionsMap.get(asset).get("price") < OptionPrice_T && Math.abs((Double)optionsMap.get(asset).get("delta")) > 0.6 && Math.abs((Double)optionsMap.get(asset).get("delta")) < 0.8){
				Properties pp = new Properties();
				pp.setProperty("Volatility", String.valueOf(optionsMap.get(asset).get("Volatility")));
				pp.setProperty("optionPrice", String.valueOf(optionsMap.get(asset).get("price")));
				
				pp.setProperty("decay", String.valueOf(decay));
				pp.setProperty("delta", String.valueOf(optionsMap.get(asset).get("delta")));
				pp.setProperty("RSI", String.valueOf(optionsMap.get(asset).get("RSI")));
				mockPortfolioStocks.put(asset, pp);
		 
	//			Properties stkdata=dao.getUSAssetdata(OptionSymbol.getStockSymbol(asset));
	//			mockPortfolioStocks.put(asset, stkdata);
			} // if
			else{
//				optionsMap.remove(asset);
			}
		
		
		}//for
			return mockPortfolioStocks;
	}
	
    private void	buyPuts_Mock(LinkedHashMap<String,Properties> selectedOptions){
	System.out.println("Delta,Decay & Price filtered.. "+ selectedOptions.size());

	int cnt =0;
	for (String option:selectedOptions.keySet()){
		Properties stkdata=dao.getUSAssetdata(OptionSymbol.getStockSymbol(option));
		// Apply Bollinger Band filter

		double bollingerBandUP = Double.valueOf(stkdata.getProperty("MA50"))*(1.0+BDeviation*Double.valueOf(stkdata.getProperty("Volatility20")));
		if(OptionSymbol.getOptionType(option).equalsIgnoreCase("P") && Double.valueOf(stkdata.getProperty("StockPrice"))  > bollingerBandUP ){
				 cnt++;
			//			System.out.println(cnt+" option Set "+ option+" "+selectedOptions.get(option)); 
				 selectedOptions.get(option).setProperty("Remarks","BP"); 
		}

	} // for
	System.out.println("BP: open positions "+ selectedOptions);
	dao.getSetUSPortfolio_Mock(true,selectedOptions);
	System.out.println("PUTS: Bollinger filtered.. "+ cnt);
	}
    
    private void	buyCalls_Mock(LinkedHashMap<String,Properties> selectedOptions){
    	System.out.println("Delta,Decay & Price filtered.. "+ selectedOptions.size());

    	int cnt =0;
    	for (String option:selectedOptions.keySet()){
    		Properties stkdata=dao.getUSAssetdata(OptionSymbol.getStockSymbol(option));
    		// Apply Bollinger Band filter

    		double bollingerBandDN = Double.valueOf(stkdata.getProperty("MA50"))*(1.0-BDeviation*Double.valueOf(stkdata.getProperty("Volatility20")));
    		if(OptionSymbol.getOptionType(option).equalsIgnoreCase("C") && Double.valueOf(stkdata.getProperty("StockPrice"))  < bollingerBandDN ){
    				 cnt++;
    			//			System.out.println(cnt+" option Set "+ option+" "+selectedOptions.get(option)); 
    				 selectedOptions.get(option).setProperty("Remarks","BC"); 
    		}

    	} // for
    	System.out.println("BC: open positions "+ selectedOptions);
  //  	dao.getSetUSPortfolio_Mock(true,selectedOptions);
    	System.out.println("CALLS: Bollinger filtered.. "+ cnt);
    }
    
    private void	buyPuts_Mock_Alerts(){

	HashMap<String,Properties> optionsMap = new HashMap<String,Properties>();
	NormalCurve nc = new NormalCurve();
	dao.getSetUSPortfolio_Mock_Alerts( false, optionsMap); // Retrieve
	System.out.println("Alerts filtered.. "+ optionsMap.size());
    }
	
public void updateMockPortflio(){
	// Update and close options
	LinkedHashMap<String,Properties> mockOptionsMap = new LinkedHashMap<String,Properties>();

	dao.setOpenPositions_Mock( false, mockOptionsMap); //get OPtions_Open
	System.out.println("Updating MOCK Portfolio..");
	Object[] keyset = mockOptionsMap.keySet().toArray();
	for(int x=0; x<keyset.length ;x++ ){
		Properties prop = (Properties)mockOptionsMap.get(keyset[x]);
		
//		double closeprice =Double.parseDouble(prop.get("closeprice").toString());
		double CurrentPrice =Double.parseDouble(prop.get("CurrentPrice").toString());
		double buyprice =Double.parseDouble(prop.get("BuyPrice").toString());
		String type = (String)prop.get("Type");
		double durationdays = Double.parseDouble(prop.get("durationdays").toString()); // TBD
		double Shares = Double.parseDouble(prop.get("Shares").toString());
		double RSI = Double.parseDouble(prop.get("RSI").toString());
		
		double commission = 2*(7.95+.75*Shares/100);
		double effectiveBuyprice = buyprice + commission/Shares;
		
		double growthratepercent=0 ;
		double annualizedgrowthratepercent = 0;
		double profit = 0;
		 profit = Shares*(CurrentPrice-effectiveBuyprice); // after taking out Commission
		 growthratepercent = 100*(CurrentPrice-effectiveBuyprice)/effectiveBuyprice;
// NR		 annualizedgrowthratepercent = 100*(365/durationdays)*(Math.log(CurrentPrice/effectiveBuyprice));
		 double CostBasis = Shares*effectiveBuyprice; // Cost Basis incLudes commission
			 
			prop.put("profit",profit);
			prop.put("AbsoluteGrowthP",growthratepercent);
			prop.put("APY",annualizedgrowthratepercent);	
			prop.put("CostBasis",CostBasis);	
			prop.put("Commission",commission);	
		//	prop.put("buyprice",buyprice);	// Never Update the original Buy Price
			
		// Logic to close the position	for PUTS
			if(type.equalsIgnoreCase("BP")){
			if(growthratepercent < MaxLoss || RSI > RSI_Puts ){
				prop.put("SellPrice",CurrentPrice);
	
				Date sellDate = new Date(((Date)prop.get("BuyDate")).getTime()+ (long)durationdays*86400000) ;
				// System.out.println("Date "+sellDate);
				prop.put("SellDate",sellDate);
				System.out.println("Closing Option MOCK Portfolio..-> "+keyset[x]+"  Gain/Loss: "+growthratepercent);	
			}
			}else{ }
			
	}// for
 
	dao.setOpenPositions_Mock( true, mockOptionsMap); //Store
}

void getBollingerAlerts(){
	HashMap<String,Properties> optionsMap = new HashMap<String,Properties>();
	dao.getSetUSBollinger_Alerts(false, optionsMap);
	System.out.println(optionsMap);
}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		USPortfolio_Mock mp = 	new USPortfolio_Mock();
		mp.getBollingerAlerts();
		/*
		mp.buyPuts_Mock(mp.selectOptions_Mock());
		mp.buyCalls_Mock(mp.selectOptions_Mock());
		mp.updateMockPortflio();
		mp.buyPuts_Mock_Alerts();
		*/
	}

}
