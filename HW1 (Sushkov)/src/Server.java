import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Server thread creates N clients.
 * 
 * @author msushkov
 *
 */
public class Server implements Runnable 
{
	// stores the number of client threads
	private final int N;

	// keep track of how many clients are still active 
	// (if this is 0 then we are done)
	private int numActiveClients;

	// queue of size 1 to hold the token
	public BlockingQueue<Token> serverTokenQueue;

	// FIFO queue of size N to hold the requests 
	// (requests are stored in the order they appear)
	public BlockingQueue<Message> requestQueue;


	/**
	 * Initializes a Server object.
	 * 
	 * @param n represents the # of client threads
	 */
	public Server(int n) throws InterruptedException
	{
		N = n;
		numActiveClients = n;
		serverTokenQueue = new LinkedBlockingQueue<Token>(1);
		requestQueue = new LinkedBlockingQueue<Message>(n);

		// initially server has the token
		serverTokenQueue.put(new Token());
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
		int count = 0;

		// create N client threads
		for (int i = 0; i < N; i++)
		{
			Thread client = new Thread(new Client(count, this));
			client.start();
			count++;
		}
		
		Token token = null;
		Message currMsg = null;

		// do stuff while there are still active clients
		while (numActiveClients > 0)
		{
			// acquire the token when it arrives
			token = serverTokenQueue.take();

			// wait for message in request queue
			currMsg = requestQueue.take();					

			// if this message is a request
			if (currMsg instanceof Request)
			{
				// get the client that sent the current request
				Client currClient = ((Request) currMsg).getRequester();

				// send the token to requester
				currClient.clientTokenQueue.put(token);
			}

			// if this message is a terminated message
			else if (currMsg instanceof TerminatedMessage)
			{
				// need to put the token back into the queue since we did not send it
				serverTokenQueue.add(token);
				
				System.out.println("Server just received a terminated message.");
				numActiveClients--;
			}

			// error
			else
				System.out.println("ERROR: Server received the wrong message type.");
			
		} // end while

		System.out.println("Server thread is done!");
	}
}
