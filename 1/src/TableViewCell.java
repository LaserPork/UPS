import javafx.beans.property.SimpleStringProperty;

public class TableViewCell {
	
	private SimpleStringProperty name;
	private SimpleStringProperty players;
	private int id;
	
	public TableViewCell(int id, String name, int players){
		this.name = new SimpleStringProperty(name);
		this.players = new SimpleStringProperty(players+"/5");
		this.id = id;
	}
	
	public int getId(){
		return id;
	}

	public SimpleStringProperty nameProperty() {
		return name;
	}

	public void setName(SimpleStringProperty name) {
		this.name = name;
	}

	public SimpleStringProperty playersProperty() {
		return players;
	}

	public void setPlayers(SimpleStringProperty players) {
		this.players = players;
	}
	
	public void setPlayers(int players){
		this.players = new SimpleStringProperty(players+"/5");
	}
}
