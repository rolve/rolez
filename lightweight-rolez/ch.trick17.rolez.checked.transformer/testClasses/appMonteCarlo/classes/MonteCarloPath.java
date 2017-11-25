package classes;

import java.util.Random;

import rolez.annotation.*;
import rolez.checked.lang.CheckedArray;

@Checked
class MonteCarloPath extends RatePath {
	
	final double volatility;
	final double expectedReturnRate;
	
	final int steps;
	
	CheckedArray<double[]> fluctuations;
	
	public MonteCarloPath(Returns returns, int steps) {
		super(returns, new CheckedArray<double[]>(new double[steps]));
		this.volatility = returns.volatility;
		this.expectedReturnRate = returns.expectedReturnRate;
		this.steps = steps;
	}
	
	public void computeFluctuations(long randomSeed) {
		Random random = new Random(randomSeed);
		double mean = (this.expectedReturnRate - 0.5 * this.volatility * this.volatility) * this.dTime;
		double stdDev = this.volatility * Math.sqrt(this.dTime);
		CheckedArray<double[]> fluctuations = new CheckedArray<double[]>(new double[steps]);
		fluctuations.setDouble(0, 0);
		for (int i = 0; i < fluctuations.arrayLength(); i++) {
			fluctuations.setDouble(i, mean + stdDev * random.nextGaussian());
		}
		this.fluctuations = fluctuations;
	}
	
	public void computePathValues(double startValue) {
		CheckedArray<double[]> pathValues = new CheckedArray<double[]>(new double[this.pathValues.arrayLength()]);
		CheckedArray<double[]> fluctuations = this.fluctuations;
		fluctuations.getDouble(0);
		pathValues.setDouble(0, startValue);
		for (int i = 1; i < pathValues.arrayLength(); i++) {
			pathValues.setDouble(i, pathValues.getDouble(i-1) * Math.exp(fluctuations.getDouble(i)));
		}
		this.pathValues = pathValues;
	}
}