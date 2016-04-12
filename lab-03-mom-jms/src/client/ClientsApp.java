package client;


public class ClientsApp {
	public static void main(String[] args) {
		Clients clients = null;
		int clientsNumber;
		try {
			if (args.length == 1){/* user specified his clients number */
				clientsNumber = Integer.parseInt(args[0]);
				clients = new Clients(clientsNumber);
				clients.start();
			}
			else
				System.out.println("Usage: java ClientsApp <clients number>");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (clients != null) {
				clients.stop();
			}
		}
	}
}
