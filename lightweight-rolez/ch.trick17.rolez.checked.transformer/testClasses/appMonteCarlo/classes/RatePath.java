package classes;

import rolez.annotation.*;

import rolez.checked.lang.CheckedArray;

@Checked
class RatePath extends Path {

	CheckedArray<double[]> pathValues;
	
	public RatePath(String name, int startDate, int endDate, double dTime, CheckedArray<double[]> pathValues) {
		super(name, startDate, endDate, dTime);
		this.pathValues = pathValues;
	}
	
	public RatePath(Path other, CheckedArray<double[]> pathValues) {
		super(other);
		this.pathValues = pathValues;
	}
	
	
}