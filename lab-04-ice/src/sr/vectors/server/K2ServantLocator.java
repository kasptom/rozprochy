package sr.vectors.server;

import java.util.logging.Logger;

import Ice.Current;
import Ice.LocalObjectHolder;
import Ice.Object;
import Ice.UserException;
import sr.ice.vectors.impl.VectOpsI;

public class K2ServantLocator implements Ice.ServantLocator {
	private Logger logger = Server.getLogger();
	
	@Override
	public Object locate(Current curr, LocalObjectHolder cookie) throws UserException {
		VectOpsI tmpVectOps = new VectOpsI();
		String info = "category: "+ curr.id.category+", name: "+curr.id.name+ ", servant: "+ tmpVectOps.hashCode();
		logger.info(info);
		return tmpVectOps;
	}

	@Override
	public void finished(Current curr, Object servant, java.lang.Object cookie) throws UserException {
		String info = "category: "+ curr.id.category+", name: "+curr.id.name+
				", servant: "+ servant.hashCode() + ", operation: "+ curr.operation ;
		logger.info(info);
	}

	@Override
	public void deactivate(String category) {
		// TODO Auto-generated method stub
		
	}

}
