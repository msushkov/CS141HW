
public class HW1 {
	
	public static void main(String[] args) {
		for (int i = 10; i < 150; i = i + 10) {
			int numClients = i;  // Number of clients to create
			Thread s = new Thread(new Server(numClients));
			System.out.println("Clients = " + i);
			s.start();
			try {
				s.join();
			} catch (Exception e) {
				
			}
		}


	}
}
