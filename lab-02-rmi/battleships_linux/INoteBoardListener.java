import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INoteBoardListener extends Remote {	
	public void onNewText(String text) throws RemoteException;
	public char onOpponentsShot(int x, int y) throws RemoteException;
	public void onOpponentsQuit() throws RemoteException;
	public void onOpponentsJoin(String oppNick) throws RemoteException;
}
