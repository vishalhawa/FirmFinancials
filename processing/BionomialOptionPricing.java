package processing;

public class BionomialOptionPricing {



	double time_tm = .25;
	double r = 0.05;
	double sigma = 0.3;
	int no_periods = 6;
	int max_terminal_nodes;
	double tinc, r1per, disc, up, down, price_up, price_dn, cur_price;
	double[] val;
	
	public BionomialOptionPricing (int max_terminal_nodes) {
	val = new double[max_terminal_nodes];
	}
	private void newPars(double time_tm, int nper, double r, double sigma) {
	no_periods = nper;
	tinc = time_tm/(double) no_periods;
	r1per = 1.0 + r * tinc;
	disc = 1.0 / r1per;
	up = r1per + sigma * Math.sqrt(tinc);
	down = r1per - sigma * Math.sqrt(tinc);
	price_up = disc * 0.5;
	price_dn = disc * 0.5;
	}
	public double Eurcall (double spot, double strike, double time_tm, int nper, double r, double sigma) {
	int i, j; // i and j indicate the current node in the binomial tree
	double price;
//	 calulate the up and down parameters
	newPars(time_tm, nper, r, sigma);
	/*
	First, set all the values of the option at nodes at the terminal
	date and store these values in the array val. We set the counter i
	3(which can be interpreted as the number of up moves taken to get to
	the terminal node) equal to 0 and repeat so long as i is less than or equal
	to the number of periods.
*/
//	 initialize terminal payoffs: i is the number of up moves over the whole life
	for(i = 0; i <= no_periods; i++) {
	price = spot * Math.pow(up,(double) i) * Math.pow(down,(double) (no_periods - i));
	val[i] = Math.max(price - strike,(double) 0.0);
	}
//	 compute prices back through the tree
//	 j+1 is the number of periods from the end
//	 i is the number of up moves from the start
//	 can be computed inplace which is why the algotirhm is so efficient in memory
	for(j = 0; j < no_periods; j++) {
	for(i = 0; i < no_periods; i++) {
	val[i] = price_dn * val[i] + price_up * val[i+1];
	}
	}
	return(val[0]);
	}
	public static void main(String[] args) {
	int nop;
	double price, sig, ret, tom;
	Double dPrice;
	String strPrice;
	BionomialOptionPricing pricing = new BionomialOptionPricing(5001);
	nop = 10; // number of sub-periods
	tom = 0.25; // years
	ret = 0.05;
	sig = 0.4;
	price = pricing.Eurcall(90.0, 100.0, tom, nop, ret, sig);
	dPrice = new Double(price);
	System.out.println("Price is " + dPrice.toString());
	}

	/**
	 * @param args
	 */
	

}
