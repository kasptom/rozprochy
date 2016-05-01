package sr.vectors.server;

import Ice.Identity;
import sr.ice.vectors.impl.VectOpsI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
	int status;
	Ice.Communicator communicator = null;
	private static final Logger logg= Logger.getLogger("SERVER");
	private static final String LOGG_FILE_PATH = "log/logs.txt";
	private static final String DB_PATH = "db/account";
	private static final String PERSIST_PATH = "persist/";
	private static final long N = getN();
	
	public Server(){
		try {
			//prepare file structure
			//... for logging
			boolean append = true;
			File loggFile = new File(LOGG_FILE_PATH);
			if(!loggFile.exists()){
				loggFile.getParentFile().mkdir();
				loggFile.createNewFile();
			}
			FileHandler fh = new FileHandler(LOGG_FILE_PATH, append); 
			logg.addHandler(fh);
			fh.setFormatter(new SimpleFormatter());
			logg.setUseParentHandlers(false);
			//... for k1 category objects state
			File dbFile = new File(DB_PATH);
			if(!dbFile.exists()){
				dbFile.getParentFile().mkdir();
				dbFile.createNewFile();
				logg.info("created db file structure for k1 category");
			}
			//... for k5 servants state
			File persistFile = new File(PERSIST_PATH);
			if(!persistFile.exists()){
				persistFile.mkdir();
				logg.info("created persistance directory");
			}
			
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start(String[] args){
		try{
			//1. inicjalizacja ICE
			communicator = Ice.Util.initialize(args);
			//2. Konfiguroawanie adaptera
			//Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter1","tcp -h 127.0.0.1 -p 10000:udp -h hostname -p 10000");
			Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Adapter"); 
			//3. tworzenie serwanta (serwantow)
			////VecOpsI vectorServant1 = new VecOpsI("K1-SERVANT", "k1-states");
			
			
			//4. dodanie wpisow do ASM
			/*
			 * dla obiekt�w z kategorii K1 pierwsze odwo�anie do obiektu powinno skutkowa� utworzeniem
			 * dedykowanego serwanta zainicjowanego poprzednim stanem obiektu odczytanym z
			 * zewn�trznego repozytorium (np. bazy danych, pliku) i dodaniem skojarzenia obiekt-serwant do
			 * tablicy ASM; kolejne wywo�ania do tego obiektu powinien obs�ugiwa� ten sam serwant
			 */
			Ice.ServantLocator k1ServantLocator = new K1ServantLocator();
			adapter.addServantLocator(k1ServantLocator, "k1");
			/*
			 * dla obiekt�w z kategorii K2 ka�de odwo�anie powinno skutkowa� utworzeniem nowego
			 * serwanta jedynie na potrzeb� obs�ugi tego pojedynczego wywo�ania.
			 */
			Ice.ServantLocator k2ServantLocator = new K2ServantLocator();
			adapter.addServantLocator(k2ServantLocator, "k2");
	
			/*
			 * 	dla obiekt�w z kategorii K3 wywo�ania powinny by� dzielone pomi�dzy pul� N serwant�w
			 *	wskazywanych na potrzeb� tego wywo�ania zgodnie z jakim� algorytmem (np. LRU). Liczba
			 *	serwant�w jest sta�a (N), tzn. nie zmienia si� w trakcie dzia�ania aplikacji. Nie zak�adamy
			 *	wi�zania danego obiektu z danym serwantem, to raczej takie workery.
			 */
			HashMap<Integer, Ice.Object> idsMap = new HashMap< Integer, Ice.Object>();
			logg.info("==========creating sevants of 3rd category========");
			for(int i=0; i<Server.N; i++){
				idsMap.put(i, new VectOpsI());
				adapter.add(idsMap.get(i), new Identity("k3", "serv"+i));
				logg.info("created servant: #"+i+" of k3 category");
			}
			
			Ice.ServantLocator k3ServantLocator = new K3ServantLocator();
			adapter.addServantLocator(k3ServantLocator, "k3");
			
			/*
			 * dla wszystkich obiekt�w z kategorii K4 powinien by� u�ywany zawsze ten sam serwant
			 */
			adapter.addDefaultServant(new VectOpsI("DEF_SERV#K4"), "k4");
			
			/*
			 * 	dla obiekt�w z kategorii K5 powinna by� utrzymywana pula N dedykowanych serwant�w
			 *	pozostaj�cych w pami�ci operacyjnej, w razie potrzeby stworzenia dodatkowego serwanta stan
			 *	kt�rego� innego (wskazanego przez algorytm LRU) musi by� przed usuni�ciem utrwalony w
			 *	zewn�trznym repozytorium (wskaz�wka, skorzystaj z implementacji ewiktora) W procesie
			 *	inkarnacji OCZYWI�CIE nale�y odczytywa� zapisany wcze�niej stan.
			 */
			Ice.ServantLocator k5Evictor = new K5Evictor();
			adapter.addServantLocator(k5Evictor, "k5");
			 // 5. Aktywacja adaptera i przej�cie w p�tl� przetwarzania ��da�
			adapter.activate();
			System.out.println("Entering event processing loop...");
			logg.info("=======================SERVER ON====================");
			communicator.waitForShutdown();
		}
		catch (Exception e)
		{
			System.err.println(e);
			status = 1;
		}
		if (communicator != null)
		{
			// Clean up
			try
			{
				communicator.destroy();
			}
			catch (Exception e)
			{
				System.err.println(e);
				status = 1;
			}
		}
		System.exit(status);
	}


	public static void main(String[] args)
	{
		Server app = new Server();
		app.start(args);
	}
	
	public static Logger getLogger(){
		return logg;
	}
	public static String getDBPath(){
		return DB_PATH;
	}
	
	public static String getPersistPath(){
		return PERSIST_PATH;
	}
	
	public static long getN(){
		long N = 0;
		try {
			BufferedReader br = new BufferedReader( new FileReader("config/config.vectors"));
			String line = null;
			while((line = br.readLine()) != null){
				if(line.startsWith("N")){
					N = Long.parseLong(line.split("=")[1].trim());
				}
			}
			br.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		return N;
	}
}
