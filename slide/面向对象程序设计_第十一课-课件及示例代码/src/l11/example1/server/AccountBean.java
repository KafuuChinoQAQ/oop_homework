package l11.example1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AccountBean extends Remote {
	
	public String getAccountName() throws RemoteException;
	public void setAccountName(String accountName) throws RemoteException;
	
	public double getBalance() throws RemoteException;
	public void setBalance(double balance) throws RemoteException;
	
	public String getAccountInfo() throws RemoteException;
}