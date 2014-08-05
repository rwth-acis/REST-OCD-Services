package i5.las2peer.services.servicePackage.adapters.coverInput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.Adapters;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.Reader;
import java.util.List;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;


public class LabeledMembershipMatrixInputAdapter extends AbstractCoverInputAdapter {

	public LabeledMembershipMatrixInputAdapter(Reader reader) {
		this.setReader(reader);
	}
	
	@Override
	public Cover readCover(CustomGraph graph) throws AdapterException {
		try {
			List<String> line = Adapters.readLine(reader);
			Matrix memberships = new CCSMatrix(graph.nodeCount(), line.size() - 1);
			int nodeIndex = 0;
			double belongingFactor;
			while(line.size() > 0) {
				for(int i=1; i<line.size(); i++) {
					belongingFactor = Double.parseDouble(line.get(i));
					memberships.set(nodeIndex, i-1, belongingFactor);
				}
				nodeIndex++;
				line = Adapters.readLine(reader);
			}
			return new Cover(graph, memberships);
		}
		catch (Exception e) {
			throw new AdapterException(e);
		}
		finally {
			try {
				reader.close();
			}
			catch (Exception e) {
			}
		}
	}

}
