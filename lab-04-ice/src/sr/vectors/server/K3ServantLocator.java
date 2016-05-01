package sr.vectors.server;

import java.util.logging.Logger;

import Ice.Current;
import Ice.Identity;
import Ice.LocalObjectHolder;
import Ice.Object;
import Ice.UserException;

public class K3ServantLocator implements Ice.ServantLocator {
	private static long N = Server.getN();
	private static int prev = -1;
	private static Logger logger = Server.getLogger();

	
	
	@Override
	public Object locate(Current curr, LocalObjectHolder cookie) throws UserException {
		prev = (int)((prev + 1)%N);
		return curr.adapter.find(new Identity("k3", "serv"+prev));
	}

	@Override
	public void finished(Current curr, Object servant, java.lang.Object cookie) throws UserException {
		logger.info("servant: "+servant.hashCode()+" of k3 category, operation:" + curr.operation);
	}

	@Override
	public void deactivate(String category) {
		// TODO Auto-generated method stub
		
	}

}
