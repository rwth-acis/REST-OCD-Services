package i5.las2peer.services.ocd.cd.data.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import i5.las2peer.services.ocd.cd.data.table.DataTable;
import i5.las2peer.services.ocd.cd.data.table.Table;
import i5.las2peer.services.ocd.cd.data.table.TableInterface;
import i5.las2peer.services.ocd.cd.simulation.game.GameType;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

public class SimulationList extends ArrayList<SimulationSeriesGroup> implements TableInterface {
	private static final long serialVersionUID = 1L;
	
	Set<String> networks;
	String name;

	public SimulationList() {

	}

	public SimulationList(String name) {
		this.name = name;
	}

	public void add(List<SimulationSeriesGroup> simulation) {
		for (SimulationSeriesGroup s : simulation) {
			this.add(s);
		}
	}

	public List<String> networks() {

		networks = new HashSet<>();
		for (SimulationSeriesGroup s : this) {
			networks.add(s.getNetwork().getName());
		}

		return new ArrayList<String>(networks);
	}

	@Override
	public Table toTable() {
		Table table = new Table();
		table.add("network").append("PD_REP").append("PD_IM").append("PD_MOR").append("PD-C_REP").append("PD-C_REP").append("PD-C_REP");
		
		
		List<String> networks = networks();
		System.out.println(networks.size());

		List<List<Double>> values = new ArrayList<>();		
		for (String n : networks) {
			table.add(n);
			List<Double> list = new ArrayList<>();
			list.add((double) -1);
			list.add((double) -1);
			list.add((double) -1);
			list.add((double) -1);
			list.add((double) -1);
			list.add((double) -1);
			values.add(list);
		}

		// PD_R PD_IM PD_MOR
		for (SimulationSeriesGroup s : this) {
			int netId = getRow(s.getNetwork().getName());
			int gId = -1;
			if (s.getSimulationSeries().get(0).getParameters().getGame().equals(GameType.PRISONERS_DILEMMA)) {
				switch (s.getSimulationSeries().get(0).getParameters().getDynamic()) {
				case REPLICATOR:
					gId = 0;
					break;
				case UNCONDITIONAL_IMITATION:
					gId = 1;
					break;
				case MORAN:
					gId = 2;
					break;
				case UNKNOWN:
					break;
				case WS_LS:
					break;
				default:
					break;
				}
			}
			if (s.getSimulationSeries().get(0).getParameters().getGame().equals(GameType.PRISONERS_DILEMMA_COST)) {
				switch (s.getSimulationSeries().get(0).getParameters().getDynamic()) {
				case REPLICATOR:
					gId = 3;
					break;
				case UNCONDITIONAL_IMITATION:
					gId = 4;
					break;
				case MORAN:
					gId = 5;
					break;
				case UNKNOWN:
					break;
				case WS_LS:
					break;
				default:
					break;
				}
			}
			values.get(netId).set(gId, s.averageCooperationValue());
		}

		for (int i = 0; i < values.size(); i++) {
			for (Double val : values.get(i)) {
				table.append(i, String.valueOf(val));
			}

		}
		return table;
	}

	public int getRow(String meta) {

		List<String> networks = networks();
		for (int i = 0; i < networks.size(); i++) {
			if (meta.equals(networks.get(i)))
				return i;
		}
		return -1;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataTable getNetworkCorrelationDataTable(GraphProperty property) {

		DataTable table = new DataTable();
		table.add("a").append("b").append("label");
		for (SimulationSeriesGroup series : this) {
			String label[] = series.getNetwork().getName().split("_");
			table.add(series.averageCooperationValue(), series.getNetwork().getProperty(property));
			if (label[0].equals("URCH") || label[0].equals("STDOCTOR")) {
				String[] nl = label[1].split("-");
				table.append(nl[0]);
			} else {
				table.append(label[0]);
			}

		}

		return table;

	}

}
