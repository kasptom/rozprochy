import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;

/*public interface INoteBoard extends Remote{
	public void appendText( String newNote ) throws RemoteException;
	public void clean() throws RemoteException;
}*/
public interface INoteBoard extends Remote {
	public String getText() throws RemoteException;
	public void appendText(String nick, String text)
		throws RemoteException, UserRejectedException, NotBoundException, MalformedURLException;
	public void clean(String nick)
		throws RemoteException, UserRejectedException;
	public void register( IUser u, INoteBoardListener l )
		throws RemoteException, UserRejectedException, MalformedURLException, NotBoundException;
	public void unregister(IUser u) throws RemoteException, NotBoundException, MalformedURLException;
	public char takeShot(String nick, int x, int y)
			throws RemoteException, UserRejectedException, NotBoundException, MalformedURLException;

}
