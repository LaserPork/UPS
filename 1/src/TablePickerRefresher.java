import javafx.scene.control.TableView;

public class TablePickerRefresher extends Thread{
	
	private TableView<TableViewCell> tables;
	private Connection connection;
	private boolean running = true;
	
	public TablePickerRefresher(TableView<TableViewCell> tables, Connection connection){
		this.tables = tables;
		this.connection = connection;
	}
	
	public void run(){
		while(running){
			for (int i = 0; i < tables.getItems().size(); i++) {
				try {
					if(connection.out != null){
						connection.askTable(i);
					}
					sleep(1000);
				} catch (Exception e) {
					return;
				}
				if(!running){
					break;
				}
			
			}
		}
	}
	
	public void stopRefreshing(){
		this.running = false;
	}
	
}
