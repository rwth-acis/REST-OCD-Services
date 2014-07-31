package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Cover;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

@Entity
@IdClass(MetricLogId.class)
public class MetricLog {

	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String typeColumnName = "TYPE";
	private static final String valueColumnName = "VALUE";
	public static final String coverIdColumnName = "COVER_ID";
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	@Id
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name=graphIdColumnName, referencedColumnName=Cover.graphIdColumnName),
		@JoinColumn(name=graphUserColumnName, referencedColumnName=Cover.graphUserColumnName),
		@JoinColumn(name=coverIdColumnName, referencedColumnName=Cover.idColumnName)
	})
	private Cover cover;
	@ElementCollection
	private Map<String, String> parameters;
	@Column(name = valueColumnName)
	private double value;
	@Column(name = typeColumnName)
	private int typeId;
	/*
	 * Only provided for persistence. 
	 */
	protected MetricLog() {
	}
	
	public MetricLog(MetricType type, double value, Map<String, String> parameters, Cover cover) {
		if(type != null) {
			this.typeId = type.getId();
		}
		else {
			this.typeId = MetricType.UNDEFINED.getId();
		}
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
		this.value = value;
		this.cover = cover;
	}

	public long getId() {
		return id;
	}

	public MetricType getType() {
		return MetricType.lookupType(this.typeId);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public double getValue() {
		return value;
	}
	
}
