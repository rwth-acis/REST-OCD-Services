package i5.las2peer.services.ocd.cooperation.data.table;

import java.util.List;

public class TableRowFormatter extends Formatter {

	private int places;

	public TableRowFormatter() {

	}

	public TableRowFormatter(int places) {
		this.places = places;
	}

	public TableRow decimals(TableRow row) {

		return decimals(row, places);
	}

	public TableRow decimals(TableRow row, int digits) {

		List<String> cells = row.getCells();
		for (int i = 0; i < cells.size(); i++) {
			String cell = cells.get(i);
			try {
				double d = Double.parseDouble(cell);
				if (d % 1.0 < 0.00000001 && d % 1.0 > -0.000000001) {
					cell = String.valueOf((int) d);
				} else {
					cell = decimals(cell, digits);
				}
				cells.set(i, cell);
			} catch (Exception e) {
			}
		}
		return row;
	}

	public TableRow replace(TableRow row, String string, String string2) {

		for (int i = 0; i < row.getCells().size(); i++) {
			String cell = row.getCells().get(i);
			cell = cell.replaceAll(string, string2);
			row.getCells().set(i, cell);
		}
		return row;
	}

}
