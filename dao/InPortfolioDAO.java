package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import processing.FirmDataCollector;

public class InPortfolioDAO extends PortfolioDAO {

	public InPortfolioDAO() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public Properties getcompanydata(String code){
		
		Properties cprop = new Properties();
		cprop.setProperty("icicicode", code);
		try{
	
			Statement stmt = portfolio.createStatement();
	
			ResultSet rs = stmt.executeQuery("select PE,ROE,NPM,Sector,DCFValue,ClosePrice, Volatility, Beta, INcompanysnap.LastUpdated from INCompanysnap, INDCFAnalysis WHERE INcompanysnap.icicicode='"+code+"'  and INCompanysnap.icicicode=INDCFAnalysis.icicicode");
			
			while(rs.next()== true){
				cprop.setProperty("Sector", String.valueOf(rs.getString("Sector")));
				cprop.setProperty("ClosePrice", String.valueOf(rs.getDouble("ClosePrice")));

				cprop.setProperty("Volatility", String.valueOf(rs.getDouble("Volatility")));
				cprop.setProperty("Beta", String.valueOf(rs.getDouble("Beta")));
				cprop.setProperty("DCFValue", String.valueOf(rs.getDouble("DCFValue")));
				
				cprop.setProperty("NPM", String.valueOf(rs.getDouble("NPM")));
				cprop.setProperty("ROE", String.valueOf(rs.getDouble("ROE")));
				cprop.setProperty("PE", String.valueOf(rs.getDouble("PE")));
				cprop.setProperty("lastdate", String.valueOf(rs.getDate("LastUpdated")));
			}
	
			stmt.close();
			
			}
			catch(SQLException se){
				se.printStackTrace();
			}
	
		return cprop;
	}

	public List getDCFList(){
			
			List DCFlist = new ArrayList();
			try{
		
			Statement stmt = portfolio.createStatement();
			ResultSet rs = stmt.executeQuery("select icicicode,lastupdated from INdcfAnalysis order by icicicode");
			
			while(rs.next()== true){
				DCFlist.add(rs.getString("icicicode"));
	//			System.out.println(rs.getString(1));
			}
	
			stmt.close();
			
			}
			catch(SQLException se){
				se.printStackTrace();
			}
			return DCFlist ;
	
		}

	public List getICICICODE(){
	
		List codelist = new ArrayList();
		try{
			
		
		Statement stmt = portfolio.createStatement();
		
		ResultSet rs = stmt.executeQuery("select icicicode from INCompanysnap WHERE (((Now()-[LastUpdated])>5)) OR (((INCompanysnap.LastUpdated) Is Null)) order by LastUpdated");
		
		while(rs.next()== true){
			codelist.add(rs.getString("icicicode"));
	
		}
	
		stmt.close();
		
		}
		catch(SQLException se){
			se.printStackTrace();
		}
		return codelist ;
	}

	public LinkedHashMap<String,Properties> getMFListing(){
		
		LinkedHashMap<String,Properties> MFList = new LinkedHashMap<String,Properties>();
		try{
			
		
		Statement stmt = portfolio.createStatement();
		
		ResultSet rs = stmt.executeQuery("select searchString, FundFamily,FundName from INMutualFund WHERE (((Now()-[LastUpdated])>5)) OR (((INMutualFund.LastUpdated) Is Null)) order by LastUpdated");
		
		while(rs.next()== true){
			Properties fundprop = new Properties(); 
			fundprop.setProperty("FundName", rs.getString("FundName"));
			fundprop.setProperty("searchString", rs.getString("searchString"));
			MFList.put(rs.getString("FundFamily"),fundprop);
	
		}
	
		stmt.close();
		
		}
		catch(SQLException se){
			se.printStackTrace();
		}
		return MFList ;
	}

	public HashMap getSectorAvg(String sector){
	
		LinkedHashMap<String,Double> sectorAvg = new LinkedHashMap<String,Double>();
		try{
			
		Statement stmt = portfolio.createStatement();
	
		ResultSet rs = stmt.executeQuery("SELECT * FROM  INSectorData  WHERE (Sector='"+sector+"')" );
		
		while(rs.next()== true){
			sectorAvg.put("Beta", rs.getDouble("Beta"));
			sectorAvg.put("Volatility", rs.getDouble("Volatility"));
			sectorAvg.put("NPM", rs.getDouble("NPM"));
			sectorAvg.put("ROE", rs.getDouble("ROE"));
			sectorAvg.put("PE", rs.getDouble("PE"));
			sectorAvg.put("EarningsGrowthP", rs.getDouble("EarningsGrowthP"));
	
		}
		}
		catch(SQLException se){
			se.printStackTrace();
		}	
		return sectorAvg;
	}

	public HashMap getSectorData(String sector){
		
	HashMap<String,Properties> sectorMap = new LinkedHashMap<String,Properties>();
		try{
	
			Statement stmt = portfolio.createStatement();
	
			ResultSet rs = stmt.executeQuery("SELECT INCompanysnap.MCap,INCompanysnap.actual,INCompanysnap.EPS2,INCompanysnap.icicicode, INDCFAnalysis.DCFValue, INDCFAnalysis.COE,INCompanysnap.NPM, INCompanysnap.PE, INCompanysnap.ROE, INCompanysnap.BookValue, INCompanysnap.ClosePrice, INCompanysnap.Sales,INCompanysnap.LastUpdated, INDCFAnalysis.Volatility FROM  INCompanysnap LEFT JOIN  INDCFAnalysis ON INCompanysnap.icicicode = INDCFAnalysis.icicicode WHERE (INCompanysnap.Sector='"+sector+"')  order by INCompanysnap.icicicode");
			
			while(rs.next()== true){
				Properties sprop = new Properties();
				//sprop.setProperty("Sector", sector);
				sprop.setProperty("DCFValue", String.valueOf(rs.getDouble("DCFValue")));
				sprop.setProperty("MCap", String.valueOf(rs.getDouble("MCap")));
				sprop.setProperty("Volatility", String.valueOf(rs.getDouble("Volatility")));
				sprop.setProperty("COE", String.valueOf(rs.getDouble("COE")));
				sprop.setProperty("PE", String.valueOf(rs.getDouble("PE")));
				sprop.setProperty("ROE", String.valueOf(rs.getDouble("ROE")));
				sprop.setProperty("BookValue", String.valueOf(rs.getDouble("BookValue")));
				sprop.setProperty("ClosePrice", String.valueOf(rs.getDouble("ClosePrice")));
				sprop.setProperty("Sales", String.valueOf(rs.getDouble("Sales")));
				sprop.setProperty("NPM", String.valueOf(rs.getDouble("NPM")));
				sprop.setProperty("Actual", String.valueOf(rs.getDouble("actual")));
				sprop.setProperty("EPS2", String.valueOf(rs.getDouble("EPS2")));
				sectorMap.put(String.valueOf(rs.getString("icicicode")), sprop);
	
			}
	
			stmt.close();
			
			}
			catch(SQLException se){
				se.printStackTrace();
			}
			System.out.println("Sector data SIZE:"+sectorMap.size());
	//System.out.println("Sector data:"+sectorMap);
		return sectorMap;
	}

	public void persistData(Map dataMap){
			try{
	
				Statement stmt = portfolio.createStatement();
				Object Flag = dataMap.remove("pagetype");
				
				Object[] keyset = dataMap.keySet().toArray();
				for(int x=0; x<keyset.length ;x++ ){
					 
				String sql = "";
			
				if(Flag.toString().equals("index")){ // INDEX
					Properties prop = (Properties)dataMap.get(keyset[x]);
					 sql = "update Configparameters set Value1="+prop.get("Index Value:")+",Value2="+prop.get("NewVolatility")+",Value3="+prop.get("NewBeta")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where Name='"+keyset[x]+"'" ;
	//				 System.out.println(sql);
				}
	
					if(Flag.toString().equals("company")){ // COMPANY
						Properties compprop = (Properties)dataMap.get(keyset[x]);
	
	//					stmt.executeUpdate("insert into Companysnap (icicicode,actual,EPS1,EPS2,DebtRatio,OPM,GPM,NPM) values ('"+keyset[x]+"','"+compprop.get("Actual")+"','"+compprop.get("EPS1")+"','"+compprop.get("EPS2")+"','"+compprop.get("Debt / Equity")+"','"+compprop.get("OPM (%)")+"','"+compprop.get("GPM (%)")+"','"+compprop.get("NPM (%)")+"')");
						 sql = "update INCompanysnap set MCap="+compprop.get("Market Capitalization")+", Sales="+compprop.get("Sales Income")+",actual="+compprop.get("Actual")+",EPS1="+compprop.get("EPS1")+",EPS2="+compprop.get("EPS2")+",DebtRatio="+compprop.get("Debt / Equity")+",OPM="+compprop.get("OPM (%)")+",GPM="+compprop.get("GPM (%)")+",NPM="+compprop.get("NPM (%)")+", ClosePrice="+compprop.get("Close Price")+", DividendYield="+compprop.get("Dividend Yield %")+", ROE="+compprop.get("Return on Net Worth")+", PE="+compprop.get("P/E")+", BookValue="+compprop.get("Book Value")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where icicicode='"+keyset[x]+"'" ;
	//					 System.out.println(sql);
						 stmt.executeUpdate(sql);
						 sql = "update INDCFAnalysis set Beta="+compprop.get("NewBeta")+",Volatility="+compprop.get("NewVolatility")+",LastUpdated='"+FirmDataCollector.mmdd +"'  where icicicode='"+keyset[x]+"'" ;
						 
					}
	
				
						if(Flag.toString().equals("Funds")){ // FUNDS
								Properties fundProp = (Properties)dataMap.get(keyset[x]);
								
	
	//							stmt.executeUpdate("insert into Companysnap (icicicode,actual,EPS1,EPS2,DebtRatio,OPM,GPM,NPM) values ('"+keyset[x]+"','"+compprop.get("Actual")+"','"+compprop.get("EPS1")+"','"+compprop.get("EPS2")+"','"+compprop.get("Debt / Equity")+"','"+compprop.get("OPM (%)")+"','"+compprop.get("GPM (%)")+"','"+compprop.get("NPM (%)")+"')");
								 sql = "update INMutualFund set NAV="+fundProp.get(fundProp.propertyNames().nextElement())+",LastUpdated='"+FirmDataCollector.mmdd+"'  where FundName='"+fundProp.propertyNames().nextElement().toString()+"'" ;
								 System.out.println(sql);
								 stmt.executeUpdate(sql);
							//	 stmt.executeUpdate(sql);
							}// Funds
	/*						
					else{ // SECTOR
						ArrayList sectorlist = (ArrayList)dataMap.get(keyset[x]);
	//					String sql = "insert into FirmData (icicicode,sectorID,firmname,LastFYNetProfit,PrevQNetProfit,CurrQNetProfit,EPS,PE,BookValue,RONW,MCap,LTQ,DPS,YearHigh,YearLow,LTP,Rating) values ('"+keyset[x];
						 sql = "Update FirmData set sectorID='"+sectorlist.get(0)+"', firmname='"+sectorlist.get(1)+"', LastFYNetProfit='"+sectorlist.get(2)+"', PrevQNetProfit='"+sectorlist.get(3)+"', CurrQNetProfit='"+sectorlist.get(4)+"', EPS='"+sectorlist.get(5)+"', PE='"+sectorlist.get(6)+"', BookValue='"+sectorlist.get(7)+"', RONW='"+sectorlist.get(8)+"', MCap='"+sectorlist.get(9)+"', LTQ='"+sectorlist.get(10)+"', DPS='"+sectorlist.get(11)+"', YearHigh='"+sectorlist.get(12)+"', YearLow='"+sectorlist.get(13)+"', LTP='"+sectorlist.get(14)+"', Rating='"+sectorlist.get(15)+"', LastUpdated='"+(1+calendar.get(Calendar.MONTH))+"/"+calendar.get(Calendar.DATE) +"' where icicicode='"+keyset[x]+"'";
		
					}
	*/					
					 stmt.executeUpdate(sql);
	
				}
			
			stmt.close();
	//		portfolio.commit();
	//		portfolio.close(); //clears and flushes out the last run statement
			
			
			}
			catch(SQLException se){
				se.printStackTrace();
			}
		}

	public void persistRelativeAnalysis(HashMap sectorMap){
			
			try{
			Iterator it = sectorMap.entrySet().iterator();
			Statement stmt = portfolio.createStatement();
			while(it.hasNext()){
				Map.Entry e = (Map.Entry)it.next(); 
				Properties prop = (Properties)e.getValue();
				
				String sql = "update INRelativeAnalysis set EarningsGrowthP="+prop.get("EarningsGrowth%")+",PBVActual="+prop.get("PBVActual")+",PBVRegr="+prop.get("PBVRegr")+",PSActual="+prop.get("PSActual")+",PSRegr="+prop.get("PSRegr")+",PSRsquare="+prop.get("PSRsquare")+",PBVRsquare="+prop.get("PBVRsquare")+"  where icicicode='"+e.getKey()+"'" ;
			//	System.out.println("SQL: "+sql);			
					stmt.executeUpdate(sql);
				
				}
			stmt.close();
			}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

	public void StoreAverages(String sector){
		
		try{
	
			Statement stmt2 = portfolio.createStatement();
			
			String sql = "SELECT 100*(1/2)*avg((EPS2-actual)/actual) as AEGR,avg(roe) as AROE,Avg(beta) AS ABeta, avg(rsqr) AS ARsqr, avg(volatility) AS Vol, avg(NPM) AS ANPM, avg(PE) AS APE FROM INdcfanalysis, INcompanysnap WHERE (((INcompanysnap.Sector)='"+sector+"')) And INcompanysnap.icicicode=INdcfanalysis.icicicode";
	
			 //sql = "SELECT Avg(beta) as ABeta FROM dcfanalysis, companysnap WHERE (((companysnap.Sector)='"+sector+"')) And companysnap.icicicode=dcfanalysis.icicicode";
	
			ResultSet rset = stmt2.executeQuery(sql);
			rset.next();
				sql = "Update INSectorData set EarningsGrowthP="+rset.getDouble("AEGR")+",ROE="+rset.getDouble("AROE")+",beta="+rset.getDouble("ABeta")+" , volatility="+rset.getDouble("Vol")+" , NPM="+rset.getDouble("ANPM")+" , PE="+rset.getDouble("APE")+",LastUpdated='"+FirmDataCollector.mmdd  +"' where sector='"+sector+"'";
				//System.out.println(sql);
				stmt2.executeUpdate(sql);
			
			stmt2.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		
	}

	public void UpdateBetaVOL(String scripname, double beta, double vol, double rsqr){
		
		// This is from Beta Calculator - resetting beta, Vol etc
		try{
		Statement stmt = portfolio.createStatement();
		String sql = "update INDCFAnalysis set Beta="+beta+", Volatility="+vol+", RSQR="+rsqr+"  where icicicode='"+scripname+"'";
	
		stmt.executeUpdate(sql );				
		stmt.executeUpdate("update INDCFAnalysis  Set LastUpdated='"+ FirmDataCollector.mmdd+"' where icicicode='"+scripname+"'");
		portfolio.commit();
		stmt.close();
		}
		catch (Exception ex){ex.printStackTrace();}
		
	}

}
