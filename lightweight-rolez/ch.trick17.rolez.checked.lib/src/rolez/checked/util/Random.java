package rolez.checked.util;

public class Random extends WrapperType<java.util.Random> {
	
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
}
