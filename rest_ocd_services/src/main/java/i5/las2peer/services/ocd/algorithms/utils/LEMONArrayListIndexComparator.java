package i5.las2peer.services.ocd.algorithms.utils;

import java.util.ArrayList;
import java.util.Comparator;


public class LEMONArrayListIndexComparator implements Comparator<Integer>
{
    private final ArrayList<Double> arrayList;

    public LEMONArrayListIndexComparator(ArrayList<Double> arrayList)
    {
        this.arrayList = arrayList;
    }

    public ArrayList<Integer> createIndexArrayList()
    {
    	ArrayList<Integer> indexes = new ArrayList<Integer>(arrayList.size());
        for (int i = 0; i < arrayList.size(); i++)
        {
            indexes.add(i, i); // Autoboxing
        }
        return indexes;
    }

    @Override
    public int compare(Integer index1, Integer index2)
    {
    	int compRes = arrayList.get(index1).compareTo(arrayList.get(index2));
         // Autounbox from Integer to int to use as array indexes
    	if(compRes < 0)
    	{
    		return 1;
    	}
    	else if(compRes == 0)
    	{
    		return 0;
    	}
    	else if(compRes > 0)
    	{
    		return -1;
    	}
    	System.out.println("SHOULD NOT HAPPEN" + arrayList.get(index1) + " " + arrayList.get(index2));
    	return -1;
    }
}