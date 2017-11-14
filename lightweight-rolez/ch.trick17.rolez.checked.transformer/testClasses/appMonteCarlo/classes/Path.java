package classes;

import rolez.annotation.*;

@Checked
class Path {
	final String name;
	final int startDate;
	final int endDate;
	final double dTime;
	
	public Path(String name, int startDate, int endDate, double dTime) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.dTime = dTime;
	}
	
	public Path(Path other) {
		this.name = other.name;
		this.startDate = other.startDate;
		this.endDate = other.endDate;
		this.dTime = other.dTime;
	}
	
}