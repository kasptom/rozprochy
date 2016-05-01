
#ifndef SR_VEC_ICE
#define SR_VEC_ICE

#include <Ice/BuiltinSequences.ice> 

module Vectors
{
	exception RequestCanceledException
	{
	};
	
	struct Vector{
		float x;
		float y;
		float z;
	};

	interface VectOps
	{
		Vector add(Vector a, Vector b);
		Vector sub(Vector a, Vector b);
		Vector vmul(Vector a, Vector b);
		float smul(Vector a, Vector b);
		float norm(Vector a);
		string getName();
	};
	
	
	struct Stats
	{
		string userName;
		long operationsCount;
	};
	
	interface StatsManager
	{
		string getUserName();
		long getOperationsCount();
		void incrementOperationsCount();
		void saveState();
	};
	
	interface TimeStamp{
		void setTimeStamp();
		string getTimeStamp();
	};

};

#endif
