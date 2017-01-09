import java.io.PrintWriter;

public class Checker extends Thread{
	
	private String sent;
	private String response;
	private PrintWriter out;
	private Connection connection;
	
	public Checker(String sent, String response, Connection connection){
		this.sent = sent;
		this.response = response;
		this.out = connection.getOut();
		this.connection = connection;
		setDaemon(true);
		start();
	}
	
	public String getResponse(){
		return response;
	}
	
	public void run(){
		connection.getAs().freeze();
		for (int i = 0; i < 2; i++) {
			try {
				Thread.sleep(8000);
				System.out.println("Checker sends:	"+sent);
				out.println(sent);
			} catch (Exception e) {
				//Message came
				connection.getAs().unfreeze();
				return;
			}
			
		}
		System.out.println("Response took too long, disconnecting");
		connection.disconnect();
		
	}
}
