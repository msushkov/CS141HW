import java.util.concurrent.ArrayBlockingQueue;

/*
 * Server thread creates n clients.
 */
public class Server implements Runnable {
	int numClients;
	int finishedClients = 0;
	
	// Blocking request queue for clients holding their IDs
	private ArrayBlockingQueue<Integer> queue;
	
	// Blocking queue of token
	private ArrayBlockingQueue<Integer> token = new ArrayBlockingQueue<Integer>(1);
	
	// Clients and threads stored in arrays
	private Client[] clients;
	private Thread[] threads;
	private long[] averageHungerTimes;

	public Server(int given_num_clients) {
		numClients = given_num_clients;
		queue = new ArrayBlockingQueue<Integer>(numClients);
		clients = new Client[numClients];
		threads = new Thread[numClients];
		averageHungerTimes = new long[numClients];
		for (int i = 0; i < numClients; i++) {
			clients[i] = new Client(this, i);
			threads[i] = new Thread(clients[i]);
			threads[i].start();
		}
		// Adding initial token of value 0
		token.add(0);
	}

	public void run() {
		// Holds the ID of the next client to be given the token
		int clientId;	 
		
		while (true) {
			try {
				// wait for the token until we acquire it
				int value = token.take();
				
				// if we are done calculate avg hungry time and break
				if (finishedClients == numClients) {
					// Making sure we receive client data
					Thread.sleep(500);
					
					System.out.println("THE END");
					
					// calculate avg hungry time
					long total = 0;
					for (int i = 0; i < numClients; i++)
						total = total + averageHungerTimes[i];

					System.out.println("The average hungry time was " + total/numClients);
					break;
				}
				// wait for a request until we get one
				clientId = queue.take();
				
				// send the token to the appropriate client
				clients[clientId].getToken(value);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void getToken(int x, boolean done) {
		token.add(x);
		if (done) {
			finishedClients++;
		}
	}
	
	public void getAverageHunger(int cId, long time) {
		averageHungerTimes[cId] = time;
	}
	
	public void request(int cId) {
		try {
			// Record the request by placing the ID of the client in the queue.
			queue.put(cId);
		} catch (InterruptedException e) {
			e.printStackTrace();			
		}
	}
	
}
