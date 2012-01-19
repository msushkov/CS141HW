import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

public class Server implements Runnable, Queueable {

	public static final int STATE_TOKEN = 0;
	public static final int STATE_NO_TOKEN = 1;

	private int state;
	private LinkedBlockingDeque<Message> inQueue;
	private LinkedList<Message> reqQueue;
	private int runningClients;

	public Server(int numClients) {
		// Set up the default state
		state = STATE_TOKEN;
		runningClients = numClients;

		// Initialize the queues
		inQueue = new LinkedBlockingDeque<Message>();
		reqQueue = new LinkedList<Message>();
	}

	// This adds a message to the input queue
	@Override
	public void addMessage(Message m) {
		inQueue.add(m);
	}

	@Override
	public void run() {
		try {
			// Run while there are clients left
			while (runningClients > 0) {
				// Read a message
				Message m = inQueue.takeFirst();

				switch (m.getType()) {
					case Message.MESSAGE_TERMINATION:
						// If the message was termination, decrease the total number
						// of clients by one
						runningClients--;
						System.out.println(Main.getSimTime() + " "
								+ "Server: termination received");
						break;

					case Message.MESSAGE_TOKEN:
						// If it's a token, set our state to having a token
						state = STATE_TOKEN;
						System.out.println(Main.getSimTime() + " "
								+ "Server: token returned");
						break;

					case Message.MESSAGE_REQUEST:
						// If it is a request, add the request to our request queue
						reqQueue.add(m);
						System.out.println(Main.getSimTime() + " "
								+ "Server: token requested");
						break;
				}

				// If we have a token and a request to fulfill, fulfill the request
				if (!reqQueue.isEmpty() && state == STATE_TOKEN) {
					System.out.println(Main.getSimTime() + " "
							+ "Server: sending token");
					reqQueue.poll().getSender().addMessage(
							new Message(Message.MESSAGE_REQUEST, this));
					state = STATE_NO_TOKEN;
				}
			}
		} catch (InterruptedException ex) {
			// This should never happen
			ex.printStackTrace();
		}
	}
}
