package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

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

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A log representation for an OcdMetric execution.
 * @author Sebastian
 *
 */
@Entity
@IdClass(OcdMetricLogId.class)
public class OcdMetricLog {

	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String typeColumnName = "TYPE";
	private static final String valueColumnName = "VALUE";
	public static final String coverIdColumnName = "COVER_ID";
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";
	public static final String statusIdColumnName = "STATUS";
	
	public static final String coverKeyColumnName = "COVER_KEY";
	private static final String parameterColumnName = "PARAMETER";
	public static final String collectionName = "ocdmetriclog";
	/*
	 * Field names
	 */
	public static final String STATUS_ID_FIELD_NAME = "statusId";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	/**
	 * System generated persistence key.
	 */
	private String key;
	/**
	 * The cover the metric was run on.
	 */
	@Id
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name=graphIdColumnName, referencedColumnName=Cover.graphIdColumnName),
		@JoinColumn(name=graphUserColumnName, referencedColumnName=Cover.graphUserColumnName),
		@JoinColumn(name=coverIdColumnName, referencedColumnName=Cover.idColumnName)
	})
	private Cover cover;
	/**
	 * Parameters used by the metric.
	 */
	@ElementCollection
	private Map<String, String> parameters;
	/**
	 * The calculated metric value.
	 */
	@Column(name = valueColumnName)
	private double value;
	/**
	 * Id of the metrics corresponding ocd metric type.
	 */
	@Column(name = typeColumnName)
	private int typeId;
	/**
	 * The status of the corresponding execution.
	 */
	@Column(name = statusIdColumnName)
	private int statusId = ExecutionStatus.WAITING.getId();
	
	/**
	 * Creates a new instance.
	 * Only provided for persistence. 
	 */
	protected OcdMetricLog() {
	}
	
	/**
	 * Creates a new instance.
	 * @param type The type of the corresponding metric.
	 * @param value The value calculated by the metric.
	 * @param parameters The parameters used by the metric.
	 * @param cover The cover the metric was run on.
	 */
	public OcdMetricLog(OcdMetricType type, double value, Map<String, String> parameters, Cover cover) {
		if(type != null) {
			this.typeId = type.getId();
		}
		else {
			this.typeId = OcdMetricType.UNDEFINED.getId();
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

	/**
	 * Returns the log id.
	 * @return The id.
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Returns the log key.
	 * @return The key.
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Returns the type of the corresponding metric.
	 * @return The type.
	 */
	public OcdMetricType getType() {
		return OcdMetricType.lookupType(this.typeId);
	}

	/**
	 * Returns the parameters used by the corresponding metric.
	 * @return A mapping from each parameter name to the corresponding value in String format.
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Returns the value calculated by the corresponding metric.
	 * @return The value.
	 */
	public double getValue() {
		return value;
	}
	
	/**
	 * Sets the corresponding metric value.
	 * @param value The value.
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * Returns the execution status of the corresponding metric.
	 * @return The status.
	 */
	public ExecutionStatus getStatus() {
		return ExecutionStatus.lookupStatus(statusId);
	}
	
	/**
	 * Sets the execution status of the corresponding metric.
	 * @param status The status.
	 */
	public void setStatus(ExecutionStatus status) {
		this.statusId = status.getId();
	}
	
	/**
	 * Returns the cover the corresponding metric was run on.
	 * @return The cover.
	 */
	public Cover getCover() {
		return cover;
	}
	
	//persistence functions
	public void persist( ArangoDatabase db, DocumentCreateOptions opt) {
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(typeColumnName, this.typeId);
		bd.addAttribute(statusIdColumnName, this.statusId);
		bd.addAttribute(valueColumnName, this.value);
		bd.addAttribute(parameterColumnName, this.parameters); //TODO 
		bd.addAttribute(coverKeyColumnName, this.cover.getKey());
		
		collection.insertDocument(bd, opt);
		this.key = bd.getKey();
	}
	
	public static OcdMetricLog load(String key, Cover cover, ArangoDatabase db, DocumentReadOptions opt) {
		OcdMetricLog oml = new OcdMetricLog();
		ArangoCollection collection = db.collection(collectionName);
		
		BaseDocument bd = collection.getDocument(key, BaseDocument.class, opt);
		if (bd != null) {
			ObjectMapper om = new ObjectMapper();
			Object objParameter = bd.getAttribute(parameterColumnName);
			String valueString = bd.getAttribute(valueColumnName).toString();
			String typeIdString = bd.getAttribute(typeColumnName).toString();
			String statusIdString = bd.getAttribute(statusIdColumnName).toString();	

			oml.cover = cover;
			if (objParameter != null) {
				oml.parameters = om.convertValue(objParameter, Map.class);
			}
			oml.value = Double.parseDouble(valueString);
			oml.typeId = Integer.parseInt(typeIdString);
			oml.statusId = Integer.parseInt(statusIdString);
			oml.key = key;
		}	
		else {
			System.out.println("empty OcdMetricLog document");
		}
		return oml;
	}
	

	public String String() {
		String n = System.getProperty("line.separator");
		String ret = "OcdMetricLog: " + n;
		ret += "Key :           " + this.key + n;
		if(this.cover != null) {ret += "cover attribut existiert";}
		ret += "value :         " + this.value +n;
		ret += "typeId :        " + this.typeId + n; 
		ret += "statusId :      " + this.statusId + n;
		ret += "parameters :    " + this.parameters.toString() + n;
		
		return ret;
	}
	
}
