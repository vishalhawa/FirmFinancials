package processing;



	public class Regression {
		   public static double rsquared = 0;

		   // do some simple tests with the regression techniques
		   public static void main(String argv[]) {

		      // do a simple equation: Y = 1 + 2X : FORMAT:{Y,X}
		      double rawData[][] = {{	10.7732942	,	23.31	}	,
		    		  {	3.728559177	,	24.37	}	,
		    		  {	0.88358459	,	15.45	}	,
		    		  {	2.341319882	,	5.86	}	,
		    		  {	1.14884271	,	-2.26	}	,
		    		  {	0.777726645	,	2.11	}	,
		    		  {	6.343501541	,	9.72	}	,
		    		  {	0	,	-36.49	}	,
		    		  {	2.284117846	,	11.9	}	,
		    		  {	4.257322176	,	12.33	}	,
		    		  {	2.95043273	,	8.58	}	,
		    		  {	5.294637596	,	15.69	}	,
		    		  {	1.95431908	,	11.73	}	,
		    		  {	11.33487865	,	-2.5	}	,
		    		  {	0.359485368	,	-35.16	}	,
		    		  {	0.877038695	,	3.08	}	,
		    		  {	2.496810411	,	5.19	}	,
		    		  {	8.020287958	,	11.41	}	,
		    		  {	0.695388528	,	4.31	}	,
		    		  {	3.752212389	,	11.26	}	,
		    		  {	12.45539335	,	23.08	}	,
		    		  {	14.30004268	,	8.55	}	,
		    		  {	12.19997011	,	5.34	}	,
		    		  {	6.549145299	,	5.53	}	,
		    		  {	4.103810264	,	8.59	}		};
		      double coef[] = linear_equation(rawData, 1);
		      print_equation(coef);

		      rawData = new double[100][2];

		      // Second order equation: Y = 1 + 2X + 3X^2
		      for (int i = 0; i < rawData.length; i++) {
		         rawData[i][0] = 1 + 2*i + 3*i*i;
		         rawData[i][1] = i;
		      }
		      print_equation(linear_equation(rawData, 2));

		      // Third order equation: Y = 4 + 3X + 2X^2 + 1X^3
		      for (int i = 0; i < rawData.length; i++) {
		         rawData[i][0] = 4 + 3*i + 2*i*i + 1*i*i*i;
		         rawData[i][1] = i;
		      }
		      print_equation(linear_equation(rawData, 3));

		   }

		   // Apply least squares to raw data to determine the coefficients for
		   // an n-order equation: y = a0*X^0 + a1*X^1 + ... + an*X^n.
		   // Returns the coefficients for the solved equation, given a number
		   // of y and x data points. The rawData input is given in the form of
		   // {{y0, x0}, {y1, x1},...,{yn, xn}}.   The coefficients returned by
		   // the regression are {a0, a1,...,an} which corresponds to
		   // {X^0, X^1,...,X^n}. The number of coefficients returned is the
		   // requested equation order (norder) plus 1.
		  public static double[] linear_equation(double rawData[][], int norder) {
		      double a[][] = new double[norder+1][norder+1];
		      double b[] = new double[norder+1];
		      double term[] = new double[norder+1];
		      double ysquare = 0;

		//      System.out.println("SIZE: "+rawData.length);
		      // step through each raw data entries
		      for (int i = 0; i < rawData.length; i++) {
		   // 	  System.out.println(rawData[i][0]+" "+rawData[i][1]);
		         // sum the y values
		         b[0] += rawData[i][0];
		         ysquare += rawData[i][0] * rawData[i][0];

		         // sum the x power values
		         double xpower = 1;
		         for (int j = 0; j < norder+1; j++) {
		            term[j] = xpower;
		            a[0][j] += xpower;
		            xpower = xpower * rawData[i][1];
		         }

		         // now set up the rest of rows in the matrix - multiplying each row by each term
		         for (int j = 1; j < norder+1; j++) {
		            b[j] += rawData[i][0] * term[j];
		            for (int k = 0; k < b.length; k++) {
		               a[j][k] += term[j] * term[k];
		            }
		         }
		      }

		      // solve for the coefficients
		      double coef[] = gauss(a, b);

		      // calculate the r-squared statistic
		      double ss = 0;
		      double yaverage = b[0] / rawData.length;
		      for (int i = 0; i < norder+1; i++) {
		         double xaverage = a[0][i] / rawData.length;
		         ss += coef[i] * (b[i] - (rawData.length * xaverage * yaverage));
		      }
		      rsquared = ss / (ysquare - (rawData.length * yaverage * yaverage));

		      // solve the simultaneous equations via gauss
		      return coef;
		   }

		   // it's been so long since I wrote this, that I don't recall the math
		   // logic behind it. IIRC, it's just a standard gaussian technique for
		   // solving simultaneous equations of the form: |A| = |B| * |C| where we
		   // know the values of |A| and |B|, and we are solving for the coefficients
		   // in |C|
		   static double[] gauss(double ax[][], double bx[]) {
		      double a[][] = new double[ax.length][ax[0].length];
		      double b[] = new double[bx.length];
		      double pivot;
		      double mult;
		      double top;
		      int n = b.length;
		      double coef[] = new double[n];

		      // copy over the array values - inplace solution changes values
		      for (int i = 0; i < ax.length; i++) {
		         for (int j = 0; j < ax[i].length; j++) {
		            a[i][j] = ax[i][j];
		         }
		         b[i] = bx[i];
		      }

		      for (int j = 0; j < (n-1); j++) {
		         pivot = a[j][j];
		         for (int i = j+1; i < n; i++) {
		            mult = a[i][j] / pivot;
		            for (int k = j+1; k < n; k++) {
		               a[i][k] = a[i][k] - mult * a[j][k];
		            }
		            b[i] = b[i] - mult * b[j];
		         }
		      }

		      coef[n-1] = b[n-1] / a[n-1][n-1];
		      for (int i = n-2; i >= 0; i--) {
		         top = b[i];
		         for (int k = i+1; k < n; k ++) {
		            top = top - a[i][k] * coef[k];
		         }
		         coef[i] = top / a[i][i];
		      }
		      return coef;
		   }

		   // simple routine to print the equation for inspection
		   static void print_equation(double coef[]) {
		      for (int i = 0; i < coef.length; i++) {
		         if (i == 0) {
		            System.out.print("Y = ");
		         } else {
		            System.out.print(" + ");
		         }
		         System.out.print(coef[i] + "*X^" + i);
		      }
		      System.out.println("      [r^2 = " + rsquared + "]");
		   }
		}


	



