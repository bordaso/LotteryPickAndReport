package init;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//TASKS:
//Write an optimized code that can report the results in a couple of seconds – or faster, even better.  
//___ANSWER---> Tried my best, the 10M data process time takes around 9s-13s. The reporting with the _calledNormal()_ takes around 700-900 MILLIsecond, with the _calledWithExecutorService()_ it takes around 4-6 seconds

//Document the asymptotic run time of your solution  
//___ANSWER---> I dont really know how to do that properly.

//Use code comments (enough to make it easy to understand)  
//___ANSWER---> Tried.

//Document your ideas how you could further  improve the calculation speed, if any  
//___ANSWER---> Somehow the data processing needs to be speeded up.....regarding to the reporting part, I have tried to speed up the normal process with the Executor service, but it had a way worse time...

//Submit your code by email (do not share it in public space like github), including instructions on how to build and run. 
//___ANSWER---> Done

//Implemented by BORDASO
//2019.06.17.
//Budapest, Hungary
public class LotteryCorp {

//	private static Integer[] testNUmbers= {4, 79, 13, 80, 56};
	private static Integer[] testNUmbers = { 71, 84, 48, 85, 38 };
	private static List<Integer> WINNER = Arrays.asList(testNUmbers);
	
	private static List<List<Integer>> testList = new ArrayList<>();

	//Maps lottery numbers to list of "lottery tickets" that has picked the number
	private static Map<Integer, List<List<Integer>>> lotteryNumbersAndContainingTickets = new HashMap<>();

	private static int twoHit;
	private static int threeHit;
	private static int fourHit;
	private static int fiveHit;

	private static Instant start;

	public static void main(String[] args) {

		//Process dataset
		dataProcess(args);

		// Normal way to report
		calledNormal();
		
		// Working but worse trial to enhance the normal way to report
		// calledWithExecutorService();

	}
	
	private static void dataProcess(String[] args) {
		
		WINNER = args.length == 0 ? WINNER
				: Arrays.asList(args).stream().mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

		
		IntStream.rangeClosed(1, 90).collect(() -> lotteryNumbersAndContainingTickets, (ac, e) -> {
			ac.put(e, new ArrayList<List<Integer>>());
		}, (ac, ac2) -> {
			ac.putAll(ac2);
		});

		Instant startTheWholeProcess = Instant.now();
		System.out.println("startTheWholeProcess " + startTheWholeProcess);

		File file = new File("resources/10mTEST.txt");
		FileInputStream fis = null;
		BufferedReader br = null;

		try {
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

			String rawLine;
			int i = 0;
			// while(i != 1) {
			while ((rawLine = br.readLine()) != null) {

				String[] arrayLine = rawLine.split(" ", 0);

				List<Integer> lotteryTicket = Arrays.asList(arrayLine).stream().mapToInt(Integer::parseInt).boxed()
						.collect(Collectors.toList());
				
			//	testList.add(lotteryTicket);

				Collections.sort(lotteryTicket);

				//All "lottary-tickets" mapped to their lowest picked number
				int zerothVal = lotteryTicket.get(0);
				int num = zerothVal < 0 ? 1 : zerothVal;
				lotteryNumbersAndContainingTickets.get(num).add(lotteryTicket);
				

				// i++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		Instant dataSetupDone = Instant.now();
		System.out.println("dataSetupDone in seconds:"
				+ Duration.between(startTheWholeProcess, dataSetupDone).toNanos() / 1_000_000_000);
		System.out.println("READY ");
		
	}

	private static void calledNormal() {

		getConsoleInput();

		start = Instant.now();
		System.out.println("START " + start);

		//Check all the numbers-> the keys, get the related lottery-tickets, then check them for hits
		for (int winnerNum1stIt = 1; winnerNum1stIt < 90; winnerNum1stIt++) {
			List<List<Integer>> lotteryTickets = lotteryNumbersAndContainingTickets.get(winnerNum1stIt);
			for (List<Integer> lotteryTicket : lotteryTickets) {
				int hits = 0;
				for (int winnerNum2ndIt : WINNER) {
					if (lotteryTicket.contains(winnerNum2ndIt)) {
						hits++;
					}
					;
				}

				switch (hits) {
				case 2:
					twoHit++;
					break;
				case 3:
					threeHit++;
					break;
				case 4:
					fourHit++;
					break;
				case 5:
					fiveHit++;
					break;
				}
			}

		}

		System.out.println("twoHit " + twoHit);
		System.out.println("threeHit " + threeHit);
		System.out.println("fourHit " + fourHit);
		System.out.println("fiveHit " + fiveHit);

		Instant finish = Instant.now();
		System.out.println("FINISH NORMAL" + finish);

		long diff = Duration.between(start, finish).toNanos() / 1_000_000;

		System.out.println("Difference in millisecond " + diff);

	}

	private static void calledWithExecutorService() {
		
		getConsoleInput();

		start = Instant.now();
		System.out.println("START " + start);

		ExecutorService executor = Executors.newFixedThreadPool(90);

		try {

			List<Callable<Object>> calls = IntStream.rangeClosed(1, 90).mapToObj(e ->

			Executors.callable(() -> {

				List<List<Integer>> lotteryTickets = lotteryNumbersAndContainingTickets.get(e);
				for (List<Integer> lotteryTicket : lotteryTickets) {
					int hits = 0;
					for (int winnerNum2ndIt : WINNER) {
						if (lotteryTicket.contains(winnerNum2ndIt)) {
							hits++;
						}
						;
					}

					switch (hits) {
					case 2:
						twoHit++;
						break;
					case 3:
						threeHit++;
						break;
					case 4:
						fourHit++;
						break;
					case 5:
						fiveHit++;
						break;
					}
				}

			})

			).collect(Collectors.toList());

			List<Future<Object>> futures = executor.invokeAll(calls);

			System.out.println("twoHit " + twoHit);
			System.out.println("threeHit " + threeHit);
			System.out.println("fourHit " + fourHit);
			System.out.println("fiveHit " + fiveHit);

			Instant finish = Instant.now();
			System.out.println("FINISH EXECUTOR SERVICE" + finish);

			float diff = Duration.between(start, finish).toNanos() / 1_000_000_000;

			System.out.println("Difference in seconds " + diff);

		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			// shut down the executor manually
			executor.shutdown();
		}

	}

	private static void getConsoleInput() {

		BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
		String inputFromConsole = "";
		try {
			inputFromConsole = consoleReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(inputFromConsole.equals("")) {
			return;
		}
		
		String[] inputFromConsoleArray = inputFromConsole.split(" ", 0);
		
		

		List<Integer> tempList = new ArrayList<>();
		tempList.addAll(Arrays.asList(inputFromConsoleArray).stream().mapToInt(Integer::parseInt).boxed()
				.collect(Collectors.toList()));	
				
		
		WINNER = tempList.size()==0?WINNER:tempList;

	}

}
