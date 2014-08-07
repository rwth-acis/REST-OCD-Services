package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AbstractOutputAdapter;

import java.io.IOException;
import java.io.Writer;

public class BenchmarkAveragesOutputAdapter extends AbstractOutputAdapter {

	public BenchmarkAveragesOutputAdapter(Writer writer) {
		this.writer = writer;
	}
	
	/*
	 * All input arrays must be of equal size and of corresponding order.
	 */
	public void writeAverages(String[] algoFileNameExtensions, double[] timeAverages, double[] nmiAverages, double[] omegaAverages) {
		try {
			writer.write("Times:\n");
			for(int i=0; i<algoFileNameExtensions.length; i++) {
				writer.write(algoFileNameExtensions[i] + ": " + timeAverages[i] + "\n");
			}
			writer.write("\n");
			writer.write("NMIs:\n");
			for(int i=0; i<algoFileNameExtensions.length; i++) {
				writer.write(algoFileNameExtensions[i] + ": " + nmiAverages[i] + "\n");
			}
			writer.write("\n");
			writer.write("Omegas:\n");
			for(int i=0; i<algoFileNameExtensions.length; i++) {
				writer.write(algoFileNameExtensions[i] + ": " + omegaAverages[i] + "\n");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
