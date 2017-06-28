package rolez.light.wrappers;

import rolez.lang.Guarded;

public class Random extends Guarded {
	
	private java.util.Random random;
	
	public Random() {
		random = new java.util.Random();
	}
	
	public Random(long seed) {
		random = new java.util.Random(seed);
	}
	
	public boolean nextBoolean() {
		return random.nextBoolean();
	}
	
	public void nextBytes(byte[] bytes) {
		random.nextBytes(bytes);
	}
	
	public double nextDouble() {
		return random.nextDouble();
	}
	
	public float nextFloat() {
		return random.nextFloat();
	}
	
	public double nextGaussian() {
		return random.nextGaussian();
	}
	
	public int nextInt() {
		return random.nextInt();
	}
	
	public int nextInt(int n) {
		return random.nextInt(n);
	}
	
	public long nextLong() {
		return random.nextLong();
	}
	
	public void setSeed(long seed) {
		random.setSeed(seed);
	}
}
