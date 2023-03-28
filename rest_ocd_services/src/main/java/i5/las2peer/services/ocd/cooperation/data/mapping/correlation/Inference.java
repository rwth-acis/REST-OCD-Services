package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

import java.util.List;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.cooperation.data.table.TableRow;


public class Inference {

		///////// Entity Fields ///////////
		private double wilcoxon;

		private double mannWhitney;
	

		/////////// Constructor ///////////

		public Inference() {

		}

		public Inference(double[] val1, double[] val2) {			
			
			this.wilcoxon = calculateWilcoxons(val1, val2);
			this.mannWhitney = calculateMannWhitneyUs(val1, val2);
		}

		public Inference(List<Double> valList1, List<Double> valList2) {
			
			int val1Size = valList1.size();
			int val2Size = valList2.size();
			double[] val1 = new double[val1Size];
			double[] val2 = new double[val2Size];
			
			for (int i = 0; i < val1Size; i++) {
				val1[i] = valList1.get(i);
				val2[i] = valList2.get(i);
			}

			this.wilcoxon = calculateWilcoxons(val1, val2);
			this.mannWhitney = calculateMannWhitneyUs(val1, val2);
		}

		/////////// Calculations ///////////
						
		protected double calculateWilcoxons(double[] val1, double[] val2) {
			
			WilcoxonSignedRankTest correlation = new WilcoxonSignedRankTest();
			return correlation.wilcoxonSignedRank(val1, val2);
		}
		
		protected double calculateMannWhitneyUs(double[] val1, double[] val2) {
			
			MannWhitneyUTest correlation = new MannWhitneyUTest();
			return correlation.mannWhitneyU(val1, val2);
		}


		//////////// Getter /////////////
				
		@JsonProperty
		public double getWilcoxon() {
			return this.wilcoxon;
		}
		
		@JsonProperty
		public double getMannWhitneyU() {
			return this.mannWhitney;
		}
		
		///// Setter /////
				
		@JsonSetter
		public void setWilcoxon(double wilcoxon) {
			this.wilcoxon = wilcoxon;
		}
		
		@JsonSetter
		public void setMannWhitneyU(double whitney) {
			this.mannWhitney = whitney;
		}
		
		/////////// Print ///////////

		public TableRow toTableLine() {		

			TableRow line = new TableRow();		
			line.add(getWilcoxon()).add(getMannWhitneyU());		
			return line;
		}

		public TableRow toHeadLine() {

			TableRow line = new TableRow();
			line.add("wilcoxon").add("whitneyU");
			return line;

		}
	
}
