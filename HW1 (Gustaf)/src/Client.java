import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class Client implements Runnable {
	
	private int id;
	Server controller;
	Random rand = new Random();
	int minH = 195; 	// min time to be thinking
	int diffH = 10; 	// max difference
	
	int minE = 15;      // min time to be eating
	int diffE = 10;     // max difference
	int M = 10;   		// Number of times client wants to "eat"
	long[] hungryTimes = new long[M];
	
	
	private ArrayBlockingQueue<Integer> token = new ArrayBlockingQueue<Integer>(1);

	
	
	public void run() {
		boolean done = false; // set when the client has eaten M times
		
		for (int i = 0; i < M; i++) {
			try {
				// Thinking
				int thinkFor = minH + rand.nextInt(diffH);
				Thread.sleep(thinkFor);
				
				// Hungry
				controller.request(id);
				long hungerStart = System.nanoTime();
				//System.out.print(System.nanoTime());
				//System.out.println("requested and am " + id);
				int value = token.take();
				long hungerEnd = System.nanoTime();
				
				// Eating
				int eatFor = minE + rand.nextInt(diffE);
				//System.out.println("eating and am " + id);
				Thread.sleep(eatFor);
				
				// Last run?
				if (i == M - 1) {
					done = true;
				}
				controller.getToken(value, done);
				hungryTimes[i] = hungerEnd - hungerStart;

			}
			catch (InterruptedException e) {
				System.out.println("Interrupted!");
			}
			
		}
		//System.out.println("done and am " + id);
		long totalHTime = 0;
		for (int i=0; i < M; i++) {
			totalHTime = totalHTime + hungryTimes[i];
		}
		long average = totalHTime / M;
		controller.getAverageHunger(id, average);
		//System.out.println("Average hunger time for " + id + "was " + average);
		
	}
	
	public void getToken(int x) {
		// Hand token of value x to client
		token.add(x);
	}
	
	public Client(Server s, int given_id) {
		controller = s;   // Server the client belongs to
		id = given_id;    // The id given to the client
	}
	
}
