package i5.las2peer.services.ocd.graphs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class NodeSerializer  extends JsonSerializer<DescriptiveVisualization.Node> {
    @Override
    public void serialize(DescriptiveVisualization.Node node, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", node.id);
        jsonGenerator.writeStringField("label", String.valueOf(node.label));
        jsonGenerator.writeNumberField("degree", node.degree);
        jsonGenerator.writeArrayFieldStart("numValue");
        for (Double num : node.numValue) {
            jsonGenerator.writeNumber(num);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeArrayFieldStart("stringValue");
        for (String str : node.stringValue) {
            jsonGenerator.writeString(str);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeArrayFieldStart("rgbValue");
        for (int[] rgbArray : node.rgbValue) {
            jsonGenerator.writeArray(rgbArray, 0, rgbArray.length);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
