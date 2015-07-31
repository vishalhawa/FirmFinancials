package processing;

import jsc.distributions.*;
import java.util.*;

import execution.OptionPricing;

import parser.YahooHistoryValues;

public class NormalCurve {
	
	// Also calculating Option Pricing
//	The most significant limitations are:
//		the underestimation of extreme moves, yielding tail risk, which can be hedged with out-of-the-money options;
	// - Moneyness(m): The chance/probabilty that the option will be ITM (in the money) at expiry. if m = 1 => 84% chance
//		the assumption of instant, cost-less trading, yielding liquidity risk, which is difficult to hedge;
//		the assumption of a stationary process, yielding volatility risk, which can be hedged with volatility hedging;
//		the assumption of continuous time and continuous trading, yielding gap risk, which can be hedged with Gamma hedging.

//  Hedging: Delta hedging, 
// Collar, Risk-reversal, Straddle (Long / Short)
	// http://en.wikipedia.org/wiki/Category:Options
	
	Normal normal ;
public static	double confidence = 2.0 ; //3.33 ; //99.95% confidence 3.33 SD
public	double callPrice;
public	double putPrice;
public	double callDelta;
public	double putDelta;
public	double vega ; 
public	double callTheta ;
public	double putTheta ;
public	double putRho ;
public	double callRho ;
//	double Rf = 0.0/OptionPricing.tradingDays; let calling class give this
	public	double moneyness;
	public double odds;
	public	double IVolatility; // daily
	
	// Implied Volatility = Implied Vol = Option Price / (0.4 SpotPrice* Sqrt(t) )
	public NormalCurve(){
		
		normal = new Normal(); // STD Normal only
//		System.out.println("Upper Tail:  "+normal.upperTailProb(1));
//	System.out.println("PDF at 0.5:  "+normal.pdf(0.5));
//	System.out.println("CDF at 0.5:  "+normal.cdf(0.5));
	
	}
	private double calculateMoneyness(double d1, double d2){
		
		moneyness = normal.cdf((d1+d2)/2);
//		System.out.println("Moneyness %: "+ Math.round(100*moneyness));

		return moneyness;
	}
	public double calculateOdds(double price, double spotP, double vlty,double expiry){
		
		double volatility = vlty*Math.pow(expiry, 0.5);
		double stdev = spotP*volatility;
		odds = normal.cdf((price-spotP)/stdev);
 
		return odds;
	}
	public double calculateOptionPrice(double spotPrice, double strike, double days,double riskfree, double volatility ){
		
		double d1 = (Math.log(spotPrice/strike)+ (riskfree + Math.pow(volatility, 2)/2)*days)/(Math.sqrt(days)*volatility);
		double d2 = d1 - volatility*Math.sqrt(days);
		
		callPrice = spotPrice*normal.cdf(d1) - strike*Math.exp(-riskfree*days)*normal.cdf(d2) ;
		putPrice = strike*Math.exp(-riskfree*days)*normal.cdf(-d2) - spotPrice*normal.cdf(-d1) ;
		calculateGreeks(spotPrice,strike,days,riskfree, volatility,d1,d2);
		calculateMoneyness(d1, d2);
		calculateOdds(strike, spotPrice,volatility,days);
//		IVolatility=NewtonRaphsonIVol("c" , spotPrice ,strike, days, riskfree , double cm );
//		System.out.println("BS Op Price "+BlackScholes(spotPrice, strike, days, riskfree ,volatility));
		return callPrice;
	}
	
	private void calculateGreeks(double spotPrice, double strike, double days,double riskfree, double volatility,double d1,double d2){
	
//		System.out.println("d1: "+ d1);
		 callDelta = normal.cdf(d1); // N(d1)
		 putDelta = normal.cdf(d1) -1 ; // N(d1) -1
		 vega = spotPrice*normal.pdf(d1)*Math.sqrt(days); // 
		 callTheta = -1*spotPrice*normal.pdf(d1)*volatility/(2*Math.sqrt(days)) - normal.cdf(d2)*riskfree*strike*Math.exp(-riskfree*days);
		 putTheta = -1*spotPrice*normal.pdf(d1)*volatility/(2*Math.sqrt(days)) + normal.cdf(-d2)*riskfree*strike*Math.exp(-riskfree*days);	
		 callRho = strike*days*Math.exp(-riskfree*days)*normal.cdf(d2);
		 putRho = -strike*days*Math.exp(-riskfree*days)*normal.cdf(-d2);
	}

		
/*		'NewtonRaphson implied vol By Espen Gaarder Haug, The Collector
		'using Antimatter symmetry to solve for put, see "A Look in The Antimatter Mirror"
		'can be downloaded from http://www.wilmott.com/messageview.cfm?catid=4&threadid=3656
		NewtonRaphsonCollectorVol("p",65,66,0.5,0.1,3) should return 22.29% implied Black-Scholes vol
*/		
	public double NewtonRaphsonIVol(String CallPutFlag , double S , double X, double T, double r , double cm ) {

	double vi; double ci; 
	double Vegai ; double epsilon ;
	int counter , z ;
	
		z = 1;
		if( CallPutFlag == "p") { z = -1 ;}

		// Manaster and Koehler seed value (vi)

		vi = z * Math.sqrt((Math.abs(Math.log(S / X) + r * T) * 2 / T));

		ci = z * BlackScholes(S, X, T, r, vi);
		Vegai = z * BlackScholesVega(S, X, T, r, vi);
		epsilon = 1e-12;
		counter = 0;
		while (Math.abs(cm - ci) > epsilon){
			counter = counter + 1;
			if( counter > 30){	break; }
			vi = vi - (ci - cm) / Vegai;
			ci = z * BlackScholes(S, X, T, r, vi);
			Vegai = z * BlackScholesVega(S, X, T, r, vi);
		} // while
		IVolatility = ( z * vi); 
		return IVolatility;

		}

		public double BlackScholesVega(double S , double X, double T, double r ,double v){

			double d1 ;

			d1 = (Math.log(S / X) + (r + Math.pow(v, 2) / 2) * T) / (v * Math.sqrt(T));
		return( Math.exp(Math.pow(-d1,2) / 2) * S * Math.sqrt(T) / Math.sqrt(2 * Math.PI));

		}

		public double BlackScholes(double S , double X, double T, double r ,double v){

		double d1 ;

		d1 = (Math.log(S / X) + (r + Math.pow(v, 2) / 2) * T) / (v * Math.sqrt(T));
		return( S * normal.cdf(d1) - X * Math.exp(-r * T) * normal.cdf(d1 - v * Math.sqrt(T)));

		}

		
	
	public void display(){
		
		System.out.println("Call Price: "+ callPrice);
		System.out.println("Put Price: "+ putPrice);
		System.out.println("callDelta: "+ callDelta);
		System.out.println("putDelta: "+ putDelta);
		System.out.println(" vega: "+ vega);
		System.out.println("callTheta: "+ callTheta);
		System.out.println("putTheta: "+ putTheta);
		System.out.println("putRho: "+ putRho);
		System.out.println("callRho: "+ callRho);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String scrip = "TTM";
		Properties pp = new Properties();
		pp.setProperty("scrip", scrip);
		YahooHistoryValues par = new YahooHistoryValues(scrip,"41");
		
		System.out.println(" Vlty: "+par.volatility);
		System.out.println(" SDs: "+ 5/par.volatility*Math.pow(242, 0.5)); //round(100*par.yearlyVol));
		NormalCurve nc = new NormalCurve();
		System.out.println("Odds: "+nc.calculateOdds(41, 41, par.volatility, 242)); //(54.41, 55, 400, 1.21E-4, par.volatility));
		nc.display();
		System.out.println("CDF: "+nc.normal.cdf(-2));
		
	}

}
// PDF = exp(-0.5*x^2)/sqrt(2*Pi)

