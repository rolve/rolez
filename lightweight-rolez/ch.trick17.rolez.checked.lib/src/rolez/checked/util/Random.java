package rolez.checked.util;

import rolez.checked.lang.Checked;


public class Random extends Checked implements WrapperType<java.util.Random> {
	
	private java.util.Random instance;
	
	public Random() {
		instance = new java.util.Random();
	}
	
	public Random(long seed) {
		instance = new java.util.Random(seed);
	}
	
	public boolean nextBoolean() {
		return checkLegalWrite(this).instance.nextBoolean();
	}
	
	public void nextBytes(byte[] bytes) {
		checkLegalWrite(this).instance.nextBytes(bytes);
	}
	
	public double nextDouble() {
		return checkLegalWrite(this).instance.nextDouble();
	}
	
	public float nextFloat() {
		return checkLegalWrite(this).instance.nextFloat();
	}
	
	public double nextGaussian() {
		return checkLegalWrite(this).instance.nextGaussian();
	}
	
	public int nextInt() {
		return checkLegalWrite(this).instance.nextInt();
	}
	
	public int nextInt(int n) {
		return checkLegalWrite(this).instance.nextInt(n);
	}
	
	public long nextLong() {
		return checkLegalWrite(this).instance.nextLong();
	}
	
	public void setSeed(long seed) {
		checkLegalWrite(this).instance.setSeed(seed);
	}

	@Override
	public java.util.Random getUncheckedReadInstance() {
		return checkLegalRead(this).instance;
	}

	@Override
	public java.util.Random getUncheckedWriteInstance() {
		return checkLegalWrite(this).instance;
	}
}
