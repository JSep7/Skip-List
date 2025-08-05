import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

public class SkipListTestHarness {
	private static class CPUTimer {
		public static <T> long timeFor(Callable<T> task) {
			try {
				long start = System.currentTimeMillis();
				T t = task.call();
				long end = System.currentTimeMillis();
				return end - start;
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
			return 0;
		}
	}
	
	static long RandomSeed = 1;

	static Random RandomGenerator = new Random(RandomSeed);
	static byte[] buf = new byte[1024];
	
	private static ArrayList<Integer> generateIntArrayList(int howMany) {
		ArrayList<Integer> list = new ArrayList<Integer>(howMany);
		
		for(int i = 0; i < howMany; i++) {
			list.add(Integer.valueOf(RandomGenerator.nextInt()));
		}
		
		return list;
	}
	
	private static ArrayList<Double> generateDoubleArrayList(int howMany) {
		ArrayList<Double> list = new ArrayList<Double>(howMany);
		
		for(int i = 0; i < howMany; i++) {
			list.add(Double.valueOf(RandomGenerator.nextDouble()));
		}
		
		return list;
	}

	private static String generateRandomString(int len) {
		if(len > 1024) len = 1024;
		
		buf[len - 1] = (byte) 0;

		for(int j = 0; j < (len - 1); j++) {
			buf[j] = (byte) (RandomGenerator.nextInt(94) + 32);
		}

		return new String(buf);
	}
	
	private static ArrayList<String> generateStringArrayList(int howMany, int len) {
		ArrayList<String> list = new ArrayList<String>(howMany);
		
		for(int i = 0; i < howMany; i++) {
			list.add(generateRandomString(len));
		}
		
		return list;
	}
	
	private static <T> ArrayList<T> generateStrikeList(ArrayList<? extends T> fromList, int howMany) {
		ArrayList<T> strikeList = new ArrayList<T>(howMany);
		int fromLast = fromList.size() - 1;
		
		for(int i = 0; i < howMany; i++) {
			strikeList.add(fromList.get(RandomGenerator.nextInt(fromLast)));
		}
		
		return strikeList;
	}

	private static <T> ArrayList<T> generateRemoveList(ArrayList<? extends T> fromList) {
		ArrayList<T> removeList = new ArrayList<T>(fromList.size()/2);
		
		for(int i = 0; i < fromList.size() / 2; i++) {
			removeList.add(fromList.get(i));
		}
		
		return removeList;
	}

	private static <T> int executeFinds(Collection<? extends T> coll, ArrayList<? extends T> strikes) {
		boolean sentinel;
		int failures = 0;

		for (T e: strikes) {
			sentinel = coll.contains(e);
			if(sentinel == false) {
				failures++;
			}
		}
		
		if(failures > 0) {
			System.out.printf("(%,d missing) ", failures);
		}
		
		return 0;
	}

	private static <T extends Comparable<T>> void executeCase(ArrayList<? extends T> values, ArrayList<? extends T> strikes, boolean includeLinkedList, boolean includeRemoves) {
		ArrayList<T> removeList = generateRemoveList(strikes);
		long start;
		long end;
		long ms;

		if(includeLinkedList) {
			LinkedList<T> linkedList = new LinkedList<T>();
			
			System.out.printf("  LinkedList  ");
			ms = CPUTimer.timeFor(() -> linkedList.addAll(values));
			System.out.printf("add: %,6dms  ", ms);
			ms = CPUTimer.timeFor(() -> executeFinds(linkedList, strikes));
			System.out.printf("find: %,6dms  ", ms);
			if(includeRemoves) {
				ms = CPUTimer.timeFor(() -> linkedList.removeAll(removeList));
				System.out.printf("del: %,6dms  ", ms);
				ms = CPUTimer.timeFor(() -> executeFinds(linkedList, strikes));
				System.out.printf("find: %,6dms  ", ms);
			}
			System.out.printf("\n");
		}

		System.gc();

		
		if(true) {
			SkipListSet<T> skipListSet = new SkipListSet<T>();
			
			System.out.printf("  SkipListSet ");
			ms = CPUTimer.timeFor(() -> skipListSet.addAll(values));
			System.out.printf("add: %,6dms  ", ms);
			ms = CPUTimer.timeFor(() -> executeFinds(skipListSet, strikes));
			System.out.printf("find: %,6dms  ", ms);
	
			if(includeRemoves) {
				ms = CPUTimer.timeFor(() -> skipListSet.removeAll(removeList));
				System.out.printf("del: %,6dms  ", ms);
				ms = CPUTimer.timeFor(() -> executeFinds(skipListSet, strikes));
				System.out.printf("find: %,6dms  ", ms);
			}
	
			System.out.printf("\n");
			System.out.printf("                                             ");
	
			start = System.currentTimeMillis();
			skipListSet.reBalance();
			end = System.currentTimeMillis();
			ms = end - start;
			System.out.printf("bal: %,6dms  ", ms);
	
			ms = CPUTimer.timeFor(() -> executeFinds(skipListSet, strikes));
			System.out.printf("find: %,6dms  ", ms);
			
			System.out.printf("\n");
		}

		System.gc();


		if(true) {
			TreeSet<T> treeSet = new TreeSet<T>();

			System.out.printf("  TreeSet     ");
			ms = CPUTimer.timeFor(() -> treeSet.addAll(values));
			System.out.printf("add: %,6dms  ", ms);
	
			ms = CPUTimer.timeFor(() -> executeFinds(treeSet, strikes));
			System.out.printf("find: %,6dms  ", ms);
	
			if(includeRemoves) {
				ms = CPUTimer.timeFor(() -> treeSet.removeAll(removeList));
				System.out.printf("del: %,6dms  ", ms);
				ms = CPUTimer.timeFor(() -> executeFinds(treeSet, strikes));
				System.out.printf("find: %,6dms  ", ms);
			}
			System.out.printf("\n");		
		}
		
		System.gc();
		
		System.out.printf("\n");		
	}

	public static void executeStringCase(int listSize, int strikeSize, int stringSize, boolean includeLinkedList, boolean includeRemoves) {
		System.out.printf("CASE: %,d strings of length %,d, %,d finds, %,d removals.  Generating...\n", listSize, stringSize, strikeSize, (strikeSize/2));

		ArrayList<String> strings = generateStringArrayList(listSize, stringSize);
		ArrayList<String> strikes = generateStrikeList(strings, strikeSize);
		
		executeCase(strings, strikes, includeLinkedList, includeRemoves);
	}
	
	public static void executeIntCase(int listSize, int strikeSize, boolean includeLinkedList, boolean includeRemoves) {
		System.out.printf("CASE: %,d integers, %,d finds, %,d removals.  Generating...\n", listSize, strikeSize, strikeSize/2);

		ArrayList<Integer> intlist = generateIntArrayList(listSize);
		ArrayList<Integer> strikes = generateStrikeList(intlist, strikeSize);
		
		executeCase(intlist, strikes, includeLinkedList, includeRemoves);
	}
	
	public static void executeDoubleCase(int listSize, int strikeSize, boolean includeLinkedList, boolean includeRemoves) {
		System.out.printf("CASE: %,d doubles, %,d finds, %,d removals.  Generating...\n", listSize, strikeSize, strikeSize/2);

		ArrayList<Double> doubles = generateDoubleArrayList(listSize);
		ArrayList<Double> strikes = generateStrikeList(doubles, strikeSize);
		
		executeCase(doubles, strikes, includeLinkedList, includeRemoves);
	}
	
	public SkipListTestHarness() {}
	
	public static void main(String args[]) {
		SkipListTestHarness.executeStringCase(100000, 10000, 1000, false, true);
		System.gc();
		SkipListTestHarness.executeStringCase(1000000, 10000, 1000, false, true);
		System.gc();
		SkipListTestHarness.executeStringCase(1000000, 100000, 1000, false, true);
		System.gc();
		SkipListTestHarness.executeDoubleCase(100000, 10000, true, true);
		System.gc();
		SkipListTestHarness.executeDoubleCase(1000000, 10000, false, true);
		System.gc();
		SkipListTestHarness.executeDoubleCase(1000000, 100000, false, true);
		System.gc();
		SkipListTestHarness.executeIntCase(100000, 10000, true, true);
		System.gc();
		SkipListTestHarness.executeIntCase(1000000, 10000, false, true);
		System.gc();
		SkipListTestHarness.executeIntCase(10000000, 10000, false, true);
		System.gc();
		SkipListTestHarness.executeIntCase(10000000, 1000000, false, true);	
		System.gc();
		SkipListTestHarness.executeIntCase(10000000, 10000000, false, true);	
		System.gc();
	}
}