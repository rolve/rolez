package classes;

import rolez.annotation.Checked;

@Checked
public class Account {
	
	double balance;
	
	public void deposit(double amount) {
		this.balance += amount;
	}
	
	public void withdraw(double amount) {
		this.balance -= amount;
	}
	
	public double getBalance() {
		return this.balance;
	}
	
}