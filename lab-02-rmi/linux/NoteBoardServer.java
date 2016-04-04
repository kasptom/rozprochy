import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;

public class NoteBoardServer {
	static NoteBoardImpl nbi;

	public static void main(String[] args) {
		try {
			if(args.length != 2){
				System.out.println("run server with <IP_ADDRESS> <PORT_NUMBER>");
				return;
			}
			String ipStrAddress = args[0];
			int portNumber = Integer.parseInt(args[1]);
			
			nbi = new NoteBoardImpl(ipStrAddress, portNumber);
			INoteBoard noteBoard = (INoteBoard) UnicastRemoteObject
													.exportObject(nbi,0);
			
			
													
			Naming.rebind("rmi://"+ipStrAddress+":"+portNumber+"/note", noteBoard);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
