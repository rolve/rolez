package classes;

import rolez.annotation.*;

import rolez.checked.lang.CheckedArray;

@Checked
class Returns extends Path {
	
	final double volatility;
	final double expectedReturnRate;
	
	public Returns(RatePath ratePath) {
		super(ratePath);
		
		double[] arr = new double[ratePath.pathValues.arrayLength()];
		CheckedArray<double[]> returnPathValues = new CheckedArray<double[]>(arr);
		returnPathValues.setDouble(0, 0.0);
		for (int i = 1; i < ratePath.pathValues.arrayLength(); i++) {
			returnPathValues.setDouble(i, 
					Math.log(ratePath.pathValues.getDouble(i) / ratePath.pathValues.getDouble(i-1)));
		}
		
		double mean = 0.0;
		for (int i = 1; i < returnPathValues.arrayLength(); i++) {
			mean += returnPathValues.getDouble(i);
		}
		mean /= returnPathValues.arrayLength() - 1;
		
		double variance = 0.0;
		for (int i = 1; i < returnPathValues.arrayLength(); i++) {
			variance += (returnPathValues.getDouble(i) - mean) * (returnPathValues.getDouble(i) - mean);
		}
		variance /= returnPathValues.arrayLength() - 1;
		
		double volatility2 = variance / ratePath.dTime;
		this.volatility = Math.sqrt(volatility2);
		this.expectedReturnRate = mean / ratePath.dTime + 0.5 * volatility2;
	}
	
}