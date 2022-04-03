package org.manan;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//This class perform test on bloom filter
// for 100,000 elements 1o times and take average of all
public class BloomTest {

	public static double performTest(BloomFilter bf,int elements){
		List<String> array = new ArrayList<String>();

		for (int i = 0; i < elements; i++)
			array.add(UUID.randomUUID().toString());

		for(String str:array)
			bf.add(str);

		int count =0;
		double wrongCount = 0;

		for(String str:array)
			if(!bf.isMember(str)){
				count+=1;
			}

		for (int i = 0; i < elements; i++)
			if(bf.isMember(UUID.randomUUID().toString())){
				wrongCount+=1;
			}

		return wrongCount/elements;
	}

	public static void main(String[] args) {

		double ma =0;
		for(int i=0; i<10;i++){
			BloomFilter bf = new BloomFilter(10,100000,3);
			ma+=performTest(bf,100000);
		}
		double errorRate = ma/10;

		BloomFilter bf = new BloomFilter(10,100000,3);

		System.out.println("Expected error rate: "+bf.getErrorRate());
		System.out.println("Error rate of bloom Filter is  "+ errorRate);

	}
}
