
public class HW1 {
	
	public static void main(String[] args) {
		for (int i = 1; i <= 51; i = i + 5) {
			int numClients = i;  // Number of clients to create
			Thread s = new Thread(new Server(numClients));
			//System.out.println("Clients = " + i);
			System.out.print("{" + i + ", ");
			s.start();
			try {
				s.join();
			} catch (Exception e) {
				
			}
			System.out.print("}, ");
		}


	}
}
