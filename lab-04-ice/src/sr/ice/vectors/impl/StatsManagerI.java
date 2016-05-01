package sr.ice.vectors.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import Ice.Current;
import Vectors._StatsManagerDisp;
import sr.vectors.server.Server;

public class StatsManagerI extends _StatsManagerDisp{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userName;
	private long operationsCount;
	
	public StatsManagerI(String userName, long operationsCount){
		this.userName = userName;
		this.operationsCount = operationsCount;
	}
	
	@Override
	public String getUserName(Current __current) {
		return userName;
	}

	@Override
	public long getOperationsCount(Current __current) {
		return operationsCount;
	}

	@Override
	public void incrementOperationsCount(Current __current) {
		operationsCount++;
	}

	@Override
	public void saveState(Current __current) {
		String dbPath = Server.getDBPath();
		BufferedReader br = null;
		PrintWriter pw = null;
		try{
			br = new BufferedReader(new FileReader(dbPath));
			String line;
			StringBuffer lines = new StringBuffer();
			while( (line = br.readLine()) != null){ 
				if(line.startsWith(userName)){
					lines.append(userName+";"+operationsCount+"\n");					
				}else{
					lines.append(line+"\n");					
				}
			}
			pw = new PrintWriter(new PrintWriter(new FileWriter(dbPath)));
			pw.write(lines.toString());			
		}catch(IOException e){
			e.printStackTrace();			
		}finally{
			try {
				if(br != null)
					br.close();					
				if(pw != null)
					pw.close();
			} catch (IOException e) {
				e.printStackTrace();				
			}
		}		
	}

}
