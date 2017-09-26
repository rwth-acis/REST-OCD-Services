package i5.las2peer.services.ocd.cd.data.mapping;

import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import i5.las2peer.services.ocd.cd.data.simulation.SimulationList;
import i5.las2peer.services.ocd.cd.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.cd.data.table.Table;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationGroupSetMapping extends MappingAbstract {

	///// Entity Fields /////

	@ManyToOne(fetch = FetchType.LAZY)
	private SimulationList simulation;

	///// Getter /////

	@JsonIgnore
	public SimulationList getSimulation() {
		return simulation;
	}
	
	///// Setter /////

	@JsonIgnore
	public void setSimulation(SimulationList simulation) {
		this.simulation = simulation;
	}
	
	///// Methods /////
		
	@Override
	@JsonProperty
	public double[] getPropertyValues(GraphProperty property) {		
		
		List<SimulationSeriesGroup> series = getSimulation();
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
		
		List<SimulationSeriesGroup> series = getSimulation();
		int size = series.size();
		double[] values = new double[size];
		
		for(int i=0; i<size; i++) {
			SimulationSeriesGroup sim = series.get(i);
			values[i] = sim.averageCooperationValue();				
		}
		
		return values;		
	}	

	///// Print /////
	
	@Override
	public Table toTable() {
						
		Table table = new Table();
			table.add("").append(Correlation.toHeadLine());			
			table.add("size").append(getSizeCorrelation());
			table.add("D").append(getDensityCorrelation());
			table.add("avg deg").append(getAverageDegreeCorrelation());
			table.add("std deg").append(getDegreeDeviationCorrelation());
			table.add("CC").append(getClusteringCoefficientCorrelation());
		
		return table;
		
	}




}
