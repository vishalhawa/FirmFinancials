package processing;
import java.io.*;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import dao.InPortfolioDAO;

import execution.StockQuoter;


public class EvaluateCompanyProp {
	
	String code;
	double newPrice ;
	double oldPrice ;
	double weightVolatilityParam = 0.95;
	double weightBetaParam = 0.99;
	List dcflist ;
	BufferedWriter outbuff;
	String flag;
	Map dataMap ;
	boolean validDCFdata = false;
	
	EvaluateCompanyProp(){
		 System.out.println(" USAGE: Parameter company data map reqd");}
	
	
	EvaluateCompanyProp(Map freshdataMap){
	
		dataMap = freshdataMap;
		try{
//			outbuff = new BufferedWriter(new FileWriter("Volatility Beta.xls",true));
//			outbuff.write("ICICICODE\t OLD \t NEW\n");
	
		String compFlag = (String)freshdataMap.remove("pagetype");
		 dcflist = new InPortfolioDAO().getDCFList();  
		 flag = compFlag.toString();

				Object[] keyset = freshdataMap.keySet().toArray();
				for(int x=0; x<keyset.length ;x++ ){
//					 System.out.println(flag.toString().equals("company"));
//					 System.out.println(dcflist.contains(keyset[x]));

					 code = (String)keyset[x];
					if(flag.toString().equals("company") && dcflist.contains(keyset[x]) ){ // COMPANY
						
						Properties compprop = (Properties)freshdataMap.get(keyset[x]);

						if (!(compprop.getProperty("Close Price").equals("null") || compprop.getProperty("Open Price").equals("null")))   
						{
//							System.out.println("Close Price "+compprop.get("Close Price"));
						 newPrice = Double.parseDouble((String)compprop.get("Close Price"));
						 oldPrice = Double.parseDouble((String)compprop.get("Open Price"));
						 validDCFdata = true;
//						 sql = "update Companysnap set Sales="+compprop.get("Sales Income")+",actual="+compprop.get("Actual")+",EPS1="+compprop.get("EPS1")+",EPS2="+compprop.get("EPS2")+",DebtRatio="+compprop.get("Debt / Equity")+",OPM="+compprop.get("OPM (%)")+",GPM="+compprop.get("GPM (%)")+",NPM="+compprop.get("NPM (%)")+", ClosePrice="+compprop.get("Close Price")+", DividendYield="+compprop.get("Dividend Yield %")+", ROE="+compprop.get("Return on Net Worth")+", PE="+compprop.get("P/E")+", BookValue="+compprop.get("Book Value")+",LastUpdated='"+(1+calendar.get(Calendar.MONTH))+"/"+calendar.get(Calendar.DATE) +"'  where icicicode='"+keyset[x]+"'" ;

						}
					}// outer if

					if(flag.toString().equals("index")  ){ // INDEX
							
						Properties prop = (Properties)freshdataMap.get(keyset[x]);
						if ( ! (prop.getProperty("Index Value:").equals("null") || prop.getProperty("Open").equals("null") )) //.toString().matches("\\d+")) //&& prop.get("Open").toString().matches("\\p{Digit}+\\p{Punct}?\\p{Digit}*")) {
						{
						 newPrice = Double.parseDouble((String)prop.get("Index Value:"));
						 oldPrice = Double.parseDouble((String)prop.get("Open"));
						 validDCFdata = true;
//						 sql = "update Companysnap set Sales="+compprop.get("Sales Income")+",actual="+compprop.get("Actual")+",EPS1="+compprop.get("EPS1")+",EPS2="+compprop.get("EPS2")+",DebtRatio="+compprop.get("Debt / Equity")+",OPM="+compprop.get("OPM (%)")+",GPM="+compprop.get("GPM (%)")+",NPM="+compprop.get("NPM (%)")+", ClosePrice="+compprop.get("Close Price")+", DividendYield="+compprop.get("Dividend Yield %")+", ROE="+compprop.get("Return on Net Worth")+", PE="+compprop.get("P/E")+", BookValue="+compprop.get("Book Value")+",LastUpdated='"+(1+calendar.get(Calendar.MONTH))+"/"+calendar.get(Calendar.DATE) +"'  where icicicode='"+keyset[x]+"'" ;
//						 System.out.println(code+" CODE ");
						}
					}// outer if
					
			}// FOR
				freshdataMap.put("pagetype",compFlag);
		}catch(Exception se){
			 System.out.println("Page not found..?"+" CODE: "+code);
			//se.printStackTrace();
		}
	}
	
	public void evaluateVolatility(){
		
		double newVol;
		Properties pp = (Properties)dataMap.get(code);  
		

			Properties cprop = null;
			double currvol = 0;

			if (flag.toString().equals("company")&& validDCFdata){  cprop = new InPortfolioDAO().getcompanydata(code);
			 currvol = Double.parseDouble((String)cprop.get("Volatility"));
//			 System.out.println("Vol.."+currvol);
			}
			if (flag.toString().equals("index") && validDCFdata){  cprop = new InPortfolioDAO().getIndexdata(code);
			 currvol = Double.parseDouble((String)cprop.get("Value2"));
			}
			pp.setProperty("NewVolatility", String.valueOf(currvol)); // adding currVol as defalut
			if(validDCFdata){		
				if(cprop.getProperty("lastdate").contains(""+FirmDataCollector.calendar.get(Calendar.DATE))){
					//avoid multiple calculations of Vol and Beta for multiple runs of teh program
					newVol = currvol;
//					System.out.println(" we are on same .. date: "+cprop.getProperty("lastdate"));
				}else{
					 newVol = calculateVol(newPrice,oldPrice,currvol);
				}
				pp.setProperty("NewVolatility", String.valueOf(newVol));
/*				
				try{
					outbuff.write(code+"\t"+currvol+"\t"+newVol+"\n");
					outbuff.flush();
				}catch(Exception se){
					se.printStackTrace();
				}
*/				

		}
	}		
	
	// checl ALGO
	public double calculateVol(double nPrice, double cPrice, double cVol){
		double nVol;
		double gain = 100*Math.log(nPrice/cPrice);
		nVol = weightVolatilityParam*Math.pow((cVol), 2)+ (1-weightVolatilityParam)*Math.pow(gain, 2);
		return Math.sqrt(nVol);
	}
	
	public void evaluateBeta(){
		
		double newBeta, iBeta;
		Properties pp = (Properties)dataMap.get(code);  
		

			Properties cprop = null;
			double oldBeta=1;

			if (flag.toString().equals("company") && validDCFdata){  cprop = new InPortfolioDAO().getcompanydata(code);
			oldBeta = Double.parseDouble((String)cprop.get("Beta"));
			}
			if (flag.toString().equals("index") && validDCFdata){  cprop = new InPortfolioDAO().getIndexdata(code);
			oldBeta = Double.parseDouble((String)cprop.get("Value3"));
	
			}
			pp.setProperty("NewBeta", String.valueOf(oldBeta));
			if(validDCFdata){
				newBeta = oldBeta;
				iBeta = oldBeta;
				if(cprop.getProperty("lastdate").contains(""+FirmDataCollector.calendar.get(Calendar.DATE))){
				//avoid multiple calculations of Vol and Beta for multiple runs of teh program
					// above IF condition should be revisited // do nothing
				}else{
					//iBeta : new Beta
						iBeta = ((newPrice - oldPrice)*StockQuoter.sensexOpen)/(oldPrice*(StockQuoter.sensexIndexValue - StockQuoter.sensexOpen)); // just heuristics can be refined...
					 if(Math.abs(iBeta)< 50) newBeta = weightBetaParam*oldBeta + (1-weightBetaParam)*(iBeta);		
				}
				pp.setProperty("NewBeta", String.valueOf(newBeta));
	/*
				try{ // eventually to be removed
					outbuff.write(code+"\t"+oldBeta+"\t"+iBeta+"\t"+newBeta+"\n");

					outbuff.flush();
				}catch(Exception se){
					se.printStackTrace();
				}
*/

		}
	}
/*	
	public void finalize() {
		try {
			outbuff.close();
	}
	catch(IOException se){
			se.printStackTrace(); 
		}
	}
*/	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		new EvaluateCompanyProp().evaluateVolatility();
	}

}
