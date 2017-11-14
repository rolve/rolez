package classes;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import rolez.annotation.*;
import rolez.checked.lang.CheckedArray;

class RatePathReader {
		
	int datumField = 4;
	int minDate = 19000101;
	double epsilon = 10.0 * Double.MIN_VALUE;
	
	public RatePath readRatesFile(String ratesFile) {
		try {
			Scanner scanner = new Scanner(new FileInputStream(ratesFile));
			
			ArrayList<String> lines = new ArrayList<String>();
			while (scanner.hasNextLine())
				lines.add(scanner.nextLine());
			
			double[] pvArr = new double[lines.size()];
			CheckedArray<double[]> pathValues = new CheckedArray<double[]>(pvArr);
			int[] pdArr = new int[lines.size()];
			CheckedArray<int[]> pathDates = new CheckedArray<int[]>(pdArr);
			
			for (int i = 0; i < lines.size(); i++) {
				String[] fields = lines.get(i).split(",");
				int date = Integer.parseInt("19" + fields[0]);
				double value = Double.parseDouble(fields[this.datumField]);
				if (date <= this.minDate || Math.abs(value) <= this.epsilon)
					System.exit(1);
				
				pathValues.setDouble(i, value);
				pathDates.setInt(i, date);
			}
			
			scanner.close();
			return new RatePath(ratesFile, pathDates.getInt(0), pathDates.getInt(lines.size() - 1),
					1.0 / 365.0, pathValues);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found!");
		}
	}
}