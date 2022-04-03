package org.manan;

// Ref: Rosetta: A Robust Space-Time Optimized Range Filter for Key-Value Stores

import java.lang.reflect.Array;
import java.util.*;

public class RosettaFilter {
	private BloomFilter bf[];
	private int R;   //R Maximum range query size
	private int keyLength;
	private int version;

	public RosettaFilter(int keyLength,int elements,int R,int version) {
		this.keyLength = keyLength;
		this.R = R;
		this.version=version;
		bf = new BloomFilter[keyLength];

		if(version==1){
			for(int i=0;i<keyLength;i++){
				bf[i] = new BloomFilter(10,elements,3);
			}
		}
		else if(version==2){

		}



	}

	public void insert(String key){
		this.insert(Integer.parseInt(key));
	}

	public static int log2nlz( int bits )
	{
		if( bits == 0 )
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

	public boolean pointQuery(String key){
		return this.pointQuery(Integer.parseInt(key));
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
//		System.out.println("checking p "+p+" at level "+(level));
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

	public static void main(String[] args) {
		int[] elements= {1,4,7,11};
//		int[] elements= {8};

		RosettaFilter rf= new RosettaFilter(4,6,5,1);
		for(int value:elements){
			rf.insert(value);
		}

		System.out.println(rf.range(17,27));

	}

}
