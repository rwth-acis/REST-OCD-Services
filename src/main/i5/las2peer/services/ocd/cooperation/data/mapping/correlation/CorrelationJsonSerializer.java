package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * JSON Serializer for CorrelationDataset objects.
 */
public class CorrelationJsonSerializer extends StdSerializer<CorrelationDataset> {

	private static final long serialVersionUID = 1L;
	
	public CorrelationJsonSerializer() {
		this(null);
	}
	
	protected CorrelationJsonSerializer(Class<CorrelationDataset> t) {
		super(t);
	}

	@Override
	public void serialize(CorrelationDataset correlationDataset, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		
		generator.writeStartObject();
		for(Correlation correlation :Correlation.values()) {
			generator.writeNumberField(correlation.getName(), correlationDataset.get(correlation.getId()));
		}		
		generator.writeEndObject();		
	}

}
