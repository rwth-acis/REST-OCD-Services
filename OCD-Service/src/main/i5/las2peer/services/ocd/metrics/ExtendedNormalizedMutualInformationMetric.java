package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.utils.NonZeroEntriesVectorProcedure;

import java.util.HashMap;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;

public class ExtendedNormalizedMutualInformationMetric implements KnowledgeDrivenMeasure {

	@Override
	public void setParameters(Map<String, String> parameters) {
	}

	@Override
	public Map<String, String> getParameters() {
		return new HashMap<String, String>();
	}
	
	@Override
	public double measure(Cover cover, Cover groundTruth) throws InterruptedException {
		double metricValue = 1;
		Map<Integer, Integer> coverCommunitySizes = determineCommunitySizes(cover);
		Map<Integer, Integer> groundTruthCommunitySizes = determineCommunitySizes(groundTruth);
		metricValue -= 0.5 * calculateArbitraryConditionalEntropy(cover, groundTruth, coverCommunitySizes, groundTruthCommunitySizes);
		metricValue -= 0.5 * calculateArbitraryConditionalEntropy(groundTruth, cover, groundTruthCommunitySizes, coverCommunitySizes);
		return metricValue;
	}

	/*
	 * Determines the uncertainty about the membership of a node in an arbitrary community of cover 1,
	 * when the community memberships of the node are known for cover 2.
	 * @param cover1 The cover 1.
	 * @param cover2 The cover 2.
	 * @return The uncertainty calculated as the normalized conditional entropy.
	 */
	private double calculateArbitraryConditionalEntropy(Cover cover1, Cover cover2,
			Map<Integer, Integer> cover1CommunitySizes, Map<Integer, Integer> cover2CommunitySizes) throws InterruptedException {
		Matrix cover1Memberships = cover1.getMemberships();
		Matrix cover2Memberships = cover2.getMemberships();
		double minParticularConditionalEntropy;
		double currentParticularConditionalEntropy;
		double arbitraryConditionalEntropy = 0;
		double communityEntropy;
		double probability_x0;
		double probability_x1;
		int nodeCount = cover1.getGraph().nodeCount();
		for(int i=0; i<cover1.communityCount(); i++) {
			minParticularConditionalEntropy = Double.POSITIVE_INFINITY;
			for(int j=0; j<cover2.communityCount(); j++) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				currentParticularConditionalEntropy = calculateParticularConditionalEntropy(cover1Memberships, cover2Memberships, i, j,
						cover1CommunitySizes.get(i), cover2CommunitySizes.get(j));
				if(currentParticularConditionalEntropy < minParticularConditionalEntropy) {
					minParticularConditionalEntropy = currentParticularConditionalEntropy;
				}
			}
			if(minParticularConditionalEntropy != Double.POSITIVE_INFINITY) {
				probability_x0 = (double)(nodeCount - cover1CommunitySizes.get(i)) / (double)nodeCount;
				probability_x1 = (double)cover1CommunitySizes.get(i) / (double)nodeCount;
				communityEntropy = 0;
				if(probability_x0 > 0) {
					communityEntropy -= probability_x0 * Math.log(probability_x0) / Math.log(2);
				}
				if(probability_x1 > 0) {
					communityEntropy -= probability_x1 * Math.log(probability_x1) / Math.log(2);
				}
				if(communityEntropy > 0) {
					minParticularConditionalEntropy /= communityEntropy;
				}
				else {
					minParticularConditionalEntropy = 1;
				}
			}
			else {
				minParticularConditionalEntropy = 1;
			}
			arbitraryConditionalEntropy += minParticularConditionalEntropy;
		}
		return arbitraryConditionalEntropy / (double)cover1.communityCount();
	}
	
	/*
	 * Determines the uncertainty about the membership of a node in a particular community of cover 1, when it is known
	 * that the node is a member of a particular community in cover 2.
	 * @param cover1Memberships The community memberships of cover 1.
	 * @param cover2Memberships The community memberships of cover 2.
	 * @param cover1CommunityIndex The index of the particular community of cover 1.
	 * @param cover2CommunityIndex The index of the particular community of cover 2.
	 * @param cover1CommunitySize The size of the particular community of cover 1.
	 * @param cover2CommunitySize The size of the particular community of cover 2.
	 * @return The uncertainty calculated as the conditional entropy, if eligible according to the
	 * constraints of the definition of the NMI. Else positive infinity is returned.
	 */
	private double calculateParticularConditionalEntropy(Matrix cover1Memberships, Matrix cover2Memberships, int cover1CommunityIndex, int cover2CommunityIndex,
			int cover1CommunitySize, int cover2CommunitySize) {
		Vector cover1CommunityMemberships = cover1Memberships.getColumn(cover1CommunityIndex);
		Vector cover2CommunityMemberships = cover2Memberships.getColumn(cover2CommunityIndex);
		NonZeroEntriesVectorProcedure procedure = new NonZeroEntriesVectorProcedure();
		/*
		 * An entry is different than 0 iff the corresponding entries of both communities are different than 0.
		 */
		Vector sharedMemberships = cover1CommunityMemberships.hadamardProduct(cover2CommunityMemberships);
		sharedMemberships.eachNonZero(procedure);
		int sharedMembersCount = procedure.getNonZeroEntryCount();
		procedure = new NonZeroEntriesVectorProcedure();
		/*
		 * An entry is different than 0 iff the corresponding entries of both communities are different than 0.
		 * Positive memberships are assumed.
		 */
		Vector joinedMemberships = cover1CommunityMemberships.add(cover2CommunityMemberships);
		joinedMemberships.eachNonZero(procedure);
		int joinedMembersCount = procedure.getNonZeroEntryCount();
		int nodeCount = cover1Memberships.rows();
		/*
		 * Probabilities of y
		 */
		double probability_y0 = (double)(nodeCount - cover2CommunitySize) / (double)nodeCount;
		double probability_y1 = (double)cover2CommunitySize / (double)nodeCount;
		/*
		 * Conditional probabilities of x given y
		 */
		double probability_x0_y0 = (double)( nodeCount - joinedMembersCount ) / (double)nodeCount / probability_y0;
		double probability_x1_y0 = (double)( cover1CommunitySize - sharedMembersCount ) / (double)nodeCount / probability_y0;
		double probability_x0_y1 = (double)( cover2CommunitySize - sharedMembersCount ) / (double)nodeCount / probability_y1;
		double probability_x1_y1 = (double) sharedMembersCount / (double) nodeCount / probability_y1;
		double h_x0_y0 = 0;
		if(probability_x0_y0 > 0) {
			h_x0_y0 = - probability_x0_y0 * Math.log(probability_x0_y0) / Math.log(2d);
		}
		double h_x1_y0 = 0;
		if(probability_x1_y0 > 0) {
			h_x1_y0 = - probability_x1_y0 * Math.log(probability_x1_y0) / Math.log(2d);
		}
		double h_x0_y1 = 0;
		if(probability_x0_y1 > 0) {
			h_x0_y1 = - probability_x0_y1 * Math.log(probability_x0_y1) / Math.log(2d);
		}
		double h_x1_y1 = 0;
		if(probability_x1_y1 > 0) {
			h_x1_y1 = - probability_x1_y1 * Math.log(probability_x1_y1) / Math.log(2d);
		}
		double conditionalEntropy = Double.POSITIVE_INFINITY;
		if(h_x0_y0 + h_x1_y1 >= h_x1_y0 + h_x0_y1) {
			conditionalEntropy = (h_x0_y0 + h_x1_y0) * probability_y0;
			conditionalEntropy += (h_x0_y1 + h_x1_y1) * probability_y1;
		}
		return conditionalEntropy;
	}
	
	/*
	 * Determines the community sizes of all communities of a cover. 
	 * @param cover The cover.
	 * @return A mapping from the community indices to the community sizes.
	 */
	private Map<Integer, Integer> determineCommunitySizes(Cover cover) throws InterruptedException {
		Map<Integer, Integer> communitySizes = new HashMap<Integer, Integer>();
		for(int i=0; i<cover.communityCount(); i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			communitySizes.put(i, cover.getCommunitySize(i));
		}
		return communitySizes;
	}
	
	
}
