package l11.example1.client;

import java.rmi.Naming;

import l11.example1.server.AccountBean;

public class Client {
	public static void main(String args[]) throws Exception {
		AccountBean obj = (AccountBean) Naming
				.lookup("//localhost/Account");
		
		System.out.println("===============In Client===============");
		System.out.println("call getAccountName()");
		System.out.println("AccountName: " + obj.getAccountName());
		
		System.out.println("===============In Client===============");
		System.out.println("call getBalance()");
		System.out.println("Balance: " + obj.getBalance());
		
		System.out.println("===============In Client===============");
		System.out.println("call setAccountName()");
		obj.setAccountName("Money-Maker");
		
		System.out.println("===============In Client===============");
		System.out.println("call setBalance()");
		obj.setBalance(10000000000.00);	
		
		System.out.println("===============In Client===============");
		System.out.println("call getAccountInfo()");
		System.out.println(obj.getAccountInfo());
	}
}