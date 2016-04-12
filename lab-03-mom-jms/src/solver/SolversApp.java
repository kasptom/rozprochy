package solver;

public class SolversApp {

	public static void main(String[] args) {
		Solvers solvers = null;
		int solversNumber;
		try {
			if (args.length == 1){/* user specified his solvers number */
				solversNumber = Integer.parseInt(args[0]);
				solvers = new Solvers(solversNumber);
				solvers.start();
			}
			else
				System.out.println("Usage: java SolversApp <clients number>");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (solvers != null) {
				solvers.stop();
			}
		}
	}
}
