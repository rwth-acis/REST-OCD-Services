package i5.las2peer.services.ocd.cd.data.table;

/**
 * A DataTable
 */
public class DataTable extends Table {

	public void add(double[] val1, double[] val2) {

		if (val1.length != val2.length)
			throw new IllegalArgumentException("array lenght do not match");

		int size = val1.length;
		for (int i = 0; i < size; i++) {
			this.add(val1[i], val2[i]);
		}
	}

	public void add(double[] val1, double[] val2, double[] val3) {

		if (val1.length != val2.length || val1.length != val2.length)
			throw new IllegalArgumentException("array lenght do not match");

		int size = val1.length;
		for (int i = 0; i < size; i++) {
			this.add(val1[i], val2[i], val3[i]);
		}
	}

	public void append(double[] val) {

		if (val.length != rows())
			throw new IllegalArgumentException("array lenght do not match");

		int size = val.length;
		for (int i = 0; i < size; i++) {
			this.append(i, String.valueOf(val[i]));
		}
	}
	
	public void append(String[] val) {

		if (val.length != rows())
			throw new IllegalArgumentException("array lenght do not match");

		int size = val.length;
		for (int i = 0; i < size; i++) {
			this.append(i, String.valueOf(val[i]));
		}
	}

	public void add(double val1, double val2) {
		TableRow row = new TableRow().add(val1).add(val2);
		this.add(row);		
	}
	
	public void add(double val1, double val2, double val3) {
		TableRow row = new TableRow().add(val1).add(val2).add(val3);
		this.add(row);		
	}

}
