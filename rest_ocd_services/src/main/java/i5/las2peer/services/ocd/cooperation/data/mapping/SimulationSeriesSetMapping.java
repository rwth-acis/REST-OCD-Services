package i5.las2peer.services.ocd.cooperation.data.mapping;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import i5.las2peer.services.ocd.cooperation.data.mapping.correlation.CorrelationDataset;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cooperation.data.table.Table;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationSeriesSetMapping extends MappingAbstract {

	///// Entity Fields /////

	private List<SimulationSeries> simulation;

	///// Getter /////

	@JsonIgnore
	public List<SimulationSeries> getSimulation() {
		return simulation;
	}
	
	///// Setter /////

	@JsonIgnore
	public void setSimulation(List<SimulationSeries> simulation) {
		this.simulation = simulation;
	}
	
	///// Methods /////
		
	@Override
	@JsonProperty
	public double[] getPropertyValues(GraphProperty property) {		
		
		List<SimulationSeries> series = getSimulation();
		int size = series.size();
		double[] properties = new double[size];
		
		for(int i=0; i<size; i++) {
			CustomGraph network = series.get(i).getNetwork();
			properties[i] = network.getProperty(property);						
		}		
		return properties;		
	}
			
	@Override
	@JsonProperty
	public double[] getCooperationValues() {
		
		List<SimulationSeries> series = getSimulation();
		int size = series.size();
		double[] values = new double[size];
		
		for(int i=0; i<size; i++) {
			SimulationSeries sim = series.get(i);
			values[i] = sim.averageCooperationValue();				
		}
		
		return values;		
	}	

	///// Print /////
	
	@Override
	public Table toTable() {
						
		Table table = new Table();
			table.add("").append(CorrelationDataset.toHeadLine());			
			table.add("size").append(getSizeCorrelation());
			table.add("D").append(getDensityCorrelation());
			table.add("avg deg").append(getAverageDegreeCorrelation());
			table.add("std deg").append(getDegreeDeviationCorrelation());
			table.add("CC").append(getClusteringCoefficientCorrelation());
		
		return table;
		
	}




}
