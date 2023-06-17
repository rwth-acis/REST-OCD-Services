package i5.las2peer.services.ocd.graphs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
public class EdgeSerializer extends JsonSerializer<DescriptiveVisualization.Edge> {
    @Override
    public void serialize(DescriptiveVisualization.Edge edge, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("source", edge.source);
        jsonGenerator.writeNumberField("target", edge.target);
        jsonGenerator.writeArrayFieldStart("numValue");
        for (Double num : edge.numValue) {
            jsonGenerator.writeNumber(num);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeArrayFieldStart("stringValue");
        for (String str : edge.stringValue) {
            jsonGenerator.writeString(str);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeArrayFieldStart("rgbValue");
        for (int[] rgbArray : edge.rgbValue) {
            jsonGenerator.writeArray(rgbArray, 0, rgbArray.length);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}