package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;
import rolez.annotation.Readonly;

@Checked
public class TestAccountExample {

	private double interestRate = 0.015;
	
	public static void main(String[] args) {
		TestAccountExample instance = new TestAccountExample();
		Account acc = new Account();
		acc.deposit(1000);
		instance.payInterest(acc, true);
		System.out.println(acc.getBalance());
	}
	
	double calcInterest(double balance) {
		return interestRate * balance;
	}
	
	@Task
	@Readonly
	void payInterest(@Readwrite Account acc, boolean $asTask) {
		double interest = calcInterest(acc.getBalance());
		acc.deposit(interest);
	}
}
