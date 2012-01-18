public class Main {
	private static long nano_start_time;
	private static double average_eat_time;
	private static double num_eat_measurements;
	private static double average_think_time;
	private static double num_think_measurements;
	private static double average_hungry_time;
	private static double num_hungry_measurements;

	public static long getSimTime() {
		// Use nanotime for better granularity and convert to milliseconds
		return (System.nanoTime() - nano_start_time) / 1000000L;
	}

	public static void updateEatTime(long time) {
		average_eat_time = (num_eat_measurements * average_eat_time + (double) time)
				/ (num_eat_measurements + 1);
		num_eat_measurements += 1.0;
	}

	public static void updateHungryTime(long time) {
		average_hungry_time = (num_hungry_measurements * average_hungry_time + (double) time)
				/ (num_hungry_measurements + 1);
		num_hungry_measurements += 1.0;
	}

	public static void updateThinkingTime(long time) {
		average_think_time = (num_think_measurements * average_think_time + (double) time)
				/ (num_think_measurements + 1);
		num_think_measurements += 1.0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		nano_start_time = System.nanoTime();
		num_hungry_measurements = num_think_measurements = num_eat_measurements = average_eat_time = average_think_time = average_hungry_time = 0.0;
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
		
		System.out.println("Average eating time: " + average_eat_time);
		System.out.println("Average thinking time: " + average_think_time);
		System.out.println("Average hungry time: " + average_hungry_time);
	}

}
