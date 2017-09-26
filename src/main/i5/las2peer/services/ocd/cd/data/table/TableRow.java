package i5.las2peer.services.ocd.cd.data.table;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import i5.las2peer.services.ocd.cd.data.table.TableRow;

public class TableRow {

	List<String> cells;

	public TableRow() {
		cells = new ArrayList<>();
	}

	public TableRow(String cell) {
		this();
		this.add(cell);
	}

	// Add

	public TableRow add(String cell) {
		cells.add(cell);
		return this;
	}

	public TableRow add(int cell) {
		cells.add(String.valueOf(cell));
		return this;
	}

	public TableRow add(long cell) {
		cells.add(String.valueOf(cell));
		return this;
	}

	public TableRow add(double cell) {
		cells.add(String.valueOf(cell));
		return this;
	}

	public TableRow add(TableRow row) {
		for (String cell : row.getCells()) {
			cells.add(cell);
		}
		return this;
	}

	public TableRow add(TableLineInterface row) {
		this.add(row.toTableLine());
		return this;
	}

	// Add Front

	public TableRow addFront(String cell) {
		cells.add(0, cell);
		return this;
	}

	public TableRow addFront(int cell) {
		addFront(String.valueOf(cell));
		return this;
	}

	public TableRow addFront(double cell) {
		addFront(String.valueOf(cell));
		return this;
	}

	public TableRow addFront(long cell) {
		addFront(String.valueOf(cell));
		return this;
	}

	// Clear

	public void clear() {
		cells.clear();
	}

	public int size() {
		return cells.size();
	}

	// Getter

	protected List<String> getCells() {
		return cells;
	}
	
	public String get(int cellId) {
		return this.getCells().get(cellId);
	}

	// Prefix Suffix

	public TableRow prefix(String prefix) {

		for (int i = 0; i < size(); i++) {
			cells.set(i, prefix + cells.get(i));
		}
		return this;
	}

	public TableRow suffix(String suffix) {

		for (int i = 0; i < size(); i++) {
			cells.set(i, cells.get(i) + suffix);
		}
		return this;
	}

	// Print

	public String print() {

		return print("\t");
	}

	public String print(String seperator) {

		StringJoiner line = new StringJoiner(seperator);
		for (String cell : cells) {
			line.add(cell);
		}
		return line.toString();
	}

	public void format(Formatter formatter) {

		for (int i = 0; i < cells.size(); i++) {
			String cell = cells.get(i);
			if (cell.length() > 6 - 2) {
				cell = formatter.decimals(cell);
				cells.set(i, cell);
			}
		}
	}

}
