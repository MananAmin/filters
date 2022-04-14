package org.manan;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

//Reference : https://github.com/MagnusS/Java-BloomFilter/blob/master/src/com/skjegstad/utils/BloomFilter.java
//Reference : https://www.baeldung.com/sha-256-hashing-java
//Reference : https://github.com/google/guava/blob/48b02ce53ca35b0db1e3def8f00d257cd4adf68c/android/guava/src/com/google/common/hash/BloomFilterStrategies.java

public class BloomFilter {
	private BitSet bits;
	private int elements;
	private int k;
	private int curElements;
	private int bitsPerElements;
	private int bitsSize;
	static final Charset charset = StandardCharsets.UTF_8;
	static MessageDigest digest;

	public int getBitsSize() {
		return bitsSize;
	}

	static{
		try {
			 digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public BloomFilter(int bitsPerElements, int elements, int hashFunctions) {
		this.bitsPerElements = bitsPerElements;
		this.k = hashFunctions;
		this.elements = elements;
		this.bits = new BitSet(bitsPerElements*elements);
		this.bitsSize = bitsPerElements*elements;

	}
	public BloomFilter(int bitsSize, int hashFunctions) {
		this.k = hashFunctions;
		this.bits = new BitSet(bitsSize);
		this.bitsSize = bitsSize;

	}
	public BloomFilter(double bitsPerElements, int elements, int hashFunctions) {
		this.k = hashFunctions;
		this.elements = elements;
		this.bits = new BitSet((int)(bitsPerElements*elements));
		this.bitsSize =(int)(bitsPerElements*elements);

	}
	public BloomFilter(double errorRate, int elements) {
		this((Math.ceil(-(Math.log(errorRate) / Math.log(2))) / Math.log(2)), // c = k / ln(2)
				elements,
				(int)Math.ceil(-(Math.log(errorRate) / Math.log(2)))); // k = ceil(-log_2(false prob.))
	}

	// Ref: Less Hashing, Same Performance: Building a Better Bloom Filter
	public int[] generateHash(String key,int k){
		 byte[] bytes = key.getBytes(charset);
		 int[] output = new int[k];

		long hash64 = ByteBuffer.wrap(digest.digest(bytes)).getLong();
		int hash1 = (int) hash64;
		int hash2 = (int) (hash64 >>> 32);

		for (int i = 1; i <= k; i++) {
			int combinedHash = hash1 + (i * hash2);
			if (combinedHash < 0) {
				combinedHash = ~combinedHash;
			}
			output[i-1] =combinedHash;
		}
		return output;
	}

	public double getErrorRate() {
		// (1 - e^(-k * n / m)) ^ k
		return Math.pow((1 - Math.exp(-k * (double) this.elements
											  / (double) this.bitsSize)), k);
	}

	public void add(String input){
		int[] hash = generateHash(input,this.k);
		for(int h:hash){
			this.bits.set(Math.abs(h%this.bitsSize),true);
		}
		this.curElements++;
	}

	public void add(int input){
		this.add(Integer.toString(input));
	}

	public boolean isMember(String input){
		int[] hash = generateHash(input,this.k);
		for(int h:hash){
			if(!this.bits.get(Math.abs(h%this.bitsSize))){
				return false;
			}
		}
		return true;
	}

	public boolean isMember(int input){
		return this.isMember(Integer.toString(input));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (this.bits!=null ? this.bits.hashCode():0);
		hash = 31 * hash + this.elements;
		hash = 31 * hash + this.bitsSize;
		hash = 31 * hash + this.k;
		return hash;
	}

}
