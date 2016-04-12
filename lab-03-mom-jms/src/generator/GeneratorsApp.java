package generator;


public class GeneratorsApp {
	public static void main(String[] args) {
		Generators generators = null;
		int generatorsNumber;
		try {
			if (args.length == 1){/* user specified his generators number */
				
				generatorsNumber = Integer.parseInt(args[0]);
				generators = new Generators(generatorsNumber);
				generators.start();
			}
			else
				System.out.println("Usage: java ClientsApp <clients number>");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (generators != null) {
				generators.stop();
			}
		}
	}
}
