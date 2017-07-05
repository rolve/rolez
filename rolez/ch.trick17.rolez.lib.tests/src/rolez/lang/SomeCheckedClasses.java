package rolez.lang;

import rolez.checked.lang.Checked;

public class SomeCheckedClasses {

	public static class A extends Checked {
		
		public int value;
		
		public A() {
			this.value = 0;
		}
		
		public int getValue() {
			return this.value;
		}
		
		public void setValue(int value) {
			this.value = value;
		}
	}

	public static class B extends Checked {
		public A a;
		
		public B() {
			this.a = new A();
		}
	}
}
