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
		return checkLegalWrite(this).random.nextBoolean();
	}
	
	public void nextBytes(byte[] bytes) {
		checkLegalWrite(this).random.nextBytes(bytes);
	}
	
	public double nextDouble() {
		return checkLegalWrite(this).random.nextDouble();
	}
	
	public float nextFloat() {
		return checkLegalWrite(this).random.nextFloat();
	}
	
	public double nextGaussian() {
		return checkLegalWrite(this).random.nextGaussian();
	}
	
	public int nextInt() {
		return checkLegalWrite(this).random.nextInt();
	}
	
	public int nextInt(int n) {
		return checkLegalWrite(this).random.nextInt(n);
	}
	
	public long nextLong() {
		return checkLegalWrite(this).random.nextLong();
	}
	
	public void setSeed(long seed) {
		checkLegalWrite(this).random.setSeed(seed);
	}
}
