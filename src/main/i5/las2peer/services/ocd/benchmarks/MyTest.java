package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.graphs.Cover;

public class MyTest {
	public static void main(String [ ] args) throws OcdBenchmarkException, InterruptedException
	{

	LfrBenchmark myLfr=new LfrBenchmark();
	Cover cover = myLfr.createGroundTruthCover();
	System.out.println(cover.getName());
	System.out.println(cover.getMemberships());
	}

}
