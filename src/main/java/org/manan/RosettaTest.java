package org.manan;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.util.*;

public class RosettaTest {

	public static void pointQueryPerformance(){
		List<Integer> array = new ArrayList<>();
		int elements=10000;
		int Min = 1000000000;
		int Max = 2147483646;
		for (int i = 0; i < elements; i++){
			int random =Min + (int)(Math.random() * ((Max - Min) + 1));
			array.add(random);
		}
		Set<Integer> set = new HashSet<Integer>(array);
		RosettaFilter rf= new RosettaFilter(31,elements,1000,1);

		for(Integer num:set)
			rf.insert(num);

		int count=0;
		for(Integer num:set) {
			if(!rf.pointQuery(num)) {
				count += 1;
			}
		}

		Set<Integer> set2 = new HashSet<Integer>();

		for(Integer num:set) {
			int temp=num;
			while(set.contains(temp)){
				temp+=1;
			}
			set2.add(temp);
		}

		int falseCount=0;

		for(Integer num:set2) {
			if(rf.pointQuery(num)) {
				falseCount += 1;
			}
		}

		System.out.println("point Query false positive "+(double)falseCount/elements);
	}

	public static int[][] generateRanges(int num,int min,int R,int minKey,int maxKey){
		int rangeMin = min;
		int rangeLength = R;
		int Min = minKey;
		int Max = maxKey-R;
		int[][] ranges = new int[num][2];

		for(int i=0;i<num;i++){
			int left = (int)(Math.random() * ((Max - Min) + 1));
			int length = (int)(Math.random() * ((rangeLength - rangeMin) + 1));
			int right = left+length;
			ranges[i][0] = left;
			ranges[i][1] = right;
		}
		return ranges;
	}

	public static int[][] generateFixedRanges(int num,int R,int minKey,int maxKey){
		int Min = minKey;
		int Max = maxKey-R;
		int[][] ranges = new int[num][2];

		for(int i=0;i<num;i++){
			int left = (int)(Math.random() * ((Max - Min) + 1));
			int length = R;
			int right = left+length;
			ranges[i][0] = left;
			ranges[i][1] = right;
		}
		return ranges;
	}

	private static boolean[] findAnswers(Set<Integer> set, int[][] ranges) {
		boolean[] results = new boolean[ranges.length];

		int trueRanges =0;
		int cur =0;
		for(int[] range: ranges){
			int l = range[0];
			int r = range[1];
			boolean flag =false;
			for(int i=l;i<=r;i++){
				if(set.contains(i)){
					results[cur]=true;
					cur++;
					flag=true;
					trueRanges++;
					break;
				}
			}
			if(!flag)
				cur++;
		}
//		System.out.println("True ranges ratio : "+ trueRanges+" / "+ranges.length);
		return results;
	}

	private static void rangeQueryPerformance() {

		List<Integer> array = new ArrayList<>();
		int elements=10000;
		int Min = 1000000000;
		int Max = 2147483646;

		for (int i = 0; i < elements; i++){
			int random =Min + (int)(Math.random() * ((Max - Min) + 1));
			array.add(random);
		}
		Set<Integer> set = new HashSet<Integer>(array);
		RosettaFilter rf= new RosettaFilter(31,elements,1000,1);

		for(Integer num:set)
			rf.insert(num);

		int[][] ranges = generateRanges(10000,100,1000,Min,Max);

		boolean[] results = findAnswers(set,ranges);

		int fp=0;
		int falseQueries =0;
		for(int i=0;i<ranges.length;i++){
			if(!results[i]) {
				falseQueries++;
				if (rf.range(ranges[i][0], ranges[i][1]))
					fp++;
			}
		}
		System.out.println("False Positive rate of range Queries :"+fp+" / "+falseQueries);

	}

	private static void rangeQueryPerformanceV2(int version,int elements,int R) {

		List<Integer> array = new ArrayList<>();
		int Min = 1000000000;
		int Max = 2147483646;

		for (int i = 0; i < elements; i++){
			int random =Min + (int)(Math.random() * ((Max - Min) + 1));
			array.add(random);
		}
		Set<Integer> set = new HashSet<Integer>(array);
		RosettaFilter rf= new RosettaFilter(31,elements,R,version);

		for(Integer num:set)
			rf.insert2(num);

		int[][] ranges = generateRanges(elements,2,R,Min,Max);

		boolean[] results = findAnswers(set,ranges);

		int fp=0;
		int falseQueries =0;
		for(int i=0;i<ranges.length;i++){
			if(!results[i]) {
				falseQueries++;
				if (rf.range2(ranges[i][0], ranges[i][1]))
					fp++;
			}
		}
		System.out.println("False Positive rate of range Queries :"+fp+" / "+falseQueries);

	}

	private static double rangeQueryPerformanceWithFixedRange(int version,int elements,int R) {

		List<Integer> array = new ArrayList<>();
		int Min = 1000000000;
		int Max = 2147483646;

		for (int i = 0; i < elements; i++){
			int random =Min + (int)(Math.random() * ((Max - Min) + 1));
			array.add(random);
		}
		Set<Integer> set = new HashSet<Integer>(array);
		RosettaFilter rf= new RosettaFilter(31,elements,R,version);

		for(Integer num:set)
			rf.insert2(num);

		int[][] ranges = generateFixedRanges(elements,R,Min,Max);

		boolean[] results = findAnswers(set,ranges);

		int fp=0;
		int falseQueries =0;
		for(int i=0;i<ranges.length;i++){
			if(!results[i]) {
				falseQueries++;
				if (rf.range2(ranges[i][0], ranges[i][1]))
					fp++;
			}
		}
		double result = (double)fp/ (double) falseQueries;
		return result;
	}

	private static double rangeQueryPerformanceWithFixedRangeEqualFPR(int version,int elements,int R,int depth) {

		List<Integer> array = new ArrayList<>();
		int Min = 1000000000;
		int Max = 2147483646;

		for (int i = 0; i < elements; i++){
			int random =Min + (int)(Math.random() * ((Max - Min) + 1));
			array.add(random);
		}
		Set<Integer> set = new HashSet<Integer>(array);
		RosettaFilter rf= new RosettaFilter(31,elements,R,version,depth);

		for(Integer num:set)
			rf.insert2(num);

		int[][] ranges = generateFixedRanges(elements,R,Min,Max);

		boolean[] results = findAnswers(set,ranges);

		int fp=0;
		int falseQueries =0;
		for(int i=0;i<ranges.length;i++){
			if(!results[i]) {
				falseQueries++;
				if (rf.range2(ranges[i][0], ranges[i][1]))
					fp++;
			}
		}
//		System.out.println("False Positive rate of range Queries :"+fp+" / "+falseQueries);
//		System.out.println("bits per elements are : "+rf.bitsperkey());

		double result = (double)fp/ (double) falseQueries;
		return result;
	}


	private static void rangeQueryPerformanceV3(int elements,int R) {

		List<Integer> array = new ArrayList<>();
		int Min = 1000000000;
		int Max = 2147483646;

		for (int i = 0; i < elements; i++){
			int random =Min + (int)(Math.random() * ((Max - Min) + 1));
			array.add(random);
		}
		Set<Integer> set = new HashSet<Integer>(array);
		RosettaFilter rf= new RosettaFilter(31,elements,512,3);

		for(Integer num:set)
			rf.insert2(num);

		int[][] ranges = generateRanges(elements,2,R,Min,Max);

		boolean[] results = findAnswers(set,ranges);

		int fp=0;
		int falseQueries =0;
		for(int i=0;i<ranges.length;i++){
			if(!results[i]) {
				falseQueries++;
				if (rf.range2(ranges[i][0], ranges[i][1]))
					fp++;
			}
		}
		System.out.println("False Positive rate of range Queries :"+fp+" / "+falseQueries);

	}

	public static void performanceTest(String[] args) {
		System.out.println("Point Query");
		pointQueryPerformance();
		System.out.println();
		System.out.println("Naive implementation");
		rangeQueryPerformance();  //Naive implementation
		System.out.println();
		System.out.println("FPR Equilibrium Across Nodes.");
		rangeQueryPerformanceV2(4,10000,64);  // FPR Equilibrium Across Nodes.
		System.out.println();
		System.out.println("Optimized Allocation of Memory Across Levels");
		rangeQueryPerformanceV3(10000,64); // Optimized Allocation of Memory Across Levels
	}

	public static void generatev2RangeResult() throws IOException {
		int [] rangeSizes = new int[] {2,4,8,16,32,64};
		double[] rangeSizesChart = new double[]{2.0,4.0,8.0,16.0,32.0,64.0};
		double[] results = new double[rangeSizes.length];
		int i=0;
		for(int range:rangeSizes){
			results[i] = rangeQueryPerformanceWithFixedRange(2,100000,range);

			i+=1;
		}

		final XYChart chart = new XYChartBuilder().width(800).height(600).title("Range size vs False Positive Rate").xAxisTitle("Range Size").yAxisTitle("False Positive Rate").build();

		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
		chart.addSeries("Rosetta", rangeSizesChart,results);

		BitmapEncoder.saveBitmap(chart, "images/rosetta-filter-range-vs-fpr-final", BitmapEncoder.BitmapFormat.PNG);

	}

	public static void generateEqualFPR() throws IOException {
		int [] rangeSizes = new int[] {2,4,8,16,32,64};
		double[] rangeSizesChart = new double[]{2.0,4.0,8.0,16.0,32.0,64.0};
		double[] results = new double[rangeSizes.length];
		double[] time = new double[rangeSizes.length];

		int i=0;
		//depth to control bits per key
		int depth =21;


		for(int range:rangeSizes){
			long startTime = System.currentTimeMillis();
			results[i] = rangeQueryPerformanceWithFixedRangeEqualFPR(4,100000,range,depth);
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			time[i] = (double) elapsedTime;
			i+=1;
		}

		final XYChart chart = new XYChartBuilder().width(800).height(600).title("Range size vs False Positive Rate(A First-Cut Solution: FPR Equilibrium) (~22 bits per key)").xAxisTitle("Range Size").yAxisTitle("False Positive Rate").build();

		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
		chart.addSeries("Rosetta", rangeSizesChart,results);

		BitmapEncoder.saveBitmap(chart, "images/rosetta-filter-range-vs-fpr-equal-fpr", BitmapEncoder.BitmapFormat.PNG);

		final XYChart chart2 = new XYChartBuilder().width(800).height(600).title("Range size vs Latency in milliseconds(A First-Cut Solution: FPR Equilibrium)").xAxisTitle("Range Size").yAxisTitle("All Query Execution Latency").build();

		chart2.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
		chart2.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
		chart2.addSeries("Rosetta", rangeSizesChart,time);

		BitmapEncoder.saveBitmap(chart2, "images/rosetta-filter-range-vs-fpr-equal-fpr-time", BitmapEncoder.BitmapFormat.PNG);
	}

	public static void generateOptimizedMemory() throws IOException {
		//Optimized Allocation of Memory Across Levels
		int [] rangeSizes = new int[] {2,4,8,16,32,64};
		double[] rangeSizesChart = new double[]{2.0,4.0,8.0,16.0,32.0,64.0};
		double[] results = new double[rangeSizes.length];
		double[] time = new double[rangeSizes.length];
		int i=0;
		for(int range:rangeSizes){
			long startTime = System.currentTimeMillis();
			results[i] = rangeQueryPerformanceWithFixedRange(3,100000,range);
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			time[i] = (double) elapsedTime;
			i+=1;
		}

		final XYChart chart = new XYChartBuilder().width(800).height(600).title("Range size vs False Positive Rate(Optimized Allocation of Memory Across Levels)").xAxisTitle("Range Size").yAxisTitle("False Positive Rate").build();

		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
		chart.addSeries("Rosetta", rangeSizesChart,results);

		BitmapEncoder.saveBitmap(chart, "images/rosetta-filter-range-vs-fpr-equal-memory-fpr", BitmapEncoder.BitmapFormat.PNG);


		final XYChart chart2 = new XYChartBuilder().width(800).height(600).title("Range size vs Latency in milliseconds(Optimized Allocation of Memory Across Levels)").xAxisTitle("Range Size").yAxisTitle("All Query Execution Latency").build();

		chart2.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
		chart2.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
		chart2.addSeries("Rosetta", rangeSizesChart,time);

		BitmapEncoder.saveBitmap(chart2, "images/rosetta-filter-range-vs-fpr-equal-memory-fpr-time", BitmapEncoder.BitmapFormat.PNG);
	}

	public static void main(String[] args) throws IOException {
			generatev2RangeResult();
			generateEqualFPR();
			generateOptimizedMemory();
	}
}
