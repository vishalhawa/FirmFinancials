package execution;

import java.sql.ResultSet;
import java.util.*;

import processing.Regression;

import dao.InPortfolioDAO;

public class RelativeAnalysis {

	InPortfolioDAO pDAO;
	String[] sector={"fertilisers","power","textiles","diversified","computers","metals","auto","cement","pharmaceuticals","steel","oil","telecommunications","construction","personal","shipping","sugar","engineering","chemicals","entertainment/multi","transport","finance","infrastructure"};			

	RelativeAnalysis(){
		
		pDAO = new InPortfolioDAO();
	
	}
	
	public void ComputeRelAnaysis(){
		
		for (String sec:sector){
			System.out.println("SECTOR:..... "+sec);
			HashMap sectorMap = pDAO.getSectorData(sec);
			CalculatePBV(sectorMap);
			CalculatePS(sectorMap);
			CalculateEarningsGrowth(sectorMap);
			pDAO.persistRelativeAnalysis(sectorMap);
			pDAO.StoreAverages(sec);
			
			
		}
	
	}
	
	private void CalculateEarningsGrowth(HashMap sdata){
	
	Iterator it = sdata.entrySet().iterator();
		
		while(it.hasNext()){
		Map.Entry e = (Map.Entry)it.next(); 
		Properties prop = (Properties)e.getValue();
		
		if(!(prop.getProperty("Actual").equals("0.0")||prop.getProperty("EPS2").equals("0.0"))){

			Double EarningsGrowth = (100/2)*(Double.parseDouble(prop.getProperty("EPS2"))-Double.parseDouble(prop.getProperty("Actual")))/Math.abs(Double.parseDouble(prop.getProperty("Actual")));
		//	System.out.println("EarningsGrowth: "+EarningsGrowth);	
			prop.setProperty("EarningsGrowth%", String.valueOf(EarningsGrowth));
		}
		}

	}
	
	private void CalculatePS(HashMap sdata){
		
		ArrayList<doubleYX> al = new ArrayList<doubleYX>();
		Iterator it = sdata.entrySet().iterator();
		
		while(it.hasNext()){
		Map.Entry e = (Map.Entry)it.next(); 
		Properties prop = (Properties)e.getValue();
		if(!(prop.getProperty("MCap").equals("0.0")||prop.getProperty("Sales").equals("0.0")||prop.getProperty("NPM").equals("0.0"))){
			Double ps = Double.parseDouble(prop.getProperty("MCap"))/Double.parseDouble(prop.getProperty("Sales"));
			Double npm = Double.parseDouble(prop.getProperty("NPM"));
			doubleYX yx= new doubleYX(ps,npm);
			al.add(yx);
			prop.setProperty("PSActual", String.valueOf(ps));
			prop.setProperty("PSRegr", "TBD");
			prop.setProperty("NPM", String.valueOf(npm));
			
	
		}
		e.setValue(prop);
		}//while
		double DataYX[][]= new double[al.size()][2];
		int k = 0;
		for(doubleYX yx: al){
			 DataYX[k][0] = yx.Y;
				 DataYX[k][1]= yx.X;
				 k++;
		}
		
		double[] coeff = Regression.linear_equation(DataYX, 1);
		double slope = coeff[1];

		System.out.println("Sample Size: "+al.size());
		System.out.println("Slope: "+slope);
		System.out.println("RSQR: "+Regression.rsquared);
		System.out.println("Intercept: "+coeff[0]);
		
		it = sdata.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry e = (Map.Entry)it.next(); 
			Properties prop = (Properties)e.getValue();
			
			if(prop.getProperty("PSRegr")!=null){
				prop.setProperty("PSRegr", String.valueOf(slope*Double.parseDouble(prop.getProperty("NPM"))+coeff[0]));
				prop.setProperty("PSRsquare",String.valueOf(Regression.rsquared));
			}
		//	System.out.println("Sector prop :"+prop);
		}
		//System.out.println("Sector data :"+sdata);
	}
	
	private void CalculatePBV(HashMap sdata){
		
		ArrayList<doubleYX> al = new ArrayList<doubleYX>();
		Iterator it = sdata.entrySet().iterator();
		
		while(it.hasNext()){
		Map.Entry e = (Map.Entry)it.next(); 
		Properties prop = (Properties)e.getValue();
		if(!(prop.getProperty("MCap").equals("0.0")||prop.getProperty("BookValue").equals("0.0")||prop.getProperty("ROE").equals("0.0"))){
			Double pbv = Double.parseDouble(prop.getProperty("MCap"))/Double.parseDouble(prop.getProperty("BookValue"));
			Double roe = Double.parseDouble(prop.getProperty("ROE"));
			doubleYX yx= new doubleYX(pbv,roe);
			al.add(yx);
			prop.setProperty("PBVActual", String.valueOf(pbv));
			prop.setProperty("PBVRegr", "TBD");
			prop.setProperty("ROE", String.valueOf(roe));
			
			//System.out.println(+pbv+"\t"+roe);	
		}
		e.setValue(prop);
		}//while
		double DataYX[][]= new double[al.size()][2];
		int k = 0;
		for(doubleYX yx: al){
			 DataYX[k][0] = yx.Y;
				 DataYX[k][1]= yx.X;
				 k++;
		}
		
		double[] coeff = Regression.linear_equation(DataYX, 1);
		double slope = coeff[1];

		System.out.println("Sample Size: "+al.size());
		System.out.println("Slope: "+slope);
		System.out.println("RSQR: "+Regression.rsquared);
		System.out.println("Intercept: "+coeff[0]);
		
		it = sdata.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry e = (Map.Entry)it.next(); 
			Properties prop = (Properties)e.getValue();
			
			if(prop.getProperty("PBVRegr")!=null){
				prop.setProperty("PBVRegr", String.valueOf(slope*Double.parseDouble(prop.getProperty("ROE"))+coeff[0]));
				prop.setProperty("PBVRsquare",String.valueOf(Regression.rsquared));
			}
		//	System.out.println("Sector prop :"+prop);
		}
		//System.out.println("Sector data :"+sdata);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new RelativeAnalysis().ComputeRelAnaysis();

	}

	class doubleYX{
		
		double Y;
		double X;
		double YR; // regression value of Y
		doubleYX(double y,double x){
			Y=y;
			X=x;
		}
	}
	
}
