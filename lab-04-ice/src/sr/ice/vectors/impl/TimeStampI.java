package sr.ice.vectors.impl;

import java.sql.Timestamp;
import java.util.Date;

import Ice.Current;
import Vectors._TimeStampDisp;

public class TimeStampI extends _TimeStampDisp{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Date date = new Date();
	private String timeStamp;
	
	@Override
	public void setTimeStamp(Current __current) {
		timeStamp = (new Timestamp(date.getTime())).toString();
	}
	
	@Override
	public String getTimeStamp(Current __current) {
		return timeStamp;
	}
}
