package dao;


import java.io.*;
import java.util.*;
import java.sql.*;
import java.sql.Date;

import processing.FirmDataCollector;



public class INCompanyDAO {

	Connection connfinance;
	Connection connDCF ;
	Connection connBeta ;
	
	GregorianCalendar calendar;
	
	
	public INCompanyDAO() {
		// TODO Auto-generated constructor stub
		
		try{
			
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
//		    Connection c = DriverManager.getConnection(
//		     "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=c:/tech97.mdb"    );
		  

	//		connfinance = DriverManager.getConnection("jdbc:odbc:ExcelFinance");
			connDCF = DriverManager.getConnection("jdbc:odbc:portfolio");
		
			// Get a Database MetaData as a way of interrogating
		      // the names of the tables in this database.
	//	      DatabaseMetaData meta = connfinance.getMetaData();
			/*
		      System.out.println("We are using " + meta.getDatabaseProductName());
		      System.out.println("Version is " + meta.getDatabaseProductVersion());
		       System.out.println("User name.."+meta.getUserName());

		      ResultSet schemas = meta.getCatalogs();
		      while (schemas.next()) {
		        String tableSchema = schemas.getString(1);    // "TABLE_SCHEM"
//		        String tableCatalog = schemas.getString(2); //"TABLE_CATALOG"
		        System.out.println("tableSchema"+tableSchema);
       
		      }
	
		*/		
		       calendar = new GregorianCalendar() ;
		}
		catch(Exception e){
			e.printStackTrace();
			
		}
	}


	public void  doDataSynch(){
	
	
		try{
		
		Statement stmtXLS = connfinance.createStatement();
		Statement stmtDCF = connDCF.createStatement();
		double beta;
		String icicicode;
		ResultSet rs = stmtXLS.executeQuery("select icicicode,[Cost of Equity], [Estimated  Stock Price], [DCF Over/Under value %], RSQR, Beta, [Volatility] from [Stock Price$]");


		
		while(rs.next()== true){
			
			icicicode=rs.getString("icicicode");

			beta=rs.getDouble("Beta");
			

		
			ResultSet rs2=stmtDCF.executeQuery("select Beta from INDCFAnalysis where icicicode='"+icicicode+"'");
			if(rs2.next()== true){
				double beta2 = rs2.getDouble("Beta");
				System.out.println("Beta:"+ beta+"	Beta2:"+beta2);
				
				if(Math.abs((beta - beta2)) < 0.01){
					

					}else{
						//			System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4));
						
						String sql = "update INDCFAnalysis set  LastUpdated='"+FirmDataCollector.mmdd  +"'  where icicicode='"+icicicode+"'" ;
						System.out.println(sql);
						stmtDCF.executeUpdate(sql);
					}
			}
			
			stmtDCF.executeUpdate("update INDCFAnalysis set COE="+rs.getString("Cost of Equity")+",EstimatedPrice="+rs.getDouble("Estimated  Stock Price")+",DCFValue="+rs.getString(4)+"  where icicicode='"+icicicode+"'" );				
//			System.out.println("update DCFAnalysis set COE="+rs.getDouble("Cost of Equity")+",EstimatedPrice="+rs.getDouble("Estimated  Stock Price")+",DCFValue="+rs.getString(4)+", RSQR="+rs.getString("RSQR")+", Beta="+beta+",  Volatility="+rs.getString("Volatility Daily")+", where icicicode='"+icicicode+"'" );				

		}//while
	//	stmtXLS.close();
	//	stmtDCF.close();
		
		}
		catch(SQLException se){
			se.printStackTrace();
		}
		return  ;
	}
	
	
	public void doBetaCheck(){
		
		try{
			
			BufferedWriter outbuff = new BufferedWriter(new FileWriter("Renew Beta.xls"));
			outbuff.write("ICICICODE\t NAME \t Sector\n");
	
		System.out.println("Following Scrips need Beta refinement...");
		Statement stmt = connDCF.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT INDCFAnalysis.icicicode, INCompanysnap.Sector, Name FROM INDCFAnalysis, INCompanysnap WHERE (Now()-[INDCFAnalysis.LastUpdated]>90)  AND INCompanysnap.icicicode=INDCFAnalysis.icicicode ORDER BY INDCFAnalysis.LastUpdated");
		while(rs.next()== true){

//			stmtXLS.execute("select * from [test$] ");

			outbuff.write(rs.getString("icicicode")+"\t"+rs.getString("Name")+"\t"+rs.getString("Sector")+"\n");
//			stmtXLS.executeUpdate("insert into [test$] (Beta) values('"+rs.getString("icicicode")+"')");

		}
		System.out.println("***End of List***");
		outbuff.flush();
		}
		catch(Exception se){
			se.printStackTrace();
		}
		
	}
	
	protected void InsertCheck (){
try{
		Statement stmt = connfinance.createStatement();
		String sql = "select [DCF Over/Under value %] from [Stock Price$]" ;
		
		ResultSet rs= stmt.executeQuery(sql);
		while(rs.next()== true){
			
			System.out.println(rs.getString(1));
		}
		
		stmt.close();
	}
	catch(SQLException se){
		se.printStackTrace();
	}
	}
	
	
	public void doportfolioUpdate(){
	
		try{
			
			double growthratepercent=0 ;
			double annualizedgrowthratepercent = 0;
			double profit = 0;
			double costBasis = 0;
			double number ;
			double buyprice ;
			Date buydate ;
			
			double closeprice  ;
			Date currentDate  ;
			
		Statement stmt = connDCF.createStatement();
		Statement stmt2 = connDCF.createStatement();
		System.out.println("Performing Portfolio Update...");
		ResultSet rs = stmt.executeQuery("SELECT * FROM INportfolio order by icicicode ");
		ResultSet rs2 = null;
		while(rs.next()== true){
			String code = rs.getString("icicicode");
			double HVal = rs.getDouble("HighValue");
			 rs2=stmt2.executeQuery("select icicicode,ClosePrice,LastUpdated from INCompanysnap where icicicode='"+code+"'");
		
			if(rs2.next()== true){ // Stocks
				 number = rs.getDouble("number");
				 buyprice = rs.getDouble("buyprice");
				 buydate = rs.getDate("buydate");
				
				 closeprice = rs2.getDouble("ClosePrice");
				 currentDate = rs2.getDate("LastUpdated");
				
				  profit = number*(closeprice-buyprice);
					 growthratepercent = 100*(closeprice-buyprice)/buyprice;
					 annualizedgrowthratepercent = Math.log(closeprice/buyprice)*100*(365.0/((currentDate.getTime()-buydate.getTime())/86400000) );
					 costBasis = number*buyprice;
	
						String sqlprofit = "update INportfolio set  AnnualizedGrowth="+annualizedgrowthratepercent+",AbsoluteGrowth="+growthratepercent+" , profit="+profit+" where icicicode='"+code+"'" ;
	//					System.out.println(sqlprofit);
						stmt2.executeUpdate(sqlprofit);
		/*		if(closeprice > HVal){
				//Update Hgher Value and date	
					String sql = "update INportfolio set  HighValue="+closeprice+",HighDate='"+rs2.getDate("LastUpdated")+"' , Remarks='New High Set on "+(1+calendar.get(Calendar.MONTH))+"/"+calendar.get(Calendar.DATE) +"' where icicicode='"+code+"'" ;
					
					stmt2.executeUpdate(sql);
					
				}else{
					if(closeprice < 0.9*HVal){
						// send ALERT	
							System.out.println("ALERT !!: Scrip "+code+" has fallen by "+Math.rint(100*((closeprice-HVal)/HVal))+"%");
							String sql = "update INportfolio set  Remarks='Scrip has fallen by "+Math.rint(100*((closeprice-HVal)/HVal))+"%'  where icicicode='"+code+"'" ;
							stmt2.executeUpdate(sql);
						}
				}   */
			}// if Stocks
			 rs2=stmt2.executeQuery("select fundfamily,FundName,NAV,LastUpdated from INMutualFund where FundName='"+code+"'");
				
				if(rs2.next()== true){ // Funds
					
					 number = rs.getDouble("number");
					 buyprice = rs.getDouble("buyprice");
					 buydate = rs.getDate("buydate");
					
					 closeprice = rs2.getDouble("NAV");
					 currentDate = rs2.getDate("LastUpdated");
					 
					  profit = number*(closeprice-buyprice);
						 growthratepercent = 100*(closeprice-buyprice)/buyprice;
						 annualizedgrowthratepercent = Math.log(closeprice/buyprice)*100*(365.0/((currentDate.getTime()-buydate.getTime())/86400000) );
						 costBasis = number*buyprice;
						 System.out.println("APY.. "+annualizedgrowthratepercent ); 
							String sqlprofit = "update INportfolio set  AnnualizedGrowth="+annualizedgrowthratepercent+",AbsoluteGrowth="+growthratepercent+" , profit="+profit+" where icicicode='"+code+"'" ;
							stmt2.executeUpdate(sqlprofit);
				}
		}// while
//		rs.close();
	
	//	stmt.close();
	//	stmt2.close();
		
	}
	catch(Exception se){
		se.printStackTrace();
	}//catch
	
	}
	
	protected void finalize() {
		try{
			connfinance.close();
	}
	catch(SQLException se){
		se.printStackTrace(); 
		}
	
}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		new CompanyDAO().doDataSynch();
//		new CompanyDAO().doBetaCheck();
		new INCompanyDAO().doportfolioUpdate();
	}

}
