package l11.example1.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AccountBeanImp extends UnicastRemoteObject implements
		AccountBean {
	private static final long serialVersionUID = 7070658986001512037L;

	private String accountName = "Warrenn Buffett";
	private double balance = 10000;
	

	public AccountBeanImp() throws RemoteException {
		super(0);
	}

	public String getAccountName() throws RemoteException {
		System.out.println("===============In AccountBean===============");
		System.out.println("call getAccountName()");
		
		return this.accountName;
	}

	public void setAccountName(String accountName) throws RemoteException {
		System.out.println("===============In AccountBean===============");
		System.out.println("call setAccountName()");
		
		this.accountName = accountName;
	}

	public double getBalance() throws RemoteException {
		System.out.println("===============In AccountBean===============");
		System.out.println("call getBalance()");
		
		return this.balance;
	}

	public void setBalance(double balance) throws RemoteException {
		System.out.println("===============In AccountBean===============");
		System.out.println("call setBalance()");
		
		this.balance = balance;
	}

	public String getAccountInfo() throws RemoteException {
		System.out.println("===============In AccountBean===============");
		System.out.println("call getAccountInfo()");
		
		return "AccountName: "+ this.accountName + "\nBalance: " + this.balance;
	}
	
	
}