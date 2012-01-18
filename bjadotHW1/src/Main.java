public class Main {

	private static long nanoStartTime;
	private static double averageEatTime;
	private static double numEatMeasurements;
	private static double averageThinkTime;
	private static double numThinkMeasurements;
	private static double averageHungryTime;
	private static double numHungryMeasurements;

	public static long getSimTime() {
		// Use nanotime for better granularity and convert to milliseconds
		return (System.nanoTime() - nanoStartTime) / 1000000L;
	}

	public static void updateEatTime(long time) {
		averageEatTime = (numEatMeasurements * averageEatTime + (double) time)
				/ (numEatMeasurements + 1);
		numEatMeasurements += 1.0;
	}

	public static void updateHungryTime(long time) {
		averageHungryTime = (numHungryMeasurements * averageHungryTime + (double) time)
				/ (numHungryMeasurements + 1);
		numHungryMeasurements += 1.0;
	}

	public static void updateThinkingTime(long time) {
		averageThinkTime = (numThinkMeasurements * averageThinkTime + (double) time)
				/ (numThinkMeasurements + 1);
		numThinkMeasurements += 1.0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		nanoStartTime = System.nanoTime();
		numHungryMeasurements = numThinkMeasurements = 0.0;
		numEatMeasurements = averageEatTime = 0.0;
		averageThinkTime = averageHungryTime = 0.0;

		int numClients = 100;

		Server s = new Server(numClients);

		Client[] clients = new Client[numClients];

		for (int i = 0; i < clients.length; i++) {
			clients[i] = new Client(10, s);
		}

		Thread t = new Thread(s, "Server");
		t.start();

		for (int i = 0; i < clients.length; i++) {
			new Thread(clients[i], "Client " + i).start();
		}

		try {
			t.join();
		} catch (InterruptedException e) {
			// This should never happen
			e.printStackTrace();
		}

		System.out.println("Average eating time: " + averageEatTime);
		System.out.println("Average thinking time: " + averageThinkTime);
		System.out.println("Average hungry time: " + averageHungryTime);
	}

}
