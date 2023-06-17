package i5.las2peer.services.ocd.graphs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class JsonDataSerializer extends JsonSerializer<DescriptiveVisualization.JsonData> {
    @Override
    public void serialize(DescriptiveVisualization.JsonData jsonData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("_key", String.valueOf(jsonData._key));
        jsonGenerator.writeArrayFieldStart("shortDescription");
        for (String str : jsonData.shortDescription) {
            jsonGenerator.writeString(str);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeArrayFieldStart("detailedDescription");
        for (String str : jsonData.detailedDescription) {
            jsonGenerator.writeString(str);
        }
        jsonGenerator.writeEndArray();
        ObjectMapper objectMapper = new ObjectMapper();
        jsonGenerator.writeFieldName("nodes");
        objectMapper.writeValue(jsonGenerator, jsonData.nodes); // Serialize nodes using NodeSerializer
        jsonGenerator.writeFieldName("edges");
        objectMapper.writeValue(jsonGenerator, jsonData.edges); // Serialize edges using EdgeSerializer
        jsonGenerator.writeEndObject();
    }
}
