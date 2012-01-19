import java.util.concurrent.LinkedBlockingDeque;

public class Client implements Runnable, Queueable {

	private final int maxThinkT = 195;
	private final int minThinkT = 205;
	private final int maxEatT = 15;
	private final int minEatT = 25;

	private final int iters;
	private LinkedBlockingDeque<Message> inQueue;
	private Server server;

	public Client(int its, Server s) {
		iters = its;

		server = s;
		inQueue = new LinkedBlockingDeque<Message>();
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < iters; i++) {
				// Thinking state
				// Calculate simulation time
				long time = Main.getSimTime();

				// Sleep for a random time
				int rand_time = (int) (Math.random() * (maxThinkT - minThinkT))
						+ minThinkT;
				System.out.println(Main.getSimTime() + " "
						+ Thread.currentThread() + ": Thinking for "
						+ rand_time);
				Thread.sleep(rand_time);
				
				Main.updateThinkingTime(Main.getSimTime() - time);

				// Hungry state
				time = Main.getSimTime();
				System.out.println(Main.getSimTime() + " "
						+ Thread.currentThread() + ": Hungry");

				// Send request to server
				server.addMessage(new Message(Message.MESSAGE_REQUEST, this));

				// Wait for request to be returned. Our input queue should be
				// empty unless there is a token in it.
				inQueue.takeFirst();
				System.out.println(Main.getSimTime() + " "
						+ Thread.currentThread() + ": Received request token");
				Main.updateHungryTime(Main.getSimTime() - time);

				// Eating state
				time = Main.getSimTime();
				
				// Eat for a random time
				rand_time = (int) (Math.random() * (maxEatT - minEatT))
						+ minEatT;
				System.out.println(Main.getSimTime() + " "
						+ Thread.currentThread() + ": Eating for " + rand_time);

				Thread.sleep(rand_time);

				System.out.println(Main.getSimTime() + " "
						+ Thread.currentThread() + ": Returning request token");
				Main.updateEatTime(Main.getSimTime() - time);
				// Return token
				server.addMessage(new Message(Message.MESSAGE_TOKEN, this));
			}
		} catch (InterruptedException e) {
			// This should never happen
			e.printStackTrace();
		} finally {
			System.out.println(Main.getSimTime() + " " + Thread.currentThread()
					+ ": Terminating");
			server.addMessage(new Message(Message.MESSAGE_TERMINATION, this));
		}
	}

	// Add a message to the input queue
	@Override
	public void addMessage(Message m) {
		inQueue.add(m);

	}
}
