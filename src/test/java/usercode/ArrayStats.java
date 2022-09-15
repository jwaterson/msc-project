package usercode;

import instrumentation.ThreadMapMediator;
import instrumentation.ThreadMarker;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class whose methods provide statistics about given arrays
 *
 * @author Josh Waterson
 */
public class ArrayStats {

    /**
     * Outputs basic statistics (median, mean, mode and range) about
     * the input array to the console.
     *
     * @param arr input array
     */
    public void getStats(int[] arr) {
        if (Objects.isNull(arr) || arr.length == 0) {
            throw new IllegalArgumentException("Cannot get stats for empty or null array");
        }
        double median = getMedian(arr);
        double mean = getMean(arr);
        int[] mode = getMode(arr);
        long range = getRange(arr);

        System.out.printf("Median: %s\nMean: %s\nMode: %s\nRange: %d\n",
                formatDouble(median),
                formatDouble(mean),
                IntStream.of(mode)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining(", ")),
                range);
    }

    /**
     * gets the statistical range of input array (min element subtracted
     * from max element). Note, while this implementation uses IntStream's
     * max and min methods (both of which return OptionalInts) the stream
     * will never be empty - empty and null input arrays are handled before
     * this method is called by getStats, so the orElse() default is included
     * purely in order that the code compiles.
     *
     * @param arr   input array
     * @return      difference between max and min elements in the array
     */
    private long getRange(int[] arr) {
        long max = IntStream.of(arr).max().orElse(0);
        long min = IntStream.of(arr).min().orElse(0);
        return max - min;
    }

    /**
     * gets the mode of input array elements.
     *
     * @param arr   input array
     * @return      most frequently occurring element(s) in the input array
     */
    private int[] getMode(int[] arr) {
        Map<Integer, Long> freqMap = Arrays.stream(arr)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return freqMap.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue(),
                        Objects.requireNonNull(freqMap.entrySet().stream()
                            .max(Map.Entry.comparingByValue()).orElse(null)).getValue()))
                .mapToInt(Map.Entry::getKey)
                .toArray();
    }

    /**
     * gets the median of input array elements using an implementation of
     * quickselect algorithm.
     *
     * @param arr   input array
     * @return      median of the input array elements
     */
    private double getMedian(int[] arr) {
        int first = 0;
        int last = arr.length - 1;
        int mid = arr.length / 2;
        while (first < last) {
            int pivot = getPartitioned(arr, first, last);
            if (pivot < mid) {
                first = pivot + 1;
            }
            else if (pivot > mid) {
                last = pivot - 1;
            }
            else {
                first = pivot;
                break;
            }
        }
        return arr.length % 2 != 0 ?
                arr[first] : ((long) arr[first - 1] + (long) arr[first]) / 2.0;
    }

    /**
     * sorts given int array by the natural order of its
     * elements by partitioning and returns a pivot value.
     *
     * @param arr   input array
     * @param first first element in partitioned array
     * @param last  last element in partitioned array
     * @return      pivot value
     */
    private int getPartitioned(int[] arr, int first, int last) {
        int pivot = first;
        int temp;
        while (first <= last) {
            while (first <= last && arr[first] <= arr[pivot]) {
                first++;
            }
            while (first <= last && arr[last] > arr[pivot]) {
                last--;
            }
            if (first > last) {
                break;
            }
            temp = arr[first];
            arr[first] = arr[last];
            arr[last] = temp;
        }
        temp = arr[last];
        arr[last] = arr[pivot];
        arr[pivot] = temp;
        return last;
    }

    /**
     * iteratively gets the mean of input array elements.
     *
     * @param arr   input array
     * @return      mean of the input array elements
     */
    private double getMean(int[] arr) {
        double avg = 0;
        int d = 1;
        for (int i : arr) {
            avg += (i - avg) / d;
            ++d;
        }
        return avg;
    }

    /**
     * Helper method to output formatted representation of double value.
     *
     * @param d     double to be formatted
     * @return      formatted String of input double value
     */
    private String formatDouble(double d) {
        return String.format(d % 1.0 != 0 ? "%s" : "%.0f", d);
    }

    /**
     * Helper method for use in demonstrating sample code in main
     *
     * @return      potentially large array
     */
    private static int[] generateSomeHugeArray() {
        int[] arr1 = new int[1000]; // limited to 10000 for convenience only
        for (int i = 0; i < arr1.length; i++) {
//            arr1[i] = new Random().nextInt() * (i % 2 == 0 ? 1 : -1);
            arr1[i] = new Random().nextInt(-1, 2);
        }
        return arr1;
    }

    public static void main(String[] args) {
        ThreadMapMediator.submitThreadMarker(Thread.currentThread(),
                new ThreadMarker(System.nanoTime() - 2, 2000, ""));
        int[] myBigArray = generateSomeHugeArray();
        System.out.println(Arrays.toString(myBigArray));
        ArrayStats arrayManipulator = new ArrayStats();
        arrayManipulator.getStats(myBigArray);

    }
}
