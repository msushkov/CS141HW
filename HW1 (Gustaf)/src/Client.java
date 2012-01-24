import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class Client implements Runnable {
	
	private int id; // the ID of the client
	Server controller;  // the server the client belongs to
	Random rand = new Random();
	
	int minH = 195; 	// min time to be thinking
	int diffH = 10; 	// max difference
	
	int minE = 15;      // min time to be eating
	int diffE = 10;     // max difference
	
	int M = 20;   		// Number of times a client wants to "eat"
	
	// Time variables keeping track of how long a client has been thinking, hungry and eating respectively
	long totalThinkingTime = 0;
	long totalHungryTime = 0;
	long totalEatingTime = 0;
	
	// The client's private blocking queue for the token
	private ArrayBlockingQueue<Integer> token = new ArrayBlockingQueue<Integer>(1);
	
	public void run() {
		boolean done = false; // set when the client has eaten M times
		
		for (int i = 0; i < M; i++) {
			try {
				// Thinking
				long thinkingStart = System.nanoTime();  // record start of thinking
				int thinkFor = minH + rand.nextInt(diffH);
				Thread.sleep(thinkFor);	// think for random time
				long thinkingEnd = System.nanoTime();  // record end of thinking
				
				// Hungry
				long hungerStart = System.nanoTime(); // record start of hunger
				controller.request(id);
				//System.out.println("requested and am " + id);
				int value = token.take();
				long hungerEnd = System.nanoTime(); // record end of hunger
				
				// Eating
				long eatingStart = System.nanoTime(); // record start of eating
				int eatFor = minE + rand.nextInt(diffE);
				//System.out.println("eating and am " + id);
				Thread.sleep(eatFor);
				long eatingEnd = System.nanoTime(); // record end of eating
				
				// Last run?
				if (i == M - 1) {
					done = true;
				}
				
				// return the token to the server
				controller.getToken(value, done);
				
				// record the thinking, hungry and eating times for the round
				totalThinkingTime += thinkingEnd - thinkingStart;
				totalHungryTime += hungerEnd - hungerStart;
				totalEatingTime += eatingEnd - eatingStart;

			}
			catch (InterruptedException e) {
				e.printStackTrace();			
			}
			
		}
		//System.out.println("done and am " + id);
		long averageThinking = totalThinkingTime / M;
		long averageHunger = totalHungryTime / M;
		long averageEating = totalEatingTime / M;
		controller.getAverageTimes(averageThinking, averageHunger, averageEating);
		
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
