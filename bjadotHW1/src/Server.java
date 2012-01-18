import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

public class Server implements Runnable, Queueable {

	public static final int STATE_TOKEN = 0;
	public static final int STATE_NO_TOKEN = 1;

	private int state;
	private LinkedBlockingDeque<Message> inputQueue;
	private LinkedList<Message> requestQueue;
	private int runningClients;

	public Server(int numClients) {
		state = STATE_TOKEN;
		runningClients = numClients;

		inputQueue = new LinkedBlockingDeque<Message>();
		requestQueue = new LinkedList<Message>();
	}

	@Override
	public void addMessage(Message m) {
		inputQueue.add(m);
	}

	@Override
	public void run() {
		try {
			while (runningClients > 0) {
				Message m = inputQueue.takeFirst();

				if (m.getType() == Message.MESSAGE_TERMINATION) {
					runningClients--;
					System.out.println(Main.getSimTime() + " " + "Server: termination received");
				} else if (m.getType() == Message.MESSAGE_TOKEN) {
					state = STATE_TOKEN;
					System.out.println(Main.getSimTime() + " " + "Server: token returned");
				} else if (m.getType() == Message.MESSAGE_REQUEST) {
					requestQueue.add(m);
					System.out.println(Main.getSimTime() + " " + "Server: token requested");
				}

				if (!requestQueue.isEmpty() && state == STATE_TOKEN) {
					System.out.println(Main.getSimTime() + " " + "Server: sending token");
					requestQueue.poll().getSender().addMessage(
							new Message(Message.MESSAGE_REQUEST, this));
					state = STATE_NO_TOKEN;
				}
			}
		} catch (InterruptedException ex) {
			// This should never happen
			ex.printStackTrace();
		}
	}

	public int getState() {
		return state;
	}
}
