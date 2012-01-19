public class Main {

	private static long startTime;
	private static double avgEatT;
	private static double numEats;
	private static double avgThinkT;
	private static double numThinks;
	private static double avgHungryT;
	private static double numHungrys;

	public static long getSimTime() {
		// Use nanotime for better granularity and convert to milliseconds
		return (System.nanoTime() - startTime) / 1000000L;
	}

	// My averages update as you go along
	public static void updateEatTime(long time) {
		avgEatT = (numEats * avgEatT + (double) time)
				/ (numEats + 1);
		numEats += 1.0;
	}

	public static void updateHungryTime(long time) {
		avgHungryT = (numHungrys * avgHungryT + (double) time)
				/ (numHungrys + 1);
		numHungrys += 1.0;
	}

	public static void updateThinkingTime(long time) {
		avgThinkT = (numThinks * avgThinkT + (double) time)
				/ (numThinks + 1);
		numThinks += 1.0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		startTime = System.nanoTime();
		numHungrys = numThinks = 0.0;
		numEats = avgEatT = 0.0;
		avgThinkT = avgHungryT = 0.0;

		int numClients = 100;

		// Set up the server
		Server s = new Server(numClients);
		Thread t = new Thread(s, "Server");
		t.start();
		
		// Start the clients
		Client[] clients = new Client[numClients];

		for (int i = 0; i < clients.length; i++) {
			clients[i] = new Client(10, s);
			new Thread(clients[i], "Client " + i).start();
		}

		// Wait for the server (and thus the clients) to stop running
		try {
			t.join();
		} catch (InterruptedException e) {
			// This should never happen
			e.printStackTrace();
		}

		// Print results
		System.out.println("Average eating time: " + avgEatT);
		System.out.println("Average thinking time: " + avgThinkT);
		System.out.println("Average hungry time: " + avgHungryT);
	}

}
