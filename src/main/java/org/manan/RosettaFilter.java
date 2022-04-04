package org.manan;

// Ref: Rosetta: A Robust Space-Time Optimized Range Filter for Key-Value Stores

import java.util.ArrayList;
import java.util.List;

public class RosettaFilter {
	private BloomFilter bf[];
	private int R;   //R Maximum range query size
	private int keyLength;
	private int depth;

	public RosettaFilter(int keyLength, int elements, int R, int version) {
		this.keyLength = keyLength;
		this.R = R;
		bf = new BloomFilter[keyLength];

		if (version == 1) {
			//vanilla version Naive Approach, not space efficient
			for (int i = 0; i < keyLength; i++) {
				bf[i] = new BloomFilter(10, elements, 3);
			}
		} else if (version == 2) {
			// Removing half of top bloom filters
			this.depth = keyLength / 2;
			for (int i = 0; i < keyLength; i++) {
				if (i >= depth)
					bf[i] = new BloomFilter(10, elements, 3);
			}
		} else if (version == 3) {
			//  Optimized Allocation of Memory Across Levels.
			this.depth = keyLength/2;
			int M = 200000;
			int total = 0;
			List<Integer> array = new ArrayList<Integer>();

			for (int i = 0; i < keyLength; i++) {
				if (i >= depth) {
					int r = keyLength - i - 1;

					double c = c(r, R, M, elements);
					double g = g(r, R);

					int bits = (int) (((-1) * elements / Math.pow(Math.log(2), 2.0)) * Math.log(c / g));
					total += bits;
					if(bits>0){
						array.add(bits);
					}
				}
			}
			this.depth = keyLength-array.size();
			int j =0;
			for(int i :array){
				if(j==0){
					bf[keyLength-j-1] = new BloomFilter(100000,3);
				}
				else
					bf[keyLength-j-1] = new BloomFilter(i,3);

				j++;
			}
//			System.out.println("total : "+total);
		}

	 else if(version==4){
		// FPR Equilibrium Across Nodes.
		this.depth = (keyLength / 2);
		for (int i = 0; i < keyLength; i++) {
			if (i >= depth) {
				double fpr = 0.01;
				double inner_fpr = 1 / (2 - fpr);
				if (i == keyLength - 1)
					bf[i] = new BloomFilter(fpr, elements);
				else
					bf[i] = new BloomFilter(inner_fpr, elements);
			}
		}
	}

}


	public static double g(int r,int R){
		double result =0;
		for(int i=0;i<= Math.floor(log(R,2))-r ;i++){
			double temp;
			if(r+i> Math.floor(log2nlz(R+1)))
				temp=0;
			else if(r+i< Math.floor(log2nlz(R+1)))
				temp=1;
			else
				temp=(R - (1<<(r+i)) +1 )/(1<<(r+i));
			result+=temp;
		}
		return result;
	}

	public static double c(int r,int R,int M,int ele) {
		double first=1;
		for (int i = 0; i <= Math.floor(log(R,2) ) ; i++) {
			first*=g(r,R);
		}
		double second = Math.pow(first,1/(Math.floor(log(R,2))));
		double third = Math.pow(Math.E, ((-1)*(M)/ele)* (Math.pow(Math.log(2),2)/(Math.floor(log(R,2)))));
		return second*third;
	}

	static int log(int x, int base)
	{
		return (int) (Math.log(x) / Math.log(base));
	}
	public static int log2nlz( int bits )
	{
		if(bits == 0)
			return 0; // or throw exception
		return 31 - Integer.numberOfLeadingZeros( bits );
	}

	public void insert(int input){
		int length = log2nlz(input)+1;
		if(length>keyLength){
			throw new IllegalArgumentException("Key size is not supported");
		}
		int cur =keyLength;
		int value=input;
		while(cur>0 & value>0){
			bf[cur-1].add(value);
			value= value/2;  // as value is int => Math.floor(value/2)
			cur-=1;
		}
	}

	public void insert2(int input){
		int length = log2nlz(input)+1;
		if(length>keyLength){
			throw new IllegalArgumentException("Key size is not supported");
		}
		int cur =keyLength;
		int value=input;

		while(cur>this.depth & value>0){
			bf[cur-1].add(value);
			value= value/2;  // as value is int => Math.floor(value/2)
			cur-=1;
		}
	}

	public boolean pointQuery(int input){
		int length = log2nlz(input);
		if(length>keyLength){
			throw new IllegalArgumentException("Key size is not supported");
		}
		return bf[length-1].isMember(input);
	}

	public boolean range(int l,int r){
		return this.rangeQuery(l,r,0,1);
	}

	private boolean rangeQuery(int l, int h,int p,int level){
		if(p>h | ( p+(1<<(keyLength-level+1))-1)<l){
			// P is not in the range
			return false;
		}
		if(p>=l | p+(1<<(keyLength-level+1))-1 <=h){
			// P is in the range
			return this.doubt(p,level);
		}
		if(this.rangeQuery(l,h,p,level+1)){
			return true;
		}
		return this.rangeQuery(l,h,p+ (1<<(keyLength-level)),level+1);
	}

	private boolean doubt(int p, int level) {
//		System.out.println("Actual p "+(p>>(keyLength-level))+" at level "+(level-1));
		if(!bf[level-1].isMember(p>>(keyLength-level))) {
			return false;
		}
		if(level>=keyLength)
			return true;
		if(this.doubt(p,level+1))
			return true;
		return this.doubt(p+(1<<(keyLength-level-1)),level+1);
	}

	public boolean range2(int l,int r){
		return this.rangeQuery2(l,r,0,1);
	}

	private boolean rangeQuery2(int l, int h,int p,int level){
		if(p>h | ( p+(1<<(keyLength-level+1))-1)<l){
			// P is not in the range
			return false;
		}
		if(p>=l | p+(1<<(keyLength-level+1))-1 <=h){
			// P is in the range
			return this.doubt2(p,level);
		}
		if(this.rangeQuery2(l,h,p,level+1)){
			return true;
		}
		return this.rangeQuery2(l,h,p+ (1<<(keyLength-level)),level+1);
	}

	private boolean doubt2(int p, int level) {
//		System.out.println("Actual p "+(p>>(keyLength-level))+" at level "+(level-1));
		if(level<=depth)
			return true;
		if(!bf[level-1].isMember(p>>(keyLength-level))){
			return false;
		}
		if(level>=keyLength)
			return true;
		if(this.doubt2(p,level+1))
			return true;
		return this.doubt2(p+(1<<(keyLength-level-1)),level+1);
	}

	public static void main(String[] args) {
		int[] elements= {1,4,7,11};
		RosettaFilter rf= new RosettaFilter(4,6,5,1);
		for(int value:elements){
			rf.insert(value);
		}
		System.out.println(rf.range(8,12));
	}
}