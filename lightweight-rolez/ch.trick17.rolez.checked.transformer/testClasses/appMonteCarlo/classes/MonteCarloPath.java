package classes;

import java.util.Random;

import rolez.annotation.*;
import rolez.checked.lang.CheckedArray;

@Checked
class MonteCarloPath extends RatePath {
	
	final double volatility;
	final double expectedReturnRate;
	
	final CheckedArray<double[]> fluctuations;
	
	public MonteCarloPath(Returns returns, int steps) {
		super(returns, new CheckedArray<double[]>(new double[steps]));
		this.volatility = returns.volatility;
		this.expectedReturnRate = returns.expectedReturnRate;
		double[] fArr = new double[steps];
		this.fluctuations = new CheckedArray<double[]>(fArr); 
	}
	
	public void computeFluctuations(long randomSeed) {
		Random random = new Random(randomSeed);
		double mean = (this.expectedReturnRate - 0.5 * this.volatility * this.volatility) * this.dTime;
		double stdDev = this.volatility * Math.sqrt(this.dTime);
		for (int i = 0; i < this.fluctuations.arrayLength(); i++) {
			this.fluctuations.setDouble(i, mean + stdDev * random.nextGaussian());
		}
	}
	
	public void computePathValues(double startValue) {
		CheckedArray<double[]> pathValues = new CheckedArray<double[]>(new double[this.pathValues.arrayLength()]);
		pathValues.setDouble(0, startValue);
		for (int i = 1; i < this.pathValues.arrayLength(); i++) {
			pathValues.setDouble(i, pathValues.getDouble(i-1) * Math.exp(this.fluctuations.getDouble(i)));
		}
		this.pathValues = pathValues;
	}
}