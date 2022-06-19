package i5.las2peer.services.ocd.cooperation.data.mapping;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.ocd.cooperation.data.mapping.correlation.CorrelationDataset;
import i5.las2peer.services.ocd.cooperation.data.table.Table;
import i5.las2peer.services.ocd.cooperation.data.table.TableInterface;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

public class MappingList extends ArrayList<CoverSimulationGroupMapping> implements TableInterface {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	int communityCount = 0;

	public MappingList(String name) {
		this.setName(name);
	}
	

	@Override
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public double[] getPropertyValues(GraphProperty property) {

		List<Double> values = new ArrayList<Double>();

		for (CoverSimulationGroupMapping mapping : this) {

			double[] properties = mapping.getPropertyValues(property);
			for (int i = 0; i < properties.length; i++) {
				if (mapping.getCover().getCommunitySize(i) > 1) {
					if (mapping.getCover().getCommunitySize(i) < mapping.getCover().getGraph().getNodeCount()) {
						values.add(properties[i]);
					}
				}
			}
		}

		double[] result = new double[values.size()];
		for (int i = 0; i < values.size(); i++) {
			result[i] = values.get(i);
		}

		return result;
	}

	public double[] getCooperationValues() {

		communityCount = 0;
		List<Double> values = new ArrayList<Double>();
		for (CoverSimulationGroupMapping mapping : this) {
			double[] cooperation = mapping.getCooperationValues();
			for (int i = 0; i < cooperation.length; i++) {
				if (mapping.getCover().getCommunitySize(i) > 1) {
					if (mapping.getCover().getCommunitySize(i)  < mapping.getCover().getGraph().getNodeCount()) {
						values.add(cooperation[i]);
					}
				}
			}
		}

		double[] result = new double[values.size()];
		for (int i = 0; i < values.size(); i++) {
			result[i] = values.get(i);
		}

		communityCount = result.length;
		return result;
	}

	@Override
	public Table toTable() {

		CorrelationDataset sizeCorrelation = new CorrelationDataset(getCooperationValues(), getPropertyValues(GraphProperty.SIZE));
		CorrelationDataset densityCorrelation = new CorrelationDataset(getCooperationValues(), getPropertyValues(GraphProperty.DENSITY));
		CorrelationDataset avgDegCorrelation = new CorrelationDataset(getCooperationValues(),
				getPropertyValues(GraphProperty.AVERAGE_DEGREE));
		CorrelationDataset stdDegCorrelation = new CorrelationDataset(getCooperationValues(),
				getPropertyValues(GraphProperty.DEGREE_DEVIATION));
		CorrelationDataset ccCorrelation = new CorrelationDataset(getCooperationValues(),
				getPropertyValues(GraphProperty.CLUSTERING_COEFFICIENT));

		Table table = new Table();
		System.out.println("Community Count: " + communityCount);
		table.add("Communities: " + communityCount);
		table.add("").append(CorrelationDataset.toHeadLine());
		table.add("size").append(sizeCorrelation);
		table.add("density").append(densityCorrelation);
		table.add("avg Deg").append(avgDegCorrelation);
		table.add("std Deg").append(stdDegCorrelation);
		table.add("CC").append(ccCorrelation);
		return table;
	}

}
