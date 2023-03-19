package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomNode;
import i5.las2peer.services.ocd.graphs.CustomNodeMeta;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class MetaJsonNodeOutputAdapter extends AbstractNodeMetaOutputAdapter {

    @Override
    //TODO: Check if color values still need to be multiplied with 255
    public void writeNodeMeta(CustomNodeMeta nodeMeta) throws AdapterException {
        JSONObject obj = new JSONObject();
        // Document doc = builder.newDocument();

        obj.put("_key", nodeMeta.getKey());
        obj.put("name", nodeMeta.getName());
        obj.put("graphKey", nodeMeta.getGraphKey());
        obj.put("extraInfo", nodeMeta.getExtraInfo());

        try {
            StringWriter out = new StringWriter();
            obj.writeJSONString(out);

            writer.write(out.toString());
        } catch (IOException e) {
            throw new AdapterException(e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //TODO: Check what to throw here
            }
        }
    }

}
