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
			 * dla obiektów z kategorii K1 pierwsze odwo³anie do obiektu powinno skutkowaæ utworzeniem
			 * dedykowanego serwanta zainicjowanego poprzednim stanem obiektu odczytanym z
			 * zewnêtrznego repozytorium (np. bazy danych, pliku) i dodaniem skojarzenia obiekt-serwant do
			 * tablicy ASM; kolejne wywo³ania do tego obiektu powinien obs³ugiwaæ ten sam serwant
			 */
			Ice.ServantLocator k1ServantLocator = new K1ServantLocator();
			adapter.addServantLocator(k1ServantLocator, "k1");
			/*
			 * dla obiektów z kategorii K2 ka¿de odwo³anie powinno skutkowaæ utworzeniem nowego
			 * serwanta jedynie na potrzebê obs³ugi tego pojedynczego wywo³ania.
			 */
			Ice.ServantLocator k2ServantLocator = new K2ServantLocator();
			adapter.addServantLocator(k2ServantLocator, "k2");
	
			/*
			 * 	dla obiektów z kategorii K3 wywo³ania powinny byæ dzielone pomiêdzy pulê N serwantów
			 *	wskazywanych na potrzebê tego wywo³ania zgodnie z jakimœ algorytmem (np. LRU). Liczba
			 *	serwantów jest sta³a (N), tzn. nie zmienia siê w trakcie dzia³ania aplikacji. Nie zak³adamy
			 *	wi¹zania danego obiektu z danym serwantem, to raczej takie workery.
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
			 * dla wszystkich obiektów z kategorii K4 powinien byæ u¿ywany zawsze ten sam serwant
			 */
			adapter.addDefaultServant(new VectOpsI("DEF_SERV#K4"), "k4");
			
			/*
			 * 	dla obiektów z kategorii K5 powinna byæ utrzymywana pula N dedykowanych serwantów
			 *	pozostaj¹cych w pamiêci operacyjnej, w razie potrzeby stworzenia dodatkowego serwanta stan
			 *	któregoœ innego (wskazanego przez algorytm LRU) musi byæ przed usuniêciem utrwalony w
			 *	zewnêtrznym repozytorium (wskazówka, skorzystaj z implementacji ewiktora) W procesie
			 *	inkarnacji OCZYWIŒCIE nale¿y odczytywaæ zapisany wczeœniej stan.
			 */
			Ice.ServantLocator k5Evictor = new K5Evictor();
			adapter.addServantLocator(k5Evictor, "k5");
			 // 5. Aktywacja adaptera i przejœcie w pêtlê przetwarzania ¿¹dañ
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
