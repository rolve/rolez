package rolez.checked.util;

import rolez.checked.lang.Checked;


public class Random extends Checked {
	
	private java.util.Random random;
	
	public Random() {
		random = new java.util.Random();
	}
	
	public Random(long seed) {
		random = new java.util.Random(seed);
	}
	
	public boolean nextBoolean() {
		return checkLegalRead(this).random.nextBoolean();
	}
	
	public void nextBytes(byte[] bytes) {
		checkLegalRead(this).random.nextBytes(bytes);
	}
	
	public double nextDouble() {
		return checkLegalRead(this).random.nextDouble();
	}
	
	public float nextFloat() {
		return checkLegalRead(this).random.nextFloat();
	}
	
	public double nextGaussian() {
		return checkLegalRead(this).random.nextGaussian();
	}
	
	public int nextInt() {
		return checkLegalRead(this).random.nextInt();
	}
	
	public int nextInt(int n) {
		return checkLegalRead(this).random.nextInt(n);
	}
	
	public long nextLong() {
		return checkLegalRead(this).random.nextLong();
	}
	
	public void setSeed(long seed) {
		checkLegalWrite(this).random.setSeed(seed);
	}
}
