import java.io.*;
import java.util.Random;
import java.io.Serializable;

public class Account implements Serializable {

	public String bankName = null;
	public String branchName = null;
	public String accountNo = null;
	public String accountHolder = null;
	private int amount = 0;

	public Account(String bankName, String branchName, String name) {


		this.bankName = bankName;
		this.branchName = branchName;

		File f = null;
		Random rnd = new Random();

		do {

			this.accountNo = Integer.toString(rnd.nextInt(999999));
			f = new File(this.accountNo);

		} while (f.exists());

		this.accountHolder = name;
		this.amount = 0;

	}

	public int getBalance() {

		return this.amount;

	}

	public String execGetBalance() {

		return Integer.toString(this.getBalance());

	}

	public int draw(int amt) {

		int max = this.getBalance();

		if (max < amt) {

			this.amount = 0;
			return max;

		} else {

			this.amount -= amt;
			return amt;

		}

	}

	public String execDraw(int amt) {

		return Integer.toString(this.draw(amt));

	}

	public void deposit(int amt) {

		this.amount += amt;

	}

	public String execDeposit(int amt) {

		this.deposit(amt);
		return "OK";

	}

	public String getAccountInfo() {

		String accountInfo = this.bankName + "," +  this.branchName + "," + this.accountNo + "," + this.accountHolder;

		return accountInfo;

	}

}



