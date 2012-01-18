
public class Request extends Message 
{
	public Client requester;
	
	public Request(Client req)
	{
		this.requester = req;
	}
	
	public Client getRequester()
	{
		return requester;
	}
}
