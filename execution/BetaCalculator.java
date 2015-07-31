package execution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

import processing.Regression;

import dao.InPortfolioDAO;



public class BetaCalculator {

	 GregorianCalendar cal ;
	 int duration ;
	 double beta;
	 static double AdjBeta;
	 static double baseR2 = 0.4;
	 static double RSQR;
	 static ArrayList<Double> Volatility = new ArrayList<Double>();
	 String hosturl = "http://in.finance.yahoo.com/q/hp?s=";
	
	 BetaCalculator(String scrip1, String scrip2){
	//	duration = days;
		cal = new GregorianCalendar() ;
		//		System.out.println("Start date "+String.format("%1$te-%1$tb-%1$ty", cal));
		getBeta(cal,duration,scrip1,scrip2);
		System.out.println("The BETA: " +beta);
		System.out.println("ADJ BETA: " +AdjBeta);
		
	}
	
	 
		public void getBeta(GregorianCalendar todaydate,int duration,String scrip1, String scrip2){
			
			GregorianCalendar pastdate = (GregorianCalendar)cal.clone();
			pastdate.add(Calendar.DAY_OF_MONTH, -duration);
			LinkedHashMap<String,List<Double>> quoteMap = new LinkedHashMap<String,List<Double>>();
			Map<String,List<Double>> Quotes = getValues(quoteMap,scrip1); 
			System.out.println(scrip1+" Volatility: "+Volatility.get(0));
			 Quotes = getValues(quoteMap,scrip2); 
			 System.out.println(scrip2+" Volatility: "+Volatility.get(1));
			//Quotes and quoteMap same data set??		 
			try{
				
				BufferedWriter outbuff = new BufferedWriter(new FileWriter("quotes.xls"));
	
				//double[] d = Quotes.values().toArray(new double[0]);
				//System.out.println("Value List "+l);
		
			Iterator<Map.Entry<String,List<Double>>> it = Quotes.entrySet().iterator();
	
			while(it.hasNext()){
			Map.Entry<String,List<Double>> e = it.next(); 
		
			if(e.getValue().size()<2){ it.remove(); }		 // drop all list entries whose size is less than 2 (untraded scrips for the day)
		
			else{
					outbuff.write(e.getKey()+"\t"+e.getValue().get(0)+"\t"+e.getValue().get(1)+"\n");  
		//			DataYX[i][0] = e.getValue().get(1); // Y value
		//				DataYX[i][1] = e.getValue().get(0); // X value
				//		System.out.println(DataYX[i][0]+" "+DataYX[i][1]);
		//					i++;
			}// else
			}// while
			outbuff.flush();
			outbuff.close();
			double DataYX[][]= new double[Quotes.size()][2]; // Alocate size properly
			
			it = Quotes.entrySet().iterator();
			int k = 0;
			while(it.hasNext()){
				Map.Entry<String,List<Double>> e = it.next(); 
				DataYX[k][0] = e.getValue().get(0); // Y value
				DataYX[k][1] = e.getValue().get(1); // X value
				k++;
			}
			double[] coeff = Regression.linear_equation(DataYX, 1);
			beta = coeff[1];
			RSQR = Regression.rsquared;
			AdjBeta = beta*(baseR2/Regression.rsquared);
			System.out.println("RSQR: "+Regression.rsquared);
			System.out.println("Intercept: "+coeff[0]);
			
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			
			System.out.println("Mape Size: "+Quotes.size());
		   //(Quotes.get(String.format("%1$te-%1$tb-%1$ty", todaydate))- Quotes.get(String.format("%1$te-%1$tb-%1$ty", pastdate)))/Quotes.get(String.format("%1$te-%1$tb-%1$ty", pastdate)) ;
		
				}
		
		private LinkedHashMap<String,List<Double>> getValues(LinkedHashMap<String,List<Double>> quoteMap,String scrip){
			

			try{
			URL url = new URL(hosturl+scrip);
			BufferedReader in  = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder sbuild = new StringBuilder();
			String s;
			while (( s = in.readLine())!= null){
				
				sbuild.append(s);
			}
			


			StringReader sreader = new StringReader(sbuild.toString());
			sreader.reset();
			Scanner scan = new Scanner(sreader);
			
			String key;
			String prevVal=null;
			double mean =0;
			double stdev =0;
			int k =0; // counter for mean and stdev
			
			while(( key= scan.findWithinHorizon("\\d?\\d-\\p{Alpha}\\p{Alpha}\\p{Alpha}-08", 0))!=null){
			//	System.out.println("Scanval->"+key);
				//scanner should not go pass </td>
				String val;
				 scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
				 scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
				 scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
				scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
				 scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
				 val = scan.findWithinHorizon("((-?(\\d+\\,\\d+)*(\\d+\\.?\\d*))(</td>| ))|<tr>", 0);
				
				val=val.replaceAll("</td>", "");
				val=val.replaceAll("<tr>", "null");
				val=val.replaceAll(",", ""); // to avoid DB field(Number)format error 
				k++;
				//System.out.println("Val->"+val);
				//prop.entrySet(key,Double.parseDouble(val)); //setProperty(key, val);
				if (quoteMap.get(key) ==null){
					quoteMap.put(key, new LinkedList<Double>());
				}
				double gain =0;
							//System.out.println("Volatility: "+stdev);
				if(prevVal!=null){ gain = Math.log(Double.parseDouble(prevVal)/Double.parseDouble(val));

				quoteMap.get(key).add(gain);
				double prevmean = mean;
				mean = prevmean + (gain -prevmean)/k;
				stdev = stdev + (gain-prevmean)*(gain-mean);

				}
				
				prevVal =val;
			//	quoteMap.put(key, Double.parseDouble(val));
			}//  while
			Volatility.add(Math.sqrt(stdev/k))  ;
			
			}// try
			catch(Exception ex){
				ex.printStackTrace();
			}
			
			return quoteMap;
			
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	     System.out.print("Enter Yahoo Code Scrip name for Calculating BETA with Sensex: "); 

	      //  open up standard input 
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 

	      String scripName = null; 

	      //  read the username from the command-line; need to use try/catch with the 
	      //  readLine() method 
	      try { 
	    	  scripName = br.readLine(); 
	    	  new BetaCalculator(scripName+".NS","%5EBSESN");
	    	  
	    	  System.out.print(scripName+": Do you wish to commit Adj Beta, Volatility, RSQR? Y/N"); 
		 
		       String response = br.readLine(); 
		       if(response.equals("Y")){ 
		    	   
		    	   System.out.print("Enter ICICI Scrip Name: "); 
		    		  scripName = br.readLine(); 
		    	   // Commit new values to DB 'DCFAnalysis'
		    		  new InPortfolioDAO().UpdateBetaVOL(scripName, AdjBeta, 100*Volatility.get(0), RSQR);
		    		   System.out.print("Values Commited: "); 
		       }else{
		    	   System.out.print("You have decided nto to commit: Exiting... "); 
		       }
	      } catch (IOException ioe) { 
	         System.out.println("IO error trying to read scrip name!"); 
	         System.exit(1); 
	      } 

		

	}

}
