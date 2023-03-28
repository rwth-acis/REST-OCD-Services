package i5.las2peer.services.ocd.cooperation.data.mapping;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import i5.las2peer.services.ocd.cooperation.data.mapping.correlation.CorrelationDataset;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.cooperation.data.table.Table;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationGroupSetMapping extends MappingAbstract {

	///// Entity Fields /////

	private List<SimulationSeriesGroup> simulation;

	private String username;

	///// Getter /////

	@JsonProperty
	public List<SimulationSeriesGroup> getSimulation() {
		return simulation;
	}
	
	///// Setter /////

	@JsonIgnore
	public void setSimulation(List<SimulationSeriesGroup> simulation) {
		this.simulation = simulation;
	}
	
	///// Methods /////
		
	@Override
	@JsonIgnore
	public double[] getPropertyValues(GraphProperty property) {		

		List<SimulationSeriesGroup> simulationGroup = getSimulation();
		int size = simulationGroup.size();
		double[] properties = new double[size];
		
		for(int i=0; i<size; i++) {
			CustomGraph network = simulationGroup.get(i).getNetwork();
			properties[i] = network.getProperty(property);

		}		
		return properties;		
	}
	
	@Override
	@JsonProperty
	public double[] getCooperationValues() {
		
		List<SimulationSeriesGroup> series = getSimulation();
		int size = series.size();
		double[] values = new double[size];
		
		for(int i=0; i<size; i++) {
			SimulationSeriesGroup sim = series.get(i);
			values[i] = sim.averageCooperationValue();				
		}
		
		return values;		
	}	

	@JsonProperty
	public double[] getSize() {
		return getPropertyValues(GraphProperty.SIZE);
	}

	@JsonProperty
	public double[] getAverageDegree() {
		return getPropertyValues(GraphProperty.AVERAGE_DEGREE);
	}

	@JsonProperty
	public double[] getDensityValues() {
		return getPropertyValues(GraphProperty.DENSITY);
	}

	@JsonProperty
	public double[] getSizeDegreeDeviation() {
		return getPropertyValues(GraphProperty.DEGREE_DEVIATION);
	}

	@JsonProperty
	public double[] getClusteringCoefficient() {
		return getPropertyValues(GraphProperty.CLUSTERING_COEFFICIENT);
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

	public void correlate(String username) {
		
		this.username = username;

		this.correlate();
		
	}



}
