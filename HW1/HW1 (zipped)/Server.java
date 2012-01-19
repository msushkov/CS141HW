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
	
	// Variables holding the total of all clients' average times
	private long hungerTimes = 0;
	private long eatingTimes = 0;
	private long thinkingTimes = 0;

	public Server(int given_num_clients) {
		// Construction initialising values and arrays
		numClients = given_num_clients;
		queue = new ArrayBlockingQueue<Integer>(numClients);
		clients = new Client[numClients];
		threads = new Thread[numClients];
		
		// Create and start the clients as threads and save both the thread and client objects in arrays
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
		
		// Wait for all clients (specifically the last) to finish reporting their average times 
		for (int i = 0; i < numClients; i++) {
			try {
				threads[i].join();	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		// Calculating and printint the average times for thinking, hunger and eating
		System.out.println("Average thinking time was " + thinkingTimes / numClients + " ns");
		System.out.println("Average hunger time was " + hungerTimes / numClients + " ns");
		System.out.println("Average eating time was " + eatingTimes / numClients + " ns");
		
		System.out.println("Server finished.");
	}
	
	// Gets the token back from a client and a boolean indicating if the client is full
	public void getToken(int x, boolean done) {
		token.add(x);
		
		// If the client reports done record this
		if (done) {
			finishedClients++;
		}
	}
	
	// Receives the average times from a client and adds their times to the total count
	public void getAverageTimes(long thinking, long hungry, long eating) {
		thinkingTimes += thinking;
		hungerTimes += hungry;
		eatingTimes += eating;
	}
	
	// Gets the ID of a client wanting the token and puts this in the queue
	public void request(int cId) {
		try {
			// Record the request by placing the ID of the client in the queue.
			queue.put(cId);
		} catch (InterruptedException e) {
			e.printStackTrace();			
		}
	}
	
}
