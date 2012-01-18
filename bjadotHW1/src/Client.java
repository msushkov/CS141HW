import java.util.concurrent.LinkedBlockingDeque;

public class Client implements Runnable, Queueable {

	private final int maxThinkingTime = 195;
	private final int minThinkingTime = 205;
	private final int maxEatingTime = 15;
	private final int minEatingTime = 25;

	private final int iterations;
	private LinkedBlockingDeque<Message> inputQueue;
	private Server server;

	public Client(int its, Server s) {
		iterations = its;

		server = s;
		inputQueue = new LinkedBlockingDeque<Message>();
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < iterations; i++) {
				// Thinking state
				long time = Main.getSimTime();

				// Sleep for a random time
				int rand_time = (int) (Math.random() * (maxThinkingTime - minThinkingTime))
						+ minThinkingTime;
				System.out.println(Main.getSimTime() + " " + Thread.currentThread() + ": Thinking for "
						+ rand_time);

				Thread.sleep(rand_time);
				Main.updateThinkingTime(Main.getSimTime() - time);
				
				// Hungry state
				time = Main.getSimTime();
				System.out.println(Main.getSimTime() + " " + Thread.currentThread() + ": Hungry");

				// Send request to server
				server.addMessage(new Message(Message.MESSAGE_REQUEST, this));

				// Wait for request to be returned. Our input queue should be
				// empty unless there is a token in it.
				inputQueue.takeFirst();
				System.out.println(Main.getSimTime() + " " + Thread.currentThread()
						+ ": Received request token");
				Main.updateHungryTime(Main.getSimTime() - time);
				
				// Eating state
				time = Main.getSimTime();
				rand_time = (int) (Math.random() * (maxEatingTime - minEatingTime))
						+ minEatingTime;
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
		inputQueue.add(m);

	}
}
