package org.manan;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.BitmapEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

// chart code reference: https://github.com/knowm/XChart/blob/develop/README.md

//This class perform test on bloom filter
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

	public static double SingleBloomFilterTest(int c,int elements, int h){
		double ma =0;
		for(int i=0; i<10;i++){
			BloomFilter bf = new BloomFilter(c,elements,h);
			ma+=performTest(bf,elements);
		}
		double errorRate = ma/10;
		return errorRate;
	}

	public static double TheoryFalsePositive(int c){
		double k = c*Math.log(2);
		double result = Math.pow((1 - Math.exp(-k/c)), k);
		return result;
	}

	public static void generateRatioVsFpr() throws IOException {
		int [] ratios = new int[] {2,3,4,5,6,7,8,9,10,11,12};
		double[] ratiosChart = new double[]{2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0};
		int [] elements = new int[] {500,2000,8000,64000,128000};
		String [] seriesName = new String[]{"m=500","m=2000","m=8000","m=64000","m=128000"};
		HashMap<Integer,double[]> fprs = new HashMap<Integer,double[]>();

		for (int element:elements) {
			double[] results = new double[ratios.length];
			int count=0;
			for(int ratio: ratios) {
				double fpr= SingleBloomFilterTest(ratio,element,3);
				results[count]= fpr;
				count+=1;
			}
			fprs.put(element,results) ;
		}

		// chart code reference
		final XYChart chart = new XYChartBuilder().width(800).height(600).title("n/m bits per elements ratio of Bloom Filters vs. False Positive Rate (for h=3)").xAxisTitle("n/m bits per elements").yAxisTitle("False Positive Rate").build();

		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);

		int i=0;
		for (int element:elements) {
			chart.addSeries(seriesName[i], ratiosChart, fprs.get(element));
			i+=1;
		}
		BitmapEncoder.saveBitmap(chart, "images/bloom-filter-ratio-vs-fpr-final", BitmapEncoder.BitmapFormat.PNG);
	}

	public static void generateHashesVsFpr() throws IOException {
		int [] hashFunctions = new int[] {1,2,3,4,5,6,7,8,9,10,11,12};
		double[] hashFunctionsChart = new double[]{1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0};
		int [] ratios = new int[] {6,8,10,12,14};
		String [] seriesName = new String[]{"m/n=6","m/n=8","m/n=10","m/n=12","m/n=14"};
		HashMap<Integer,double[]> fprs = new HashMap<Integer,double[]>();
		int element = 100000;
		for (int ratio:ratios) {
			double[] results = new double[hashFunctions.length];
			int count=0;
			for(int hash: hashFunctions) {
				double fpr= SingleBloomFilterTest(ratio,element,hash);
				results[count]= fpr;
				count+=1;
			}
			fprs.put(ratio,results) ;
		}

		final XYChart chart = new XYChartBuilder().width(800).height(600).title("Number of Hash Functions vs. False Positive Rate for m=100000").xAxisTitle("Number of Hash Functions").yAxisTitle("False Positive Rate").build();

		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);

		int i=0;
		for (int ratio:ratios) {
			chart.addSeries(seriesName[i], hashFunctionsChart, fprs.get(ratio));
			i+=1;
		}

		BitmapEncoder.saveBitmap(chart, "images/bloom-filter-hash-vs-fpr-final", BitmapEncoder.BitmapFormat.PNG);

	}

	public static void main(String[] args) throws IOException {
		generateRatioVsFpr();
		generateHashesVsFpr();
	}
}
