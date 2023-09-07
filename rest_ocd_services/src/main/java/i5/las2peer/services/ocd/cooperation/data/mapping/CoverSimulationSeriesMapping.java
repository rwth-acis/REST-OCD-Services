package i5.las2peer.services.ocd.cooperation.data.mapping;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

public class CoverSimulationSeriesMapping extends MappingAbstract {

///// Entity Fields /////

	private Cover cover;

	private SimulationSeries simulation;

	///// Getter /////
		
	@JsonIgnore
	public Cover getCover() {
		return cover;
	}

	@JsonIgnore
	public SimulationSeries getSimulation() {
		return simulation;
	}
	///// Setter /////
	
	@JsonIgnore
	public void setCover(Cover cover) {
		this.cover = cover;
	}

	@JsonIgnore
	public void setSimulation(SimulationSeries simulation) {
		this.simulation = simulation;
	}
	
	///// Methods /////
		
	@Override
	@JsonProperty
	public double[] getPropertyValues(GraphProperty property) {
			
		int size = getCover().communityCount();
		double[] properties = new double[size];
		
		for(int communityId=0; communityId<size; communityId++) {
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

}
