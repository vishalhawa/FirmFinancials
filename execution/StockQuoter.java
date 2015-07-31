/*
 * This Package is to run over Indian Stock Market 
 * */

package execution;


import java.net.*;
import java.io.*;
import java.util.*;
import dao.INCompanyDAO;
import dao.InPortfolioDAO;
import parser.*;
import processing.FirmDataCollector;

public class StockQuoter {

	InPortfolioDAO portfoliodao;
	FirmDataCollector fdc;
	public static double sensexOpen;
	public static double sensexprevClose;
	public static double sensexIndexValue;
	public StockQuoter(){
	
		portfoliodao = new InPortfolioDAO();
		fdc = new FirmDataCollector();
		try{
		Properties pp =	new yahooDataParser().parseSensex(getPage(new URL("http://finance.yahoo.com/q?s=^BSESN")));
		sensexOpen = Double.parseDouble(pp.getProperty("Open"));
		sensexprevClose = Double.parseDouble(pp.getProperty("Prev Close:"));
		sensexIndexValue = Double.parseDouble(pp.getProperty("Index Value:"));
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("error message "+e.getMessage());
 		
		}
	}
	   
	public void getIndexData(){
		try{
			
			List indexlist=	portfoliodao.getSetIndices();//this method is not implemented
			while (indexlist.size() > 0){
				 System.out.println("indexlist: "+indexlist.size()+" "+indexlist.get(0));
				 
				 			
					URL url = new URL("http://in.finance.yahoo.com/q?s="+indexlist.get(0));

					
					 fdc.callback(url, getPage(url),new URL("http://in.finance.yahoo.com/"));
						
					 indexlist.remove(0);
				
			} // while 
			
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("error message "+e.getMessage());
 		
		}
	}
	public void getCompanyData(){
		
		try{
		List codelist = portfoliodao.getICICICODE();
		
		while (codelist.size() > 0){
			 System.out.println("codelist: "+codelist.size()+" "+codelist.get(0));
			 
			 			
				URL url = new URL("http://content.icicidirect.com/Research/companysnapshot.asp?icicicode="+codelist.get(0));

				
				 fdc.callback(url, getPage(url),new URL("http://content.icicidirect.com/Research/companysnapshot.asp"));
					
			codelist.remove(0);
			
		} // while ??

		}// try
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("error message "+e.getMessage());
 		
		}
//		new INCompanyDAO().doDataSynch();
//		new CompanyDAO().doBetaCheck();
		new INCompanyDAO().doportfolioUpdate();	
		
	} // method get company data
		
	private StringReader	getPage(URL url){
		
		return DataParser.getPage(url);
		 
	}// method
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	//	new StockQuoter().getIndexData(); // fetches only US data@@
		new FundQuoter().getNAV();
		new StockQuoter().getCompanyData();
		new indiaInfolineParser().parseFunds();

	}

}
