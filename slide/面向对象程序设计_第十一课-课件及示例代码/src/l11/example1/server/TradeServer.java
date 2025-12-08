package l11.example1.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class TradeServer {
	public static void main(String args[]) throws Exception {
		System.out.println("Trade server started");

		try { // special exception handler for registry creation
			LocateRegistry.createRegistry(1099);
			System.out.println("java RMI registry created.");
		} catch (RemoteException e) {
			// do nothing, error means registry already exists
			System.out.println("java RMI registry already exists.");
		}

		// Instantiate RmiServer
		AccountBean obj = new AccountBeanImp();

		// Bind this object instance to the name "RmiServer"
		Naming.rebind("//localhost/Account", obj);
		System.out.println("AccountBean bound in registry");
	}
}
