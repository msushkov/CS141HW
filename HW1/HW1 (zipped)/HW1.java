
/*
 * Runs the simulation.
 */
public class HW1 {
	
	// specifies how many clients server will create
	private static final int numClients = 10;
	
	public static void main(String[] args) {
		// create server thread
		System.out.println("Creating server thread.");
		Thread s = new Thread(new Server(numClients));
		s.start();
	}
}
