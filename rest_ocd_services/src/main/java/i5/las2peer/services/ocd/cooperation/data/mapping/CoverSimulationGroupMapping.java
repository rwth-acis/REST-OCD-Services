package i5.las2peer.services.ocd.cooperation.data.mapping;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import i5.las2peer.services.ocd.cooperation.data.mapping.correlation.CorrelationDataset;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.cooperation.data.table.Table;
import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoverSimulationGroupMapping extends MappingAbstract {

	///// Entity Fields /////

	private Cover cover;

	private SimulationSeriesGroup simulation;

	///// Getter /////
		
	@JsonIgnore
	public Cover getCover() {
		return cover;
	}

	@JsonIgnore
	public SimulationSeriesGroup getSimulation() {
		return simulation;
	}
	///// Setter /////
	
	@JsonIgnore
	public void setCover(Cover cover) {
		this.cover = cover;
	}

	@JsonIgnore
	public void setSimulation(SimulationSeriesGroup simulation) {
		this.simulation = simulation;
	}
	
	///// Methods /////
		
	@Override
	@JsonProperty
	public double[] getPropertyValues(GraphProperty property) {
			
		int size = getCover().communityCount();
		double[] properties = new double[size];
		
		for(int communityId = 0; communityId < size; communityId++) {
			properties[communityId] = getCover().getCommunityProperty(communityId, property);
		}
		return properties;
	}
	
	@Override
	@JsonProperty
	public double[] getCooperationValues() {
		
		List<Community> communityList = getCover().getCommunities();
		double[] values = getSimulation().getAverageCommunityCooperationValues(communityList);
		
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
