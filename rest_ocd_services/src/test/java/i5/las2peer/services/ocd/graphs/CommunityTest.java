package i5.las2peer.services.ocd.graphs;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.graphstream.graph.Node;


@ExtendWith(MockitoExtension.class)
public class CommunityTest {
	
	@Mock CustomGraph graph;
	@Mock Cover cover;
	
	@Mock CustomNode cn1;
	@Mock CustomNode cn2;
	@Mock CustomNode cn3;
	@Mock CustomNode cn4;
	
	@Mock Node node1;
	@Mock Node node2;
	@Mock Node node3;
	@Mock Node node4;
	
	
	@BeforeEach
	public void setUp() {
		
		when(cover.getGraph()).thenReturn(graph);
		
		when(graph.getNode(cn1)).thenReturn(node1);
		when(graph.getNode(cn2)).thenReturn(node2);
		when(graph.getNode(cn3)).thenReturn(node3);
		when(graph.getNode(cn4)).thenReturn(node4);
		
		when(graph.getCustomNode(node1)).thenReturn(cn1);
		when(graph.getCustomNode(node2)).thenReturn(cn2);
		when(graph.getCustomNode(node3)).thenReturn(cn3);
		when(graph.getCustomNode(node4)).thenReturn(cn4);
		
		when(graph.getNodeName(node1)).thenReturn("0");
		when(graph.getNodeName(node2)).thenReturn("1");
		when(graph.getNodeName(node3)).thenReturn("2");
		when(graph.getNodeName(node4)).thenReturn("3");
				
	}
	
	@Test
	public void getMemberIndicesTest() {
		
		List<Integer> result;		
		Community community; 
		
		community = new Community(cover);	
		result = community.getMemberIndices();
		assertNotNull(result);
		assertEquals(0, result.size());	
		
		community = new Community(cover);
		community.setBelongingFactor(node2, 0.4);
		community.setBelongingFactor(node4, 0.7);
		result = community.getMemberIndices();
		assertNotNull(result);
		assertEquals(2, result.size());
		assertFalse(result.contains(0));
		assertTrue(result.contains(1));
		assertFalse(result.contains(2));
		assertTrue(result.contains(3));
		
		community = new Community(cover);			
		community.setBelongingFactor(node1, 0.2);
		community.setBelongingFactor(node2, 0.4);
		community.setBelongingFactor(node3, 0.1);
		community.setBelongingFactor(node4, 0.7);
		result = community.getMemberIndices();
		assertNotNull(result);
		assertEquals(4, result.size());
		assertTrue(result.contains(0));
		assertTrue(result.contains(1));
		assertTrue(result.contains(2));
		assertTrue(result.contains(3));
		
		community = new Community(cover);
		community.setBelongingFactor(node1, 0.0);
		community.setBelongingFactor(node2, 0.3);
		result = community.getMemberIndices();
		assertNotNull(result);
		assertEquals(1, result.size());
		assertFalse(result.contains(0));
		assertTrue(result.contains(1));
		assertFalse(result.contains(2));
		assertFalse(result.contains(3));
		
		
	}
	
}
