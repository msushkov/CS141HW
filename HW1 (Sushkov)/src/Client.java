import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Defines a Client thread.
 * 
 * @author msushkov
 *
 */
public class Client implements Runnable
{
	// ID for the current client thread
	public int ID;

	// stores the handle to the server object
	private Server server;

	// queue of size 1 to hold the token
	public BlockingQueue<Token> clientTokenQueue;

	/**
	 * Sets the client thread's id, the server, and instantiates the token queue.
	 * 
	 * @param id specifies the id of the current thread
	 * @param server gives a handle to the server object
	 */
	public Client(int id, Server server)
	{
		ID = id;
		clientTokenQueue = new LinkedBlockingQueue<Token>(1);
		this.server = server;
	}

	@Override
	public void run()
	{
		try 
		{
			runHelper();
		} 
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void runHelper() throws InterruptedException
	{
		int M = 100;
		int maxWaitTimeInSec = 2;
		Random x = new Random();

		// loop M times
		for (int i = 0; i < M; i++)
		{
			// THINKING

			System.out.println("Client thread " + ID + " is thinking!");

			// wait for a random time
			Thread.sleep(x.nextInt(maxWaitTimeInSec) * 1000);

			// ----------------------------
			// HUNGRY

			System.out.println("Client thread " + ID + " is hungry!");

			// append request message to server's request queue
			server.requestQueue.put(new Request(this));

			// take the token from the queue when it arrives
			Token currToken = clientTokenQueue.take();
			System.out.println("Client thread " + ID + " just took token from queue.");

			//----------------------------
			// EATING

			System.out.println("Client thread " + ID + " is eating!");

			// wait for a random time
			Thread.sleep(x.nextInt(maxWaitTimeInSec) * 1000);

			// append token to server's token queue			
			server.serverTokenQueue.put(currToken);
			System.out.println("Client thread " + ID + " just gave the server the token back.");

		} // end for

		System.out.println("Client thread " + ID + " is sending a terminated message.");

		// append terminated message to server's terminated queue
		server.requestQueue.put(new TerminatedMessage());

		System.out.println("Client " + this.ID + " is done!");

	} // end run()
}
