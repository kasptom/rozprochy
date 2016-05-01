package sr.vectors.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import Evictor.EvictorBase;
import Ice.Current;
import Ice.Identity;
import Ice.LocalObjectHolder;
import Ice.Object;
import sr.ice.vectors.impl.TimeStampI;

public class K5Evictor extends EvictorBase {
	private String persistPath = Server.getPersistPath();
	private String fileName;
	private Logger logger = Server.getLogger();
	
	@Override
	public Object add(Current c, LocalObjectHolder cookie) {
		cookie.value = new Identity(c.id.name, c.id.category);
		// if we are here it means there is no proper servant in active servant map
		// ... checking file existence
		TimeStampI timeStampI = null;
		fileName = "timestamp_"+c.id.category+"_"+c.id.name;
		File timeStampFile = new File(persistPath+fileName);
		if(!timeStampFile.exists()){
			//creating new servant
			timeStampI = new TimeStampI();
			timeStampI.setTimeStamp();
			c.adapter.add(timeStampI, (Identity)cookie.value);
		}else{
			try {//get previous state of the servant from file
				System.out.println("retreiving state from "+ fileName);
				logger.info("retreiving state from "+ fileName);
				FileInputStream fis = new FileInputStream(persistPath+fileName);
				ObjectInputStream inputStream = new ObjectInputStream(fis);
				timeStampI = (TimeStampI)(inputStream.readObject());
				
				inputStream.close();
				
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return timeStampI;
	}

	@Override
	public void evict(Object servant, java.lang.Object cookie) {
		//persist servant state
		fileName = "timestamp_"+ ((Identity)cookie).category + "_"+((Identity)cookie).name;
		File timeStampFile = new File(persistPath + fileName);
		try {
			if(!timeStampFile.exists()){
				timeStampFile.createNewFile();
			}//save state
		
			FileOutputStream fos = new FileOutputStream(persistPath+fileName);
			ObjectOutputStream outputStream = new ObjectOutputStream(fos);
			outputStream.writeObject(servant);
			
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		System.out.println("servant state saved to: "+ fileName);
		logger.info("servant state saved to: "+ fileName);
		
	}

}
