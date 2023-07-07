import java.util.Arrays;

public class TestSort {
    public native int[] sortTest(int[] arr);

    private final static int LOOP_TIME = 10;

    static {
        System.loadLibrary("utils");
    }

    public long testCxxSort(int len, int minValue, int maxValue) {
        long totalTime = 0;

        for (int i = 0; i < LOOP_TIME; i++) {
            int[] arr = randomGen(len, minValue, maxValue);

            long millis1 = System.nanoTime();
            int[] sorted = sortTest(arr);
            long millis2 = System.nanoTime();

            // Check if the array is correctly sorted
            if (!isSorted(sorted)) {
                System.out.println("Wrong!");
                break;
            }

            totalTime += millis2 - millis1;
        }

        long averageTime = totalTime / LOOP_TIME;
        System.out.println(len + " elements array sorted by C++ takes an average time of " + averageTime + " ns");
        return averageTime;
    }

    public long testJavaSort(int len, int minValue, int maxValue) {
        long totalTime = 0;

        for (int i = 0; i < LOOP_TIME; i++) {
            int[] arr = randomGen(len, minValue, maxValue);

            long millis1 = System.nanoTime();
            Arrays.sort(arr);
            long millis2 = System.nanoTime();

            // Check if the array is correctly sorted
            if (!isSorted(arr)) {
                System.out.println("Wrong!");
                break;
            }

            totalTime += millis2 - millis1;
        }

        long averageTime = totalTime / LOOP_TIME;
        System.out.println(len + " elements array sorted by Java takes an average time of " + averageTime + " ns");
        return averageTime;
    }

    public int[] randomGen(int number, int minValue, int maxValue) {
        int[] arr = new int[number];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = getRandomNumber(minValue, maxValue);
        }
        return arr;
    }

    private int getRandomNumber(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    private boolean isSorted(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        TestSort testSort = new TestSort();
    
        // 预热
        long first_time1000000 = testSort.testCxxSort(1000000, 1, 10000000);
        long second_time1000000 = testSort.testCxxSort(1000000, 1, 10000000);
        long third_time1000000 = testSort.testCxxSort(1000000, 1, 100000);
        long time3 = testSort.testCxxSort(3, 1, 1000);
        long time4 = testSort.testCxxSort(4, 1, 1000);
        long time5 = testSort.testCxxSort(5, 1, 1000);
        long time100 = testSort.testCxxSort(100, 1, 1000);
        long time1000 = testSort.testCxxSort(1000, 1, 10000);
        long time10000 = testSort.testCxxSort(10000, 1, 100000);
        long time100000 = testSort.testCxxSort(100000, 1, 1000000);
        long time1000000 = testSort.testCxxSort(1000000, 1, 10000000);
        
        long first_jtime1000000 = testSort.testJavaSort(100000, 1, 10000000);
        long second_jtime1000000 = testSort.testJavaSort(100000, 1, 10000000);
        long third_jtime1000000 = testSort.testJavaSort(100000, 1, 10000000);
        long jtime3 = testSort.testJavaSort(3, 1, 1000);
        long jtime4 = testSort.testJavaSort(4, 1, 1000);
        long jtime5 = testSort.testJavaSort(5, 1, 1000);
        long jtime100 = testSort.testJavaSort(100, 1, 1000);
        long jtime1000 = testSort.testJavaSort(1000, 1, 10000);
        long jtime10000 = testSort.testJavaSort(10000, 1, 100000);
        long jtime100000 = testSort.testJavaSort(100000, 1, 1000000);
        long jtime1000000 = testSort.testJavaSort(1000000, 1, 10000000);
    }
}
