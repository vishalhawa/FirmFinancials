package dao;


import java.net.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.sql.Date;

import execution.NYSEQuoter;

import parser.OptionSymbol;
import processing.FirmDataCollector;


public class PortfolioDAO {

	Connection portfolio;

	double SMALLVALUE = 0.001;
	final String fileName = "C:\\Users\\Vishal\\Dropbox\\Projects\\FirmFinancials\\USStocks.accdb";
	Connection con = null;



	public PortfolioDAO() {
		// TODO Auto-generated constructor stub
		
		try{
			
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
		 portfolio = DriverManager.getConnection("jdbc:odbc:portfolio");
		 portfolio.setAutoCommit(true);
//	System.out.println(portfolio.toString());
		 
//		 Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		 
//		     String url = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ="+fileName;
		 
//		     con = DriverManager.getConnection(url,"","");


		}
		catch(Exception e){
			e.printStackTrace();
			
		}
	}
	
	public void initializeStocks(){
		try{Statement stmt = portfolio.createStatement();
	 
		stmt.execute("delete * from USAlerts"); // delete all rows
		}
		catch(SQLException se){
			se.printStackTrace();
		}
	}
public void initializeOptions()
{
	try{Statement stmt = portfolio.createStatement();
	stmt.execute("delete * from USOptions"); // delete all rows
 
	}
	catch(SQLException se){
		se.printStackTrace();
	}
}

public void doAlerts(double priceLevel,String asset,double change,int sessions,double volumeChange){
	String sql=null;
	try{

		Statement stmt = portfolio.createStatement();
		sql= "insert into USAlerts (Symbol,Change,daysAgo,LastUpdated,volumeChange,Price) values('"+asset+"',"+change+","+sessions+",'"+FirmDataCollector.mmdd+"',"+volumeChange +","+priceLevel+")";
			stmt.executeUpdate(sql);
			stmt.execute("select * from USAlerts");
	stmt.close();	
	}catch(SQLException se){
		System.out.println(sql);
		se.printStackTrace();
	}// catch
}
	
public void persistUSStocks(Map dataMap){
		String sql = "";
		String sql2 = "";
		try{

			Statement stmt = portfolio.createStatement();
			Object Flag = dataMap.remove("pagetype");
			
			Object[] keyset = dataMap.keySet().toArray();
			for(int x=0; x<keyset.length ;x++ ){

			Properties prop = (Properties)dataMap.get(keyset[x]);
//			System.out.println("("+keyset[x]+")"+prop.get("("+keyset[x]+")"));	

			if(Flag.toString().equals("scrips")){ // USStocks

			double perChange=	100*(Double.valueOf((String) prop.get("("+keyset[x]+")"))/Double.valueOf((String) prop.get("prevClose"))-1);
				 sql2 = "update USStocks set MA50="+prop.get("50-Day Moving Average")+",LowValue="+prop.get("lowValue")+",HighValue="+prop.get("highValue")+",perChange="+perChange+",Kurtosis="+prop.get("Kurtosis")+",closeprice="+prop.get("("+keyset[x]+")")+",Beta="+prop.get("Beta:")+",EPS="+prop.get("Diluted EPS")+",PEG5="+prop.get("PEG Ratio")+",MedTarget="+prop.get("Median Target:")+",AnalystOpinion="+prop.get("Mean Recommendation (this week):")+", brokers="+prop.get("No. of Brokers:")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where symbol='"+keyset[x]+"'" ;
				 sql = "update USStocks set CostOfEquity="+prop.get("COE")+",RSI="+prop.get("RSI")+",GrowthRate="+prop.get("GrowthRate")+",DCFPrice="+prop.get("DCFPrice")+",Revenue="+prop.get("Revenue (ttm):")+",Volatility20="+prop.get("Volatility20")+",Volatility="+prop.get("Volatility")+",RollingAvg20="+prop.get("rollingAvg20")+",RollingAVG="+prop.get("RollingAVG")+",DividendYield="+prop.get("Forward Annual Dividend Yield")+",DividendRate="+prop.get("Forward Annual Dividend Rate")+",PEForward="+prop.get("Forward P/E")+",PS="+prop.get("Price/Sales")+",PBV="+prop.get("Price/Book")+"  where symbol='"+keyset[x]+"'" ;
			} //if Stocks
			else
				if (Flag.toString().equals("Options") ){ // US Options
					ResultSet rs; 
					rs=stmt.executeQuery("select symbol from USOptions where Symbol='"+keyset[x]+"'");
		//			System.out.println(rs.getFetchSize());
					if(rs.next()==true){
						sql = "Update  USOptions set  Price="+prop.get("Price")+", IVolatility="+prop.get("IV")+",CalculatedPrice="+prop.get("OptionPrice")+",Volatility="+prop.get("Volatility")+",Delta="+prop.get("Delta")+",Theta="+prop.get("Theta")+" where Symbol='"+keyset[x]+"'"  ;
						 sql2 = "update USOptions set ExpiryDays="+prop.get("ExpiryDays")+",RollingAVG="+prop.get("RollingAVG")+",OpenInterest="+prop.get("openInt")+",Moneyness='"+prop.get("Moneyness")+"',StockPrice='"+prop.get("StockPrice")+"', Odds='"+prop.get("odds")+"', OptionPremiumPer='"+prop.get("OptionPremiumPer")+"',LastUpdated='"+FirmDataCollector.mmdd +"'  where Symbol='"+keyset[x]+"'" ;		
					}else{// New Record
					sql = ("insert into USOptions (Symbol,Price,IVolatility,CalculatedPrice,Volatility,Delta,Theta) values('"+keyset[x]+"',"+prop.get("Price")+","+prop.get("IV")+","+prop.get("OptionPrice")+","+prop.get("Volatility")+","+prop.get("Delta")+","+prop.get("Theta")+")");
				//	 sql = "update USOptions set Price="+prop.get("Price")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where Symbol='"+keyset[x]+"'" ;
					 sql2 = "update USOptions set ExpiryDays="+prop.get("ExpiryDays")+",RollingAVG="+prop.get("RollingAVG")+",OpenInterest="+prop.get("openInt")+",Moneyness='"+prop.get("Moneyness")+"',StockPrice='"+prop.get("StockPrice")+"', Odds='"+prop.get("odds")+"', OptionPremiumPer='"+prop.get("OptionPremiumPer")+"',LastUpdated='"+FirmDataCollector.mmdd +"'  where Symbol='"+keyset[x]+"'" ;
					}//if-else
				}// outer IF US OPtions
				else{ // US Funds
				 sql = "update USFund set NAV="+prop.get("("+keyset[x]+")")+",Yield="+prop.get("Yield")+",YearReturn="+prop.get("Year-to-Date Return")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where symbol='MF-"+keyset[x]+"'" ;
				 sql2 = "update USFund set FundFamily='"+prop.get("Fund Family")+"'"+",Category='"+prop.get("Category")+"'"+"  where symbol='MF-"+keyset[x]+"'" ;
				
			}// endif Funds

			 stmt.executeUpdate(sql);
			 stmt.executeUpdate(sql2);
			 stmt.execute("update Configparameters set lastUpdated='"+FirmDataCollector.mmdd+"' where Name='Model' "); // just to flush
			}// for
		
		stmt.close();
		}
		catch(SQLException se){
			System.out.println(sql+"\n"+sql2);
			se.printStackTrace();
		}
	}
		
		

	
	public Properties getUSAssetdata(String symbol){
	
	Properties cprop = new Properties();
	cprop.setProperty("symbol", symbol);
	try{

		Statement stmt = portfolio.createStatement();

		ResultSet rs = stmt.executeQuery("select MA50,RollingAvg20,Volatility20,Kurtosis,RollingAVG,PECurr,ROE,NPM,Sector,MedTarget,ClosePrice, Volatility, Beta,USPortfolio.buyPrice from USStocks,USPortfolio WHERE USStocks.symbol='"+symbol+"' ");
		
		while(rs.next()== true){
			cprop.setProperty("Sector", String.valueOf(rs.getString("Sector")));
			cprop.setProperty("StockPrice", String.valueOf(rs.getDouble("ClosePrice")));
			cprop.setProperty("RollingAVG", String.valueOf(rs.getDouble("RollingAVG")));
			cprop.setProperty("Volatility", String.valueOf(rs.getDouble("Volatility")));
			cprop.setProperty("Beta", String.valueOf(rs.getDouble("Beta")));
			cprop.setProperty("MedTarget", String.valueOf(rs.getDouble("MedTarget")));

			cprop.setProperty("NPM", String.valueOf(rs.getDouble("NPM")));
			cprop.setProperty("ROE", String.valueOf(rs.getDouble("ROE")));
			cprop.setProperty("PE", String.valueOf(rs.getDouble("PECurr")));
			cprop.setProperty("buyPrice", String.valueOf(rs.getDouble("buyPrice")));
			cprop.setProperty("Kurtosis", String.valueOf(rs.getDouble("Kurtosis")));
			cprop.setProperty("Volatility20", String.valueOf(rs.getDouble("Volatility20")));
			cprop.setProperty("RollingAvg20", String.valueOf(rs.getDouble("RollingAvg20")));
			cprop.setProperty("MA50", String.valueOf(rs.getDouble("MA50")));
 
		}

		 rs = stmt.executeQuery("select NAV,USPortfolio.buyPrice from USFund,USPortfolio WHERE USFund.symbol='"+symbol+"' ");
		 while(rs.next()== true){
				cprop.setProperty("StockPrice", String.valueOf(rs.getDouble("NAV")));
				cprop.setProperty("RollingAVG", String.valueOf(0.10));// setting dummy for now
				cprop.setProperty("Volatility", String.valueOf(0.03));// setting dummy for now
				cprop.setProperty("buyPrice", String.valueOf(rs.getDouble("buyPrice")));
				cprop.setProperty("Kurtosis", String.valueOf(3.0)); // setting dummy for now
		 }
		
		stmt.close();
		
		}
		catch(SQLException se){
			se.printStackTrace();
		}

	return cprop;
}

	public Properties getIndexdata(String code){
		
		Properties cprop = new Properties();
		cprop.setProperty("indexcode", code);
		try{
	
			Statement stmt = portfolio.createStatement();

			ResultSet rs = stmt.executeQuery("select Value1, Value2, Value3, configparameters.LastUpdated from configparameters WHERE Name='"+code+"'");
			
			while(rs.next()== true){
				cprop.setProperty("Value1", String.valueOf(rs.getString("Value1")));
				cprop.setProperty("lastdate", String.valueOf(rs.getDate("LastUpdated")));
				cprop.setProperty("Value2", String.valueOf(rs.getString("Value2")));
				cprop.setProperty("Value3", String.valueOf(rs.getString("Value3")));
			}
//			System.out.println(cprop.getProperty("lastdate"));
			stmt.close();
			
			}
			catch(SQLException se){
				se.printStackTrace();
			}
	
		return cprop;
		
	}
	
	public  void getSetUSExecutedPortfolio(boolean hasData,LinkedHashMap<String,Properties> scrip){
		String sql = "";
		String sql2 = "";
		try{			
			Statement stmt = portfolio.createStatement();
		if(hasData){
			// persist
			Object[] keyset = scrip.keySet().toArray();
			if(keyset.length ==1){ // insert ONE record NEW RECORD

				 	Properties prop = (Properties)scrip.get(keyset[0]);
/*
				 	 if(prop.get("Remarks").toString().equalsIgnoreCase("SCX") || prop.get("Remarks").toString().equalsIgnoreCase("SCZ")){
				 		 // Sell Call "SC"- Buy / Sell Price needs to alter
				 		prop.put("SellPrice",prop.get("buyprice"));
				 		prop.put("buyprice", NYSEQuoter.calCommission(prop.get("Remarks").toString(), Double.parseDouble(prop.get("number").toString()))/Double.parseDouble(prop.get("number").toString()));	
				 		}
				 	 */
				 	 System.out.println("Inserting A record into executed portfolio..."+prop);
				 	 sql = "insert into USExecuted (Symbol,Options,Shares,BuyPrice,BuyDate,SellPrice,SellDate,Type) values('"+prop.get("Symbol")+"','"+prop.get("Option")+"',"+prop.get("number")+","+prop.get("buyprice")+",'"+new Date((Long) prop.get("BuyDate"))+"',"+prop.get("SellPrice")+",'"+ new Date((Long) prop.get("SellDate"))+"','"+prop.get("Remarks")+"')" ;
					 stmt.execute(sql);
					 sql = "delete * from USPortfolio where sellprice="+prop.get("SellPrice")+" and sellDate=#"+new Date((Long)prop.get("SellDate"))+"#" ;  // Hopefully no two transactions will have same date and sellPrice
					 stmt.execute(sql);
					 
			}
			else{ // update records
				for(int x=0; x<keyset.length ;x++ ){
		
	
				Properties prop = (Properties)scrip.get(keyset[x]);
				 sql = "update USExecuted set BuyPrice="+prop.get("buyprice")+",Commission="+prop.get("Commission")+",CostBasis="+prop.get("CostBasis")+",Profit="+prop.get("profit")+",AbsoluteGrowth="+prop.get("AbsoluteGrowthP")+", AnnualizedGrowth="+prop.get("APY")+" where TXID="+keyset[x]+"" ;
//				 sql2 = "update USportfolio set AnnualizedGrowth="+prop.get("APY")+",AbsoluteGrowth="+prop.get("AbsoluteGrowthP")+",Profit="+prop.get("profit")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where ID="+keyset[x]+"" ;

	
				 stmt.executeUpdate(sql);
				} // else
//			 stmt.executeUpdate(sql2);
			 stmt.execute("select * from USExecuted"); // just to flush the prev sql
			 
			} // for
		} // if
			else{
				// retrieve stocks
				ResultSet rs = stmt.executeQuery("select * from USExecuted ");
				
				while(rs.next()== true){
					Properties prop = new Properties();

					prop.put("scrip", rs.getString("symbol"));
					prop.put("number", rs.getDouble("shares"));
					prop.setProperty("Type", rs.getString("Type"));
					scrip.put(rs.getString("TXID"),prop);
					prop.put("buyprice", rs.getDouble("buyprice"));
					
					prop.put("SellPrice", rs.getDouble("SellPrice"));
					long buydate = rs.getDate("buydate").getTime();
			//		prop.put("durationdays", (rs.getDate("LastUpdated").getTime()-buydate)/86400000);
					prop.put("durationHeld", (rs.getDate("SellDate").getTime()-buydate)/86400000);


				}
				
			}
	
	
		}// try
		catch(SQLException se){
			System.out.println(sql);
			System.out.println(sql2);
			se.printStackTrace();
		}
	}

	public  void getSetUSPortfolio(boolean hasData,LinkedHashMap<String,Properties> scrip){
		String sql = "";
		String sql2 = "";
		try{
			Statement stmt = portfolio.createStatement();
		if(hasData){
			// persist
			
			Object[] keyset = scrip.keySet().toArray();
			for(int x=0; x<keyset.length ;x++ ){
	
				Properties prop = (Properties)scrip.get(keyset[x]);
				 sql = "update USportfolio set BEP="+prop.get("BEP")+",oddsBEP="+prop.get("oddsBEP")+", ExcercisePositionValue="+prop.get("StockPositionValue")+",BlockedValue="+prop.get("BlockedValue")+"  where ID="+keyset[x]+"" ;
				 sql2 = "update USportfolio set Commission="+prop.get("commission")+",CostBasis="+prop.get("costBasis")+",AnnualizedGrowth="+prop.get("APY")+",AbsoluteGrowth="+prop.get("AbsoluteGrowthP")+",Profit="+prop.get("profit")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where ID="+keyset[x]+"" ;

				 //	 System.out.println(sql);
			 stmt.executeUpdate(sql);
			 stmt.executeUpdate(sql2);
			 stmt.execute("select * from USportfolio"); // just to flush the prev sql
			 
			}
		
	
		}
		else{
			// retrieve stocks
			ResultSet rs = stmt.executeQuery("select ID,sector, USportfolio.symbol,number,buyprice,buydate,closeprice,SellPrice,SellDate,Remarks,USStocks.lastupdated from USportfolio,USStocks WHERE  USStocks.symbol = USportfolio.symbol order by USportfolio.LastUpdated");
			
			while(rs.next()== true){
				Properties prop = new Properties();
				prop.put("scrip", rs.getString("symbol"));
				prop.setProperty("Remarks", rs.getString("Remarks"));
				prop.put("Sector", rs.getString("sector"));
				prop.put("number", rs.getDouble("number"));

				prop.put("buyprice", rs.getDouble("buyprice"));
				prop.put("closeprice", rs.getDouble("closeprice"));

				double sellprice = rs.getDouble("SellPrice");
				prop.put("SellPrice", sellprice);
				long buydate = rs.getDate("buydate").getTime();
				if (sellprice !=0) { 
					long selldate = rs.getDate("selldate").getTime(); 
					prop.put("durationHeld", (selldate-buydate)/86400000);
					prop.put("SellDate", selldate);
				}
				prop.put("durationdays", (rs.getDate("LastUpdated").getTime()+86400000 -buydate)/86400000); // Adding 86400000 ms to avoid divide by zero error
				
				prop.put("BuyDate", buydate);
				
				scrip.put(rs.getString("ID"),prop);

			}
			// retrieve Funds
			 rs = stmt.executeQuery("select ID, USportfolio.symbol,number,buyprice,buydate,SellPrice,SellDate,NAV,Remarks,USFund.lastupdated from USportfolio,USFund WHERE ((Now()-[USportfolio.LastUpdated])>0) and USFund.symbol = USportfolio.symbol order by USportfolio.LastUpdated");
			
			while(rs.next()== true){
				Properties prop = new Properties();
				prop.put("scrip", rs.getString("symbol"));
				prop.put("number", rs.getDouble("number"));
				prop.put("buyprice", rs.getDouble("buyprice"));
				prop.put("closeprice", rs.getDouble("NAV"));
				double sellprice = rs.getDouble("SellPrice");
				prop.put("SellPrice", sellprice);
				long buydate = rs.getDate("buydate").getTime();
				prop.setProperty("Remarks", rs.getString("Remarks"));
				if (sellprice !=0) { 
					long selldate = rs.getDate("selldate").getTime();
					prop.put("durationHeld", (selldate-buydate)/86400000);
					prop.put("SellDate", selldate);
				}

				prop.put("durationdays", (rs.getDate("LastUpdated").getTime()+86400000-buydate)/86400000); // addign 86400000
				prop.put("BuyDate", buydate);
				scrip.put(rs.getString("ID"),prop);
			}
			// retrieve Options
			 rs = stmt.executeQuery("select ID, USportfolio.symbol,number,buyprice,buydate,USOptions.Price,SellPrice,SellDate,Remarks,USOptions.lastupdated,delta,theta,ExpiryDays,Volatility FROM USPortfolio LEFT JOIN USOptions ON USPortfolio.symbol=USOptions.symbol WHERE ( USportfolio.remarks<>'MF' And USportfolio.remarks<>'S') order by USportfolio.LastUpdated");
			
			while(rs.next()== true){
				Properties prop = new Properties();
				prop.put("scrip", rs.getString("symbol"));
				prop.put("number", rs.getDouble("number"));
				prop.put("buyprice", rs.getDouble("buyprice"));
				double closeprice = rs.getDouble("Price");
				if (closeprice == 0 ){
					prop.put("closeprice", String.valueOf(SMALLVALUE));
					prop.put("delta", SMALLVALUE);
				}else{
					prop.put("closeprice", closeprice);
					prop.put("delta", rs.getDouble("delta"));
				}

				
				prop.put("theta", rs.getDouble("theta"));
				prop.put("ExpiryDays", rs.getDouble("ExpiryDays"));
				prop.put("Volatility", rs.getDouble("Volatility"));
				double sellprice = rs.getDouble("SellPrice");
				prop.put("SellPrice", sellprice);
				long buydate = rs.getDate("buydate").getTime();
				prop.setProperty("Remarks", rs.getString("Remarks"));
				if (sellprice !=0) { // trade executed
					long selldate = rs.getDate("selldate").getTime();
					prop.put("durationHeld", (selldate-buydate)/86400000);
					prop.put("SellDate", selldate);
				}

				Date dt;
			if(( dt =rs.getDate("LastUpdated"))!= null){ // Might as well use system time and remove if condition
				prop.put("durationdays", (dt.getTime()+86400000-buydate)/86400000); // adding 86400000
			 }
			else{prop.put("durationdays",(System.currentTimeMillis()+86400000-buydate)/86400000);}
			
				prop.put("BuyDate", buydate);
				scrip.put(rs.getString("ID"),prop);
			}

			stmt.close();	
			
		}
		
		}// try
		catch(SQLException se){
			System.out.println(sql);
			System.out.println(sql2);
			se.printStackTrace();
		}
	}
	public LinkedHashMap<String,Properties> getNYSEticker(){
		
		LinkedHashMap<String,Properties> codelist = new LinkedHashMap<String,Properties>();
		try{
			
		
		Statement stmt = portfolio.createStatement();
		
		ResultSet rs = stmt.executeQuery("select * from USStocks WHERE ((Now()-[LastUpdated])>1)  order by LastUpdated");
		
		while(rs.next()== true){
			Properties prop = new Properties();
			codelist.put(rs.getString("symbol"),prop);
			prop.put("Sector", rs.getString("Sector"));
			
			prop.put("closeprice", rs.getDouble("closeprice"));
			prop.put("Beta", rs.getDouble("Beta"));
			prop.put("LastUpdated", rs.getDate("LastUpdated"));
			prop.put("PECurr", rs.getDouble("PECurr"));
			prop.put("EPS", rs.getDouble("EPS"));
			prop.put("DividendYield", rs.getDouble("DividendYield"));

			prop.put("PEForward", rs.getDouble("PEForward"));
			prop.put("PEG5", rs.getDouble("PEG5"));
			


		}
		
		rs = stmt.executeQuery("select * from USFund WHERE ((Now()-[LastUpdated])>1)  order by LastUpdated");
		
		while(rs.next()== true){
			Properties prop = new Properties();
			prop.put("NAV", rs.getDouble("NAV"));
			prop.put("yield", rs.getDouble("yield"));
			prop.put("LastUpdated", rs.getDate("LastUpdated"));
			prop.put("yearReturn", rs.getDouble("yearReturn"));
			
			codelist.put(rs.getString("symbol"),prop);

		}

		stmt.close();
		
		}
		catch(SQLException se){
			se.printStackTrace();
		}
		return codelist ;
	}

	public void setUSDCFPrice(	LinkedHashMap<String,Properties> dcfList){
//	public void setUSDCFPrice(String stk,double coe,double growthRate, double dcfPrice){
		String sql=null;
		 
		try{
			Statement stmt = portfolio.createStatement();
			for (String stk:dcfList.keySet()){
				double CostOfEquity = Double.valueOf( dcfList.get(stk).get("CostOfEquity").toString());
				double GrowthRate = Double.valueOf( dcfList.get(stk).get("GrowthRate").toString());
				double DCFPrice = Double.valueOf( dcfList.get(stk).get("DCFPrice").toString());

				sql = "update USStocks set CostOfEquity="+CostOfEquity+",GrowthRate="+GrowthRate+", DCFPrice="+DCFPrice +" where symbol= '"+stk+"'";
		
	//	sql = "update USStocks set CostOfEquity="+coe+",GrowthRate="+growthRate+", DCFPrice="+dcfPrice +" where symbol= '"+stk+"'";
			 stmt.executeUpdate(sql);  
			}// for
			} //try
		catch(SQLException e){
			 System.out.println(sql);
			e.printStackTrace();
		}

		
	}
	public LinkedHashMap<String,Properties> getOptionsList(){
		
		LinkedHashMap<String,Properties> OptionsList = new LinkedHashMap<String,Properties>();
		String sym = null;
		try{
			
		
		Statement stmt = portfolio.createStatement();
		
		ResultSet rs = stmt.executeQuery("select symbol,ClosePrice from USStocks WHERE  (USStocks.Options=1) order by lastupdated");
	
		while(rs.next()== true){
			try{
			Properties optionprop = new Properties();
			 sym = rs.getString("symbol");
//				System.out.println("symbol: "+sym);
			optionprop.setProperty("StockPrice", rs.getString("ClosePrice"));
//			optionprop.setProperty("IVolatility", rs.getString("IVolatility"));
//			optionprop.setProperty("Volatility", rs.getString("Volatility"));
//			optionprop.setProperty("scrip", sym.substring(0, sym.length()-15));
//			optionprop.setProperty("strikeprice", sym.substring(sym.length()-8, sym.length()-3)+"."+sym.substring(sym.length()-3, sym.length()));
			OptionsList.put(sym,optionprop);
			}catch (Exception e){	System.out.println("Price not found symbol: "+sym);}
//			
		}

		stmt.close();
		
		}
		catch(SQLException se){
		
			System.out.println("option List: "+OptionsList);
			se.printStackTrace();
		}
		return OptionsList ;
	}
	
	public  List getSetIndices(){
		List ll=null;
		return ll;
	}
	public void getSetUSIndices(boolean getting, HashMap datamap){

		try{
			Statement stmt = portfolio.createStatement();
		if(getting){	

//		System.out.println("Running index query..");
		ResultSet rs = stmt.executeQuery("select * from Configparameters WHERE Value3='US' OR Value3='USA' order by Name ");
		
		while(rs.next()== true){
			 
			datamap.put(rs.getString("Name"),Double.valueOf(rs.getString("Value1")));//.add();
//			datamap.put("Value",rs.getString("Value1"));			
//			datamap.put("Market",rs.getString("Value3"));
			//			System.out.println(rs.getString("Name"));
		}

	
		}// if
		else{ //setting 	
			Iterator it = datamap.keySet().iterator();
			while(it.hasNext()){
						//for(Object p: datamap.values()){
					String idx = (String)it.next();
 
		//		System.out.println("Update Configparameters set LastUpdated=Now(), Value1="+((Properties)datamap.get(idx)).getProperty("Index Value:") +" WHERE Name='"+idx+"'  ");

				 stmt.executeUpdate("Update Configparameters set LastUpdated=Now(), Value1="+((Properties)datamap.get(idx)).getProperty("Index Value:")+" WHERE Name='"+idx+"'  ");
				
			
			} // inner while
		}
		stmt.execute("select * from configparameters");
		stmt.close();
		portfolio.commit();
		}// try
		catch(SQLException se){
			se.printStackTrace();
		}
		
		return  ;
		
	}
	
	public  void getSetUSBollinger_Alerts(boolean hasData,HashMap<String, Properties> optionsMap){
		
		 try{
			 CallableStatement cs = portfolio.prepareCall("{call US_Stocks_Bollinger_Upper_Band()}");
 
         ResultSet rs3 = cs.executeQuery();
         while(rs3.next())
             {
        	 Properties prop = new Properties();
				optionsMap.put(rs3.getString("symbol")+rs3.getString("daysAgo"),prop);
				prop.put("MA50", rs3.getDouble("MA50"));
				prop.put("ClosePrice", rs3.getDouble("ClosePrice"));

         } // While
		 }// try
		 catch(Exception e) { 
         
         e.printStackTrace();
     }
	}
	public  void getSetUSPortfolio_Mock(boolean hasData,HashMap<String, Properties> optionsMap){
		String sql = "";
		String sql2 = "";
		try{
			Statement stmt = portfolio.createStatement();
			Statement stmt2 = portfolio.createStatement();
		if(hasData){
			// OPEN POSITION
			
			Object[] keyset = optionsMap.keySet().toArray();
			for(int x=0; x<keyset.length ;x++ ){
				ResultSet rs = stmt.executeQuery("select symbol from USportfolio_Mock where symbol='"+keyset[x]+"'");
				if( optionsMap.get(keyset[x]).getProperty("Remarks")!=null && !rs.next()){
					double buyprice = Double.valueOf(optionsMap.get(keyset[x]).getProperty("optionPrice"));
					// Insert , Open Position
					sql = "Insert into USportfolio_Mock (symbol,Remarks,shares,buyPrice,buyDate) values ('"+keyset[x]+"','"+optionsMap.get(keyset[x]).getProperty("Remarks")+"',100,"+buyprice+",#"+FirmDataCollector.mmdd+"#)";
					stmt.executeUpdate(sql);
					stmt.execute("select * from USportfolio_Mock" );
				}
				Properties prop = (Properties)optionsMap.get(keyset[x]);
	//			 sql = "update USportfolio_Mock set BEP="+prop.get("BEP")+",oddsBEP="+prop.get("oddsBEP")+", ExcercisePositionValue="+prop.get("StockPositionValue")+",BlockedValue="+prop.get("BlockedValue")+"  where ID="+keyset[x]+"" ;
				 sql2 = "update USportfolio_Mock set Commission="+prop.get("commission")+",CostBasis="+prop.get("costBasis")+",AnnualizedGrowth="+prop.get("APY")+",AbsoluteGrowth="+prop.get("AbsoluteGrowthP")+",Profit="+prop.get("profit")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where ID="+keyset[x]+"" ;

				 //	 System.out.println(sql);
		//	 stmt.executeUpdate(sql);
		//	 stmt.executeUpdate(sql2);
	 
			}// for
		
		}
		else{
			// retrieve stocks / Options - RSI Filter  80/20 
			ResultSet rs = stmt.executeQuery("SELECT  symbol,RSI FROM usstocks WHERE (RSI>0) And (RSI>80 Or RSI<20) ");
			
			while(rs.next()== true){
				
				String stk = rs.getString("symbol");
			
				double RSI= rs.getDouble("RSI");
				System.out.println("Collaborating "+stk);
				try{
				ResultSet rs2 = stmt2.executeQuery("select  USOptions.symbol,USOptions.Price,USOptions.lastupdated,delta,theta,ExpiryDays,Volatility FROM USOptions  WHERE ( left(symbol,len(symbol)-15)  = '"+stk+"')");
				while(rs2.next()== true){
//					System.out.println(" "+stk);
				Properties prop = new Properties();
				//prop.put("OptionsSymbol", rs2.getString("symbol"));
				prop.put("Volatility", rs2.getDouble("Volatility"));
				prop.put("theta", rs2.getDouble("theta"));
				prop.put("delta", rs2.getDouble("delta"));
				prop.put("price", rs2.getDouble("Price"));
				prop.put("RSI", RSI);
				optionsMap.put(rs2.getString("symbol"),prop);
				 }//while-inner
				}catch(SQLException se){ 
					se.printStackTrace();
				}
			} // while

				
		}//else
		
		}catch(SQLException se){
			System.out.println(sql);
			System.out.println(sql2);
			se.printStackTrace();
		}
	}
	
	public  void getSetUSPortfolio_Mock_Alerts(boolean hasData,HashMap<String, Properties> optionsMap){
		String sql = "";
		String sql2 = "";
		try{
			Statement stmt = portfolio.createStatement();
			Statement stmt2 = portfolio.createStatement();
		if(hasData){
			// OPEN POSITION
			
			Object[] keyset = optionsMap.keySet().toArray();
			for(int x=0; x<keyset.length ;x++ ){
				ResultSet rs = stmt.executeQuery("select symbol from USportfolio_Mock where symbol='"+keyset[x]+"'");
				if( optionsMap.get(keyset[x]).getProperty("Remarks")!=null && !rs.next()){
					double buyprice = Double.valueOf(optionsMap.get(keyset[x]).getProperty("optionPrice"));
					// Insert , Open Position
					sql = "Insert into USportfolio_Mock (symbol,Remarks,shares,buyPrice,buyDate) values ('"+keyset[x]+"','"+optionsMap.get(keyset[x]).getProperty("Remarks")+"',100,"+buyprice+",#"+FirmDataCollector.mmdd+"#)";
					stmt.executeUpdate(sql);
					stmt.execute("select * from USportfolio_Mock_Alerts" );
				}
				Properties prop = (Properties)optionsMap.get(keyset[x]);
	//			 sql = "update USportfolio_Mock set BEP="+prop.get("BEP")+",oddsBEP="+prop.get("oddsBEP")+", ExcercisePositionValue="+prop.get("StockPositionValue")+",BlockedValue="+prop.get("BlockedValue")+"  where ID="+keyset[x]+"" ;
				 sql2 = "update USportfolio_Mock set Commission="+prop.get("commission")+",CostBasis="+prop.get("costBasis")+",AnnualizedGrowth="+prop.get("APY")+",AbsoluteGrowth="+prop.get("AbsoluteGrowthP")+",Profit="+prop.get("profit")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where ID="+keyset[x]+"" ;
			}// for
		}
		else{
			// retrieve stocks / Options - Alerts Filter for SELL/Puts
			ResultSet rs = stmt.executeQuery("SELECT  DISTINCT USAlerts.symbol FROM USAlerts,USStocks WHERE USAlerts.symbol=USStocks.symbol and USStocks.ClosePrice>5 and daysAgo < 10 and change<0");
			
//			double quoteChange= rs.getDouble("change");
//			double volumeChange= rs.getDouble("volumeChange");
			
			while(rs.next()== true){
				
				String stk = rs.getString("symbol");
//				System.out.println(" "+stk);
				try{
				ResultSet rs2 = stmt2.executeQuery("select  USOptions.symbol,USOptions.Price,USOptions.lastupdated,delta,theta,ExpiryDays,Volatility FROM USOptions  WHERE ( USOptions.symbol Like '"+stk+"%') and Price < 4 and delta < -4");
				while(rs2.next()== true){
			
				Properties prop = new Properties();
				//prop.put("OptionsSymbol", rs2.getString("symbol"));
				prop.put("ExpiryDays", rs2.getDouble("ExpiryDays"));
				prop.put("theta", rs2.getDouble("theta"));
				prop.put("delta", rs2.getDouble("delta"));
				prop.put("price", rs2.getDouble("Price"));
//				prop.put("quoteChange", quoteChange);
				optionsMap.put(rs2.getString("symbol"),prop);
				 }//while-inner
				}catch(SQLException se){ 
					se.printStackTrace();
				}
			} // while

				
		}//else
		
		}catch(SQLException se){
			System.out.println(sql);
			System.out.println(sql2);
			se.printStackTrace();
		}
	}
			
	public void setOpenPositions_Mock(boolean hasData,HashMap<String, Properties> optionsMap){
		String sql = "";
		String sql2 = "";

		try{
			Statement stmt = portfolio.createStatement();
			Statement stmt2 = portfolio.createStatement();
			if(!hasData){

		ResultSet rs = stmt.executeQuery("SELECT * FROM USPortfolio_Mock,USoptions where (sellPrice is null) and (USoptions.symbol=USPortfolio_Mock.symbol)");
		
		while(rs.next()== true){
			Properties prop = new Properties();	
			String option = rs.getString("symbol");
			String stk = OptionSymbol.getStockSymbol(option);
			prop.put("OptionSymbol", option);
			prop.put("BuyPrice", rs.getDouble("BuyPrice"));
			
			prop.put("Shares", rs.getDouble("Shares"));
			prop.put("CurrentPrice", rs.getDouble("price"));
			prop.put("Volatility", rs.getDouble("Volatility"));
			prop.put("theta", rs.getDouble("theta"));
			prop.put("delta", rs.getDouble("delta"));
			prop.put("Type", rs.getString("Remarks"));
 
		 
			ResultSet rs2 = stmt2.executeQuery("select  RSI FROM USStocks  WHERE ( USStocks.symbol = '"+stk+"')");
			while(rs2.next()== true) 
				prop.put("RSI", rs2.getDouble("RSI"));
  
			prop.put("SellDate", rs.getDate("SellDate"));
			long buydate = rs.getDate("BuyDate").getTime();
	//		 	 System.out.println("Date "+new Date(buydate));
			prop.put("BuyDate", new Date(buydate));
			prop.put("durationdays", (rs.getDate("LastUpdated").getTime()+86400000 -buydate)/86400000); //This date should be coming from USOptions
			optionsMap.put(option,prop); 
			} //while-outer
		
			}else{ // Persist Information
				Object[] keyset = optionsMap.keySet().toArray();
				for(int x=0; x<keyset.length ;x++ ){
		
					Properties prop = (Properties)optionsMap.get(keyset[x]);
			//		System.out.println("Sell Date "+prop.get("SellDate"));
	 		 sql = "update USportfolio_Mock set SellPrice="+prop.get("SellPrice")+",SellDate='"+prop.get("SellDate").toString()+"'  where symbol='"+keyset[x]+"'" ;
					 sql2 = "update USportfolio_Mock set Commission="+prop.get("Commission")+",CostBasis="+prop.get("CostBasis")+",AnnualizedGrowth="+prop.get("APY")+",AbsoluteGrowth="+prop.get("AbsoluteGrowthP")+",Profit="+prop.get("profit")+"  where symbol='"+keyset[x]+"'" ;
		//			 System.out.println(sql);
				
	 		 stmt.executeUpdate(sql);
				 stmt.executeUpdate(sql2);
				 stmt.execute("select * from USportfolio_Mock"); // just to flush the prev sql
				}// for
			}
			}catch(SQLException se){
			System.out.println(sql);
			System.out.println(sql2);
			se.printStackTrace();
		} //catch
	}
	
	public void releaseConnection(){
		portfolio = null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, Properties> list = new LinkedHashMap<String, Properties>();
		new PortfolioDAO().getSetUSPortfolio_Mock(false, list);
		System.out.println("Connecting portfolio");
		System.out.println(list);



		/*
try{
			Driver d = (Driver)Class.forName ("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
			System.out.println(d);
			System.out.println(DriverManager.getDrivers().nextElement());
			Connection c = DriverManager.getConnection(	"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=portfolio.mdb");
		System.out.println(c);
	
}catch (Exception ex)
{ex.printStackTrace();}

*/

	}

	 

}
