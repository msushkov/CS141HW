public class Message {

	public static final int MESSAGE_TERMINATION = 0;
	public static final int MESSAGE_REQUEST = 1;
	public static final int MESSAGE_TOKEN = 2;

	private final int type;
	private final Queueable sender;

	public Message(int type, Queueable sender) {
		this.type = type;
		this.sender = sender;
	}

	public int getType() {
		return type;
	}

	public Queueable getSender() {
		return sender;
	}
}
