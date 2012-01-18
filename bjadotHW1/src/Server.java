import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

public class Server implements Runnable, Queueable {

	public static final int STATE_TOKEN = 0;
	public static final int STATE_NO_TOKEN = 1;

	private int state;
	private LinkedBlockingDeque<Message> input_queue;
	private LinkedList<Message> request_queue;
	private int running_clients;

	public Server(int numClients) {
		state = STATE_TOKEN;
		running_clients = numClients;

		input_queue = new LinkedBlockingDeque<Message>();
		request_queue = new LinkedList<Message>();
	}

	@Override
	public void addMessage(Message m) {
		input_queue.add(m);
	}

	@Override
	public void run() {
		try {
			while (running_clients > 0) {
				Message m = input_queue.takeFirst();

				if (m.getType() == Message.MESSAGE_TERMINATION) {
					running_clients--;
					System.out.println(Main.getSimTime() + " " + "Server: termination received");
				} else if (m.getType() == Message.MESSAGE_TOKEN) {
					state = STATE_TOKEN;
					System.out.println(Main.getSimTime() + " " + "Server: token returned");
				} else if (m.getType() == Message.MESSAGE_REQUEST) {
					request_queue.add(m);
					System.out.println(Main.getSimTime() + " " + "Server: token requested");
				}

				if (!request_queue.isEmpty() && state == STATE_TOKEN) {
					System.out.println(Main.getSimTime() + " " + "Server: sending token");
					request_queue.poll().getSender().addMessage(
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
