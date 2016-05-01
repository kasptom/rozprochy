package sr.ice.vectors.impl;

import Ice.Current;
import Vectors.Vector;
import Vectors._VectOpsDisp;

public class VectOpsI extends _VectOpsDisp{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name = null;
	public VectOpsI(){};
	public VectOpsI(String name){
		this(name, null);
	};
	public VectOpsI(String name, String statesDirectoryPath){
		this.name = name;
	}
	

	@Override
	public Vector add(Vector a, Vector b, Current __current) {
		float x = a.x + b.x;
		float y = a.y + b.y;
		float z = a.z + b.z;
		return new Vector(x,y,z);
	}

	@Override
	public Vector sub(Vector a, Vector b, Current __current) {
		float x = a.x - b.x;
		float y = a.y - b.y;
		float z = a.z - b.z;
		return new Vector(x,y,z);
	}

	@Override
	public Vector vmul(Vector a, Vector b, Current __current) {
		float x = a.y * b.z - a.z * b.y;
		float y = a.z * b.x - a.x * b.z;
		float z = a.x * b.y - a.y * b.x;
		return new Vector(x,y,z);
	}

	@Override
	public float smul(Vector a, Vector b, Current __current) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	@Override
	public float norm(Vector a, Current __current) {
		return (float)Math.sqrt(a.x*a.x + a.y*a.y + a.z*a.z);
	}
	@Override
	public String getName(Current __current) {
		return name;
	}
}
