package execution;


import java.io.*;
import java.util.*;

import dao.InPortfolioDAO;


public class ScripAnalyzer {


	InPortfolioDAO pDAO;
	/**
	 * @param args
	 */
	ScripAnalyzer(String scrip){
	pDAO = new InPortfolioDAO();
	Properties prop = pDAO.getcompanydata(scrip);

	System.out.println(prop);
	System.out.println("Sector Average for "+prop.getProperty("Sector")+" Sector:-");
	System.out.println(	pDAO.getSectorAvg(prop.getProperty("Sector")));
	
	}
	
	
	public static void main(String[] args) {
//	  prompt the user to enter their name 
	      System.out.print("Enter ICICI Code Scrip name: "); 

	      //  open up standard input 
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 

	      String scripName = null; 

	      //  read the username from the command-line; need to use try/catch with the 
	      //  readLine() method 
	      try { 
	    	  scripName = br.readLine(); 
	    	  new ScripAnalyzer(scripName);
	      } catch (IOException ioe) { 
	         System.out.println("IO error trying to read scrip name!"); 
	         System.exit(1); 
	      } 

	      System.out.println("Thanks for the name, " + scripName); 

	   } 


	}


