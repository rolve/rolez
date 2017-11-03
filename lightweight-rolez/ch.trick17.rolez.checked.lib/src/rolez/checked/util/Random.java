package rolez.checked.util;

import rolez.checked.lang.annotation.Write;

public class Random extends WrapperType<java.util.Random> {
	
	public Random() {
		instance = new java.util.Random();
	}
	
	public Random(long seed) {
		instance = new java.util.Random(seed);
	}
	
	@Write
	public boolean nextBoolean() {
		return this.instance.nextBoolean();
	}

	@Write
	public void nextBytes(byte[] bytes) {
		this.instance.nextBytes(bytes);
	}

	@Write
	public double nextDouble() {
		return this.instance.nextDouble();
	}

	@Write
	public float nextFloat() {
		return this.instance.nextFloat();
	}

	@Write
	public double nextGaussian() {
		return this.instance.nextGaussian();
	}

	@Write
	public int nextInt() {
		return this.instance.nextInt();
	}

	@Write
	public int nextInt(int n) {
		return this.instance.nextInt(n);
	}

	@Write
	public long nextLong() {
		return this.instance.nextLong();
	}

	@Write
	public void setSeed(long seed) {
		this.instance.setSeed(seed);
	}
}
