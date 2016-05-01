package sr.vectors.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import Ice.Current;
import Ice.Identity;
import Ice.LocalObjectHolder;
import Ice.Object;
import Ice.UserException;
import Vectors.Stats;
import sr.ice.vectors.impl.StatsManagerI;

public class K1ServantLocator implements Ice.ServantLocator{
	String dbPath = Server.getDBPath();
	Logger logger = Server.getLogger();
	
	@Override
	public Object locate(Current curr, LocalObjectHolder cookie) throws UserException {
		Ice.Object statsServant = curr.adapter.find(curr.id);
		System.out.println("Identity: "+curr.id);
		if(statsServant == null){
			logger.info("no servant found - creating new one");
			System.out.println("no servant found - creating new one");
			String category = curr.id.category;
			String name = curr.id.name; 
			
			//retreiving stats from database
			String userName = curr.id.name;
			Stats stats = getStats(userName);
			if(stats == null){ //stats not found - creating new entry
				logger.info("adding new user: "+userName+" to database");
				addStats(userName);
				stats = new Stats(userName, 0);
			}
			
			//creating servant for stats tracking
			statsServant = new StatsManagerI(userName, stats.operationsCount);
			curr.adapter.add(statsServant, new Identity(name, category));
			
		}
		else{
			logger.info("found servant:" + statsServant.ice_id());
			System.out.println("found servant:");// + servant.ice_id());
		}
		
		
		return statsServant;
	}

	@Override
	public void finished(Current curr, Object servant, java.lang.Object cookie) throws UserException {
		
	}

	@Override
	public void deactivate(String category) {
		logger.info("k1 - deactivating");
		
	}
	/*
	 * retreive stats of user: userName from db 
	 */
	
	private Stats getStats(String userName){
		boolean found = false;
		long operationsCount = 0; 
		Stats stats = null;
		
		BufferedReader br = null;
		
		try{
			br = new BufferedReader(new FileReader(dbPath));
		String line;
		String[] statsStr;
		
		while((line = br.readLine()) != null){
			statsStr = line.split(";");
			if(statsStr[0].trim().equals(userName)){
				operationsCount = Long.parseLong(statsStr[1]); 
				found = true;
				break;
			}
		}
		stats = found ? new Stats(userName, operationsCount) : null;
			
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return stats;
	}
	/*
	 * addStats - adds new user to database
	 * used only if user does not exist
	 */
	private void addStats(String userName){
		boolean append = true;
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileOutputStream( new File(dbPath), append));
			pw.write(userName+";0\n");
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
