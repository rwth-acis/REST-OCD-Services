package i5.las2peer.services.ocd.adapters.simulationOutput;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationAbstract;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesParameters;

public class MetaXmlSimulationOutputAdapter extends AbstractSimulationOutputAdapter {

	@Override
	public void write(SimulationAbstract simulation) throws AdapterException {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement("Simulation");
			doc.appendChild(root);

			writeElementsAbstract(simulation, doc, root);
			this.transform(doc);

		} catch (Exception e) {
			throw new AdapterException(e);
		}
	}

	public void write(SimulationSeries simulation) throws AdapterException {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement("Simulation");
			doc.appendChild(root);
			
			writeElementsAbstract(simulation, doc, root);
			writeElementsSeries(simulation, doc, root);
			this.transform(doc);

		} catch (Exception e) {
			throw new AdapterException(e);
		}
	}

	private void writeElementsAbstract(SimulationAbstract simulation, Document doc, Element root) {

		Element eltId = doc.createElement("Id");
		eltId.appendChild(doc.createTextNode(Long.toString(simulation.getId())));
		root.appendChild(eltId);

		Element eltName = doc.createElement("Name");
		eltName.appendChild(doc.createTextNode(simulation.getName()));
		root.appendChild(eltName);

		Element eltCooperation = doc.createElement("Cooperativity");
		eltCooperation.appendChild(doc.createTextNode(String.valueOf(simulation.averageCooperationValue())));
		root.appendChild(eltCooperation);

		Element eltPayoff = doc.createElement("Wealth");
		eltCooperation.appendChild(doc.createTextNode(String.valueOf(simulation.averagePayoffValue())));
		root.appendChild(eltPayoff);

		Element eltGraph = doc.createElement("Graph");
		Element eltGraphId = doc.createElement("Id");
		eltGraphId.appendChild(doc.createTextNode(String.valueOf(simulation.getNetwork().getPersistenceId())));
		eltGraph.appendChild(eltGraphId);
		Element eltGraphName = doc.createElement("Name");
		eltGraphName.appendChild(doc.createTextNode(String.valueOf(simulation.getNetwork().getName())));
		eltGraph.appendChild(eltGraphName);
		root.appendChild(eltGraph);
	}

	private void writeElementsSeries(SimulationSeries simulation, Document doc, Element root) {

		Element eltGraph = doc.createElement("Parameters");
		SimulationSeriesParameters parameters = simulation.getParameters();
		Element eltGraphId = doc.createElement("Game");
		eltGraphId.appendChild(doc.createTextNode(String.valueOf(parameters.getGame().humanRead())));
		eltGraph.appendChild(eltGraphId);
		Element eltGraphName = doc.createElement("Dynamic");
		eltGraphName.appendChild(doc.createTextNode(String.valueOf(parameters.getDynamic().humanRead())));
		eltGraph.appendChild(eltGraphName);
		root.appendChild(eltGraph);

	}

	private void transform(Document doc) throws TransformerException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource domSource = new DOMSource(doc);
		StreamResult streamResult = new StreamResult(this.writer);
		transformer.transform(domSource, streamResult);
	}

}
