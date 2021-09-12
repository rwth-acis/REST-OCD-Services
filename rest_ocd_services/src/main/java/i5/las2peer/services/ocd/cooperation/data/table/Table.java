package i5.las2peer.services.ocd.cooperation.data.table;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import i5.las2peer.services.ocd.cooperation.data.table.TableInterface;
import i5.las2peer.services.ocd.cooperation.data.table.TableRow;

/**
 * A Table consisting of {@link TableRow}s. A {@link TablePrinter} can write the
 * table into a file.
 */
public class Table {

	private List<TableRow> rows;

	public Table() {
		rows = new ArrayList<>();
	}

	public List<TableRow> getTableRows() {
		return rows;
	}

	///// add /////

	/**
	 * Add a {@link TableRow} to the end of the Table
	 * 
	 * @param tableRow
	 *            the TableRow
	 * @return the table
	 */
	public Table add(TableRow tableRow) {
		rows.add(tableRow);
		return this;
	}

	/**
	 * Add a object that implements the {@link TableInterface} to the end of the
	 * Table.
	 * 
	 * @param row
	 *            the TableRow
	 * @return the table
	 */
	public Table add(TableLineInterface row) {
		add(row.toTableLine());
		return this;
	}

	/**
	 * Creates a new TableRow consisting of one column of the given string
	 * 
	 * @param string
	 *            the string
	 * @return the table
	 */
	public Table add(String string) {
		rows.add(new TableRow().add(string));
		return this;
	}

	///// append /////

	public Table append(String row) {
		this.append(rows()-1, row);
		return this;
	}
	
	public Table append(double row) {
		this.append(rows()-1, row);
		return this;
	}
	
	public Table append(TableRow row) {
		this.append(rows()-1, row);
		return this;
	}
	
	public Table append(TableLineInterface row) {
		this.append(rows()-1, row);
		return this;
	}
	
	public Table append(int rowId, int row) {
		this.append(rowId, String.valueOf(row));
		return this;
	}
	
	public Table append(int rowId, long row) {
		this.append(rowId, String.valueOf(row));
		return this;
	}
	
	public Table append(int rowId, Double row) {
		this.append(rowId, String.valueOf(row));
		return this;
	}
	
	public void append(int rowId, TableRow tableRow) {

		if (rowId < 0 || rowId > rows())
			throw new IllegalArgumentException("invalid row id");

		getTableRows().get(rowId).add(tableRow);
	}
	
	public void append(int rowId, TableLineInterface tableLineInterface) {

		if (rowId < 0 || rowId > rows())
			throw new IllegalArgumentException("invalid row id");

		getTableRows().get(rowId).add(tableLineInterface);
	}
	
	public void append(int rowId, String row) {

		if (rowId < 0 || rowId > rows())
			throw new IllegalArgumentException("invalid row id");

		getTableRows().get(rowId).add(row);
	}
	
	///// size /////
	
	public int rows() {
		if (rows == null)
			return 0;
		return rows.size();
	}

	public int columns() {
		if (rows == null || rows.size() == 0)
			return 0;
		return rows.get(0).size();
	}
	
	///// format /////
	
	public void format(Formatter formatter) {
		
		for (TableRow row : rows) {
			row.format(formatter);
		}
	}
	
	///// print /////
	
	public String print() {

		StringJoiner table = new StringJoiner("\n");
		for (TableRow row : rows) {
			table.add(row.print());
		}
		return table.toString();
	}




}
