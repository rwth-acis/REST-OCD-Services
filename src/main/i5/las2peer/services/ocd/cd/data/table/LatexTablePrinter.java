package i5.las2peer.services.ocd.cd.data.table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class LatexTablePrinter extends TablePrinter {
	
	private String caption;
	
	public String getCaption() {
		return this.caption;
	}
	
	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	@Override
	public void printTable(Table table, File file) {

		Writer writer;
		try {
			writer = new FileWriter(file, false);
			writeLatexTable(table, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Writer writeLatexTable(Table table, Writer writer) throws IOException {

		TableRowFormatter formatter = new TableRowFormatter(4);

		// begin table
		writer.append("\\begin{table}[]").append("\n");
		writer.append("\\caption{").append(getCaption()).append("}").append("\n");
		writer.append("\\label{}").append("\n");
		writer.append("\\centering");
		writer.append("\\begin{adjustbox}{max width=\\textwidth}");
		writer.append("\\begin{tabular}{*{");
		writer.append(String.valueOf(table.columns()));
		writer.append("}{l}}").append("\n");

		// headline
		List<TableRow> rows = table.getTableRows();
		TableRow headline = rows.get(0);
		writer.append(headline.print("&").replace("#", "\\#"));
		writer.append("\\\\ ").append("\\hline").append("\n");

		// content
		for (int i = 1; i < rows.size(); i++) {
			TableRow row = rows.get(i);
			row = formatter.decimals(row);
			row = formatter.replace(row, "_", " ");
			{
				writer.append(row.print("&"));
				writer.append("\\\\");
				if (i % 5 == 0)
					writer.append("\\hline");
				writer.append("\n");
			}
		}

		// end table
		writer.append("\\end{tabular}").append("\n");
		writer.append("\\end{adjustbox}");
		writer.append("\\end{table}").append("\n");
		writer.close();
		return writer;

	}
	
}
