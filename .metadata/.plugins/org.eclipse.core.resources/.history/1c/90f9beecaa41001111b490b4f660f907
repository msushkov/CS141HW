import java.util.concurrent.LinkedBlockingDeque;

public class Client implements Runnable, Queueable {

	public static final int STATE_THINKING = 0;
	public static final int STATE_HUNGRY = 1;
	public static final int STATE_EATING = 2;

	private final int max_thinking_time = 195;
	private final int min_thinking_time = 205;
	private final int max_eating_time = 15;
	private final int min_eating_time = 25;

	private int state;
	private final int iterations;
	private LinkedBlockingDeque<Message> input_queue;
	private Server server;

	public Client(int its, Server s) {
		state = STATE_THINKING;
		iterations = its;

		server = s;
		input_queue = new LinkedBlockingDeque<Message>();
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < iterations; i++) {
				// Thinking state
				state = STATE_THINKING;
				long time = Main.getSimTime();

				// Sleep for a random time
				int rand_time = (int) (Math.random() * (max_thinking_time - min_thinking_time))
						+ min_thinking_time;
				System.out.println(Main.getSimTime() + " " + Thread.currentThread() + ": Thinking for "
						+ rand_time);

				Thread.sleep(rand_time);
				Main.updateThinkingTime(Main.getSimTime() - time);
				
				// Hungry state
				state = STATE_HUNGRY;
				time = Main.getSimTime();
				System.out.println(Main.getSimTime() + " " + Thread.currentThread() + ": Hungry");

				// Send request to server
				server.addMessage(new Message(Message.MESSAGE_REQUEST, this));

				// Wait for request to be returned. Our input queue should be
				// empty unless there is a token in it.
				input_queue.takeFirst();
				System.out.println(Main.getSimTime() + " " + Thread.currentThread()
						+ ": Received request token");
				Main.updateHungryTime(Main.getSimTime() - time);
				
				// Eating state
				state = STATE_EATING;
				time = Main.getSimTime();
				rand_time = (int) (Math.random() * (max_eating_time - min_eating_time))
						+ min_eating_time;
				System.out.println(Main.getSimTime() + " " + Thread.currentThread() + ": Eating for "
						+ rand_time);
				
				Thread.sleep(rand_time);

				System.out.println(Main.getSimTime() + " " + Thread.currentThread()
						+ ": Returning request token");
				Main.updateEatTime(Main.getSimTime() - time);
				server.addMessage(new Message(Message.MESSAGE_TOKEN, this));
			}
		} catch (InterruptedException e) {
			// This should never happen
			e.printStackTrace();
		} finally {
			System.out.println(Main.getSimTime() + " " + Thread.currentThread() + ": Terminating");
			server.addMessage(new Message(Message.MESSAGE_TERMINATION, this));
		}
	}

	@Override
	public void addMessage(Message m) {
		input_queue.add(m);

	}

	public int getState() {
		return state;
	}
}
