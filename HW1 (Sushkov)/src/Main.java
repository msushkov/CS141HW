
public class Main 
{
	private final static int numClients = 2;
	
	public static void main(String[] agrs) throws InterruptedException
	{
		// create server thread
		System.out.println("Creating server thread.");
		
		Thread t = new Thread(new Server(numClients));
		t.start();
	}
}
