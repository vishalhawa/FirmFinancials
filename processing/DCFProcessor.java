package processing;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import dao.PortfolioDAO;

public class DCFProcessor {

	
	PortfolioDAO dao = new PortfolioDAO();
	double USInflation = 0;
	double USGDP = 0;
	double USTBond = 0;
	double USMarketReturn = 0;
	
	public DCFProcessor(){
		HashMap<String,Double> indexMap = new HashMap<String,Double>();
		dao.getSetUSIndices(true,indexMap);
		USGDP = indexMap.get("USGDP");
		USInflation = indexMap.get("USInflation");
		USTBond = indexMap.get("^TYX");
		USMarketReturn = indexMap.get("MarketReturn");
		
	}
	public void calculateUSDCF (){
		
		HashMap<String,Double> indexMap = new HashMap<String,Double>();
		dao.getSetUSIndices(true,indexMap);
		LinkedHashMap<String,Properties> scrips = dao.getNYSEticker();
		LinkedHashMap<String,Properties> dcfList = new LinkedHashMap<String,Properties>();
		double costOfEquity = 0;
		double growthRate = 0;
		double marketPremium = 0;
		double DCFPrice = 0;
		int counter =0;
		for (String stk:scrips.keySet()){
			System.out.println("NYSE Set "+(scrips.size()-(counter++))+" "+scrips.get(stk).get("Beta") );	
			if(!stk.startsWith("MF-")){	
				try{

				double PEForward = Double.valueOf( scrips.get(stk).get("PEForward").toString());
				double beta = Double.valueOf( scrips.get(stk).get("Beta").toString());
				double PEG5 = Double.valueOf(scrips.get(stk).get("PEG5").toString());
				double divYield = Double.valueOf(scrips.get(stk).get("DividendYield").toString())/100;
				
				double adjEPS = Double.valueOf(scrips.get(stk).get("EPS").toString())-divYield*Double.valueOf( scrips.get(stk).get("closeprice").toString());
				costOfEquity = indexMap.get("^TYX").doubleValue()/100 +  beta*(indexMap.get("MarketReturn").doubleValue()/100-indexMap.get("^TYX").doubleValue()/100);
				if(PEG5 !=0){
					Properties prop = new Properties();
					
					growthRate = PEForward/PEG5/100;
				double growthValue = adjEPS + adjEPS*(1+growthRate)/(1+costOfEquity)+adjEPS*Math.pow((1+growthRate)/(1+costOfEquity),2)+adjEPS*Math.pow((1+growthRate)/(1+costOfEquity),3)+adjEPS*Math.pow((1+growthRate)/(1+costOfEquity),4);
				 DCFPrice = growthValue + (adjEPS*Math.pow((1+growthRate),5.0)/(costOfEquity-indexMap.get("USInflation")/100-indexMap.get("USGDP")/100))/Math.pow((1+costOfEquity),5.0);
				 System.out.println("Stock.."+stk+" Beta "+beta+ " COE "+costOfEquity+" GR "+growthRate+" DCF "+DCFPrice);
			
				 prop.put("CostOfEquity",costOfEquity);
				 prop.put("GrowthRate",growthRate);
				 prop.put("DCFPrice",DCFPrice);
				 dcfList.put(stk,prop);
		//		 dao.setUSDCFPrice(stk, costOfEquity, growthRate, DCFPrice);
				} // if = avoid NaN
				} // try
				catch(Exception e){
					System.out.println("Exception.."+stk);
					e.printStackTrace();
				}
				
				}// out IF
		}// for
		dao.setUSDCFPrice(dcfList);
	}
	
	public void calculateUSDCF(String stk, Map dataMap){
	Properties prop =	(Properties)dataMap.get(stk); //.getProperty("Beta");
	computeDCF( stk, dataMap );
//	System.out.println(stk+" datamap : "+prop.get("Volatility"));
	}
	private double computeDCF(String stk, Map<String,Properties> scrips ){
		double costOfEquity = 0;
		double growthRate = 0;
		double marketPremium = 0;
		double DCFPrice = 0;
//		System.out.println("US DATA: "+USInflation+ " "+ USGDP + " "	+ USTBond +" "+ USMarketReturn+" "  );	
		if(!stk.startsWith("MF-")){	
			try{

			double PEForward = Double.valueOf( scrips.get(stk).getProperty("Forward P/E","0"));
			double beta = Double.valueOf( scrips.get(stk).getProperty("Beta:","0"));
			double PEG5 = Double.valueOf(scrips.get(stk).getProperty("PEG Ratio","0"));
			double divYield = Double.valueOf(scrips.get(stk).getProperty("Forward Annual Dividend Yield","0"))/100;
			
			double adjEPS = Double.valueOf(scrips.get(stk).getProperty("Diluted EPS","0").toString())-divYield*Double.valueOf( scrips.get(stk).getProperty("("+stk+")","0").toString());
			costOfEquity = USTBond/100 +  beta*(USMarketReturn/100-USTBond/100);
			if(PEG5 !=0){
				growthRate = PEForward/PEG5/100;
				double growthValue = adjEPS + adjEPS*(1+growthRate)/(1+costOfEquity)+adjEPS*Math.pow((1+growthRate)/(1+costOfEquity),2)+adjEPS*Math.pow((1+growthRate)/(1+costOfEquity),3)+adjEPS*Math.pow((1+growthRate)/(1+costOfEquity),4);
				DCFPrice = growthValue + (adjEPS*Math.pow((1+growthRate),5.0)/(costOfEquity-USInflation/100-USGDP/100))/Math.pow((1+costOfEquity),5.0);
//				System.out.println("Stock.."+stk+" Beta "+beta+ " COE "+costOfEquity+" GR "+growthRate+" PROP "+(Properties)scrips.get(stk));
				Properties pp = (Properties)scrips.get(stk);
				pp.setProperty("COE", ""+costOfEquity);
				pp.setProperty("GrowthRate", ""+growthRate);
				pp.setProperty("DCFPrice", ""+DCFPrice);
				scrips.put(stk, pp);
				 //dao.setUSDCFPrice(stk, costOfEquity, growthRate, DCFPrice);
			} // if = avoid NaN
			} // try
			catch(Exception e){
				System.out.println("Exception.."+stk);
				e.printStackTrace();
			}
			
			}// out IF	
		return DCFPrice;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new DCFProcessor().calculateUSDCF();

	}

}
