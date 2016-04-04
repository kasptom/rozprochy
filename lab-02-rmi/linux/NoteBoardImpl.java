import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.HashSet;
import java.rmi.Naming;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NoteBoardImpl implements INoteBoard {
	private StringBuffer buf;
	private ConcurrentHashMap<String, Boolean> users;		//<Nick, isPlayingWithCPU>
	private ConcurrentLinkedQueue<String> realPlayers;
	private ConcurrentHashMap<String, String> opps;			//<Nick, oppNick>
	private ConcurrentHashMap<String, BotCPU> bots;			//<Nick, oppNick>
	private String ipAddress;
	private int portNumber;

	public NoteBoardImpl(String ipAddress, int portNumber) {
		buf = new StringBuffer();
		users = new ConcurrentHashMap<String, Boolean>();
		realPlayers = new ConcurrentLinkedQueue<String>();
		opps = new ConcurrentHashMap<String, String>();
		bots = new ConcurrentHashMap<String, BotCPU>();

		this.ipAddress = ipAddress;
		this.portNumber = portNumber;
	}
	public String getText() throws RemoteException{
		return buf.toString();
	}
	public void appendText(String nick, String text)
		throws RemoteException, UserRejectedException, NotBoundException, MalformedURLException
	{
		buf.append("\n"+text);
		for(String usr : users.keySet()){
			if(!usr.equals(nick)){
				Object o = Naming.lookup("rmi://"+ipAddress+":"+String.valueOf(portNumber)+"/"+ usr);
				INoteBoardListener nbl = (INoteBoardListener)o;
				nbl.onNewText("["+nick+"]: "+text);
			}
		}
		return;	
	}

	public char takeShot(String nick, int x, int y)
			throws RemoteException, UserRejectedException, NotBoundException, MalformedURLException
	{
		//System.out.println(nick + "  is taking a shot");
		INoteBoardListener nbl = null;
		if(users.get(nick)){
			return bots.get(nick).onOpponentsShot(x,y);
		}else{
			if(realPlayers.contains(nick))
				return '?';
			else{
				String oppNick = opps.get(nick);
				Object o = Naming.lookup("rmi://"+ipAddress+":"+String.valueOf(portNumber)+"/"+ oppNick);
				nbl = (INoteBoardListener)o;
			}
		}
		/*for(String usr : users){
			//System.out.println(usr);
			if(!usr.equals(nick)){
				Object o = Naming.lookup("rmi://"+ipAddress+":"+String.valueOf(portNumber)+"/"+ usr);
				nbl = (INoteBoardListener)o;
			}
		}*/
		char field = '?';
		if(nbl != null) {
            field = nbl.onOpponentsShot(x, y);
        }
		//System.out.println("FIELD VAL: "+field);
		return field;
	}

	public void clean(String nick) 
		throws RemoteException, UserRejectedException
	{
		buf = new StringBuffer();
	}
	public void register( IUser u, INoteBoardListener l )
		throws RemoteException, UserRejectedException, MalformedURLException, NotBoundException
	{
		if(users.containsKey(u.getNick()))
			throw new UserRejectedException();
		//cpu player
		users.put(u.getNick(), u.playWithCPU());
		//real player
		if(!u.playWithCPU() && realPlayers.isEmpty())
			realPlayers.add(u.getNick());
		else if(!u.playWithCPU()){
			String oppNick = realPlayers.poll();
			opps.put(u.getNick(), oppNick);
			opps.put(oppNick, u.getNick());
			Object o = Naming.lookup("rmi://"+ipAddress+":"+String.valueOf(portNumber)+"/"+ oppNick);
			INoteBoardListener nbl = (INoteBoardListener)o;
			nbl.onOpponentsJoin(u.getNick());

			Naming.rebind("rmi://"+ipAddress+":"+String.valueOf(portNumber)+"/"+u.getNick(), l);
			System.out.println("User "+ u.getNick()+" has joined");
			o = Naming.lookup("rmi://"+ipAddress+":"+String.valueOf(portNumber)+"/"+ u.getNick());
			nbl = (INoteBoardListener)o;
			nbl.onOpponentsJoin(oppNick);

			return;

		}

		/*Naming.rebind("rmi://"+ipAddress+":"+String.valueOf(portNumber)+"/"+u.getNick(), l);
		System.out.println("User "+ u.getNick()+" has joined");

		if(!users.add(u.getNick()))
				throw new UserRejectedException();*/
		//[OK] unique nick
		Naming.rebind("rmi://"+ipAddress+":"+String.valueOf(portNumber)+"/"+u.getNick(), l);
		System.out.println("User "+ u.getNick()+" has joined");
		if(u.playWithCPU()) {
			System.out.println("registering bot for " + u.getNick());
			bots.put(u.getNick(), new BotCPU(l));
			bots.get(u.getNick()).start();
		}
		return; 
	}
	public void unregister(IUser u) throws RemoteException, NotBoundException, MalformedURLException{
		String usrNick = u.getNick();
		System.out.println("User "+ usrNick +" is unregistering...");
		String oppName = opps.get(usrNick);

		if(users.get(usrNick).booleanValue()) {//CPU player
				bots.get(usrNick).interrupt();
			bots.remove(usrNick);
			System.out.println("CPU player quits");
			users.remove(usrNick);
			System.out.println("User "+usrNick+" has quit.");
			Naming.unbind(usrNick);
		}else{
			System.out.println("real player quits");
			if(realPlayers.remove(usrNick)){// not CPU but nobody played with him
				System.out.println("Noone to play with :(");
				users.remove(usrNick);
				System.out.println("User "+usrNick+" has quit.");
				Naming.unbind(usrNick);
			}else{//has been playing with someone
				System.out.println(" ...and has been playing with someone");
				System.out.println("MY OPPONENT WAS "+ oppName);
				users.remove(usrNick);
				users.remove(oppName);
				opps.remove(usrNick);
				if(opps.remove(oppName, usrNick)){
					//inform opponent about end of the game
					Object o = Naming.lookup("rmi://"+ipAddress+":"+String.valueOf(portNumber)+"/"+ oppName);
					INoteBoardListener nbl = (INoteBoardListener)o;
					nbl.onNewText("[SERVER]: your opponent (" + usrNick+") has left");
					try {
						Thread.sleep(100);
					}catch (InterruptedException e){

					}
					Naming.unbind(usrNick);
					Naming.unbind(oppName);
					nbl.onOpponentsQuit();
				}
			}
		}
	}
}	
