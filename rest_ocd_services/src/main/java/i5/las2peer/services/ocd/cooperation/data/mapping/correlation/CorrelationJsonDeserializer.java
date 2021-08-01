package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CorrelationJsonDeserializer extends StdDeserializer<CorrelationDataset> {

	private static final long serialVersionUID = 1L;

	public CorrelationJsonDeserializer() {
		this(null);

	}

	protected CorrelationJsonDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public CorrelationDataset deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		JsonNode node = parser.getCodec().readTree(parser);
		
		return null;
	}

}
