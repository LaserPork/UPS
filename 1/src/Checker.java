import java.io.PrintWriter;

public class Checker extends Thread{
	
	private String sent;
	private String response;
	private PrintWriter out;
	private Connection connection;
	
	public Checker(String sent, String response, Connection connection){
		this.sent = sent;
		this.response = response;
		this.out = connection.out;
		this.connection = connection;
		setDaemon(true);
		start();
	}
	
	public String getResponse(){
		return response;
	}
	
	public void run(){
		connection.as.freeze();
		for (int i = 0; i < 8; i++) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				//Message came
				System.out.println(response+" interrupted");
				connection.as.unfreeze();
				return;
			}
			System.out.println("Checker sends:	"+sent);
			out.println(sent);
		}
		connection.as.freeze();
		System.out.println(response+" frozen");
		
	}
}
