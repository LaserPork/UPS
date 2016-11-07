import javafx.scene.control.TableView;

public class TablePickerRefresher extends Thread{
	
	private TableView<TableViewCell> tables;
	private Connection connection;

	
	public TablePickerRefresher(TableView<TableViewCell> tables, Connection connection){
		this.tables = tables;
		this.connection = connection;
	}
	
	public void run(){
		while(true){
			for (int i = 0; i < tables.getItems().size(); i++) {
				connection.askTable(i);
				try {
					sleep(1000);
				} catch (Exception e) {
					return;
				}
			
			}
		}
	}
	
}
