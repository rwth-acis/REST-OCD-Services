package i5.las2peer.services.ocd.cooperation.data.table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import i5.las2peer.services.ocd.cooperation.data.table.Table;

public class TablePrinter {
	
	private String path;
	private String prefix;
	private String suffix;
	private Formatter formatter;

	public TablePrinter() {
		
		this.path = "";
		this.prefix = "";
		this.suffix = "";

	}
	
	public TablePrinter(String string) {
		super();
		this.path = string;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public Formatter getFormatter() {
		return formatter;
	}

	public void setFormatter(Formatter formatter) {
		this.formatter = formatter;
	}
	
	public void printTable(TableInterface tableInterface) {
		
		Table table = tableInterface.toTable();
		File path = new File(getPath());
		path.mkdirs();
		File file = buildFile(path, tableInterface.getName());
		printTable(table, file);
		
	}

	public File buildFile(File pathFile, String name) {
		return new File(pathFile, prefix + name + suffix);
	}

	protected File buildFile(String name) {
		return new File(prefix + name + suffix);
	}
	
	public void printTable(Table table, File file) {

		Writer writer;
		try {
			writer = new FileWriter(file, false);
			for (TableRow row : table.getTableRows()) {
				writer.write(row.print());
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	
}
