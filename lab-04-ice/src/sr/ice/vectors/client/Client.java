package sr.ice.vectors.client;

import Vectors.StatsManagerPrx;
import Vectors.StatsManagerPrxHelper;
import Vectors.TimeStampPrx;
import Vectors.TimeStampPrxHelper;
//import sr.ice.client.Callback_Calc_add1I;
import Vectors.VectOpsPrx;
import Vectors.VectOpsPrxHelper;
import Vectors.Vector;

public class Client {
	
	public static void main(String[] args){
		int status = 0;
		Ice.Communicator communicator = null;

		try {
			// 1. Inicjalizacja ICE
			communicator = Ice.Util.initialize(args);

			// 2. Uzyskanie referencji obiektu na podstawie linii w pliku konfiguracyjnym
			Ice.ObjectPrx base1 = communicator.propertyToProxy("Stats1.Proxy");
			Ice.ObjectPrx base2 = communicator.propertyToProxy("Vectors2.Proxy");
			Ice.ObjectPrx base3 = communicator.propertyToProxy("Vectors3.Proxy");
			Ice.ObjectPrx base4 = communicator.propertyToProxy("Vectors4.Proxy");
			Ice.ObjectPrx base5 = communicator.propertyToProxy("Timestamp5.Proxy");
			// 2. To samo co powy¿ej, ale mniej ³adnie
			//Ice.ObjectPrx base1 = communicator.stringToProxy("k1/ania:tcp -h 127.0.0.1 -p 10000");//:udp -h localhost -p 10000:ssl -h localhost -p 10001");

			// 3. Rzutowanie, zawê¿anie
			//VectOpsPrx calc1 = VectOpsPrxHelper.checkedCast(base1);
			StatsManagerPrx stats1 = StatsManagerPrxHelper.checkedCast(base1);
			VectOpsPrx vectOps2 = VectOpsPrxHelper.checkedCast(base2);
			VectOpsPrx vectOps3 = VectOpsPrxHelper.checkedCast(base3);
			VectOpsPrx vectOps4 = VectOpsPrxHelper.checkedCast(base4);
			TimeStampPrx timestamp5 = TimeStampPrxHelper.checkedCast(base5);
			if (stats1 == null) throw new Error("Invalid proxy [stats#1]");
			if (vectOps2 == null) throw new Error("Invalid proxy [vectors#2]");
			if (vectOps3 == null) throw new Error("Invalid proxy [vectors#3]");
			if (vectOps4 == null) throw new Error("Invalid proxy [vectors#4]");
			if (timestamp5 == null) throw new Error("Invalid proxy [timestamp#5]");
			
			// 4. Wywolanie zdalnych operacji

			String line = null;
			java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
			
			Vector a = new Vector(-1,3,5);
			Vector b = new Vector(2,4,-2);
						
			do
			{
				try
				{
					System.out.print("==> ");
					System.out.flush();
					line = in.readLine();

					if (line == null)
					{
						break;
					}
					if(line.equals("add")){
						Vector c = vectOps2.add(a,b);
						System.out.println(vectorToString(a) +" + "
						+ vectorToString(b) + " = " + vectorToString(c) );
					}
					if(line.equals("sub")){
						Vector c = vectOps2.sub(a,b);
						System.out.println(vectorToString(a) +" - "
						+ vectorToString(b) + " = " + vectorToString(c) );
					}
					if(line.equals("vmul")){
						Vector c = vectOps3.vmul(a,b);
						System.out.println(vectorToString(a) +" x "
						+ vectorToString(b) + " = " + vectorToString(c) );
					}
					if(line.equals("smul")){
						float c = vectOps3.smul(a,b);
						System.out.println(vectorToString(a) +" * "
						+ vectorToString(b) + " = " + c );
					}
					if(line.equals("norm")){
						float norm = vectOps4.norm(a);
						System.out.println("||"+vectorToString(a)+"|| = "+norm);
					}
					if(line.equals("stats")){
						long count = stats1.getOperationsCount();
						System.out.println("operations made: "+ count);
					}
					if(line.equals("incr")){
						System.out.println("operations incremented");
						stats1.incrementOperationsCount();
					}
					if(line.equals("time")){
						System.out.println("saved timestamp: "+ timestamp5.getTimeStamp());
					}
					if(line.equals("save")){
						System.out.println("operations saved");
						stats1.saveState();
					}
					if(line.equals("help")){
						System.out.println("available operations:");
						System.out.println("vectors: add, sub, vmul, smul, norm");
						System.out.println("stats: incr, save");
						System.out.println("timestamp: time");
					}
					else if (line.equals("x"))
					{
						stats1.saveState();
					}
				}
				catch (java.io.IOException ex)
				{
					System.err.println(ex);
				}
			}
			while (!line.equals("x"));


		} catch (Ice.LocalException e) {
			e.printStackTrace();
			status = 1;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			status = 1;
		}
		if (communicator != null) {
			// Clean up
			//
			try {
				communicator.destroy();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				status = 1;
			}
		}
		System.exit(status);
	}
	
	private static String vectorToString(Vector a){
	    return "["+a.x+", "+a.y+", "+a.z+"]";
	}
}
