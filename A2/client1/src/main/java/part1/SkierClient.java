package part1;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import part2.MetricsReporter;

public class SkierClient {
  private static int MAX_NUM_OF_THREAD = 256;
  private static int MIN_NUM_OF_THREAD = 5;
  private static int MAX_NUM_OF_SKIERS = 50000;
  private static int MIN_NUM_OF_SKI_LIFT = 5;
  private static int MAX_NUM_OF_SKI_LIFT = 60;
  private static int TIME_OUT = 30;
  private static int DEFAULT_NUM_OF_THREAD = 64;
  private static int DEFAULT_NUM_OF_SKIERS = 50000;
  private static int DEFAULT_NUM_OF_SKI_LIFT = 40;
  private static int DEFAULT_SKI_DAY_NUM = 1;
  private static String DEFAULT_RESORT_NAME = "SilverMt";
  private static String ip = "ec2-34-224-41-25.compute-1.amazonaws.com";
//  private static String ip = "localhost";
  private static Integer port = 8080;
//private static String webapp = "a1server_war_exploded";
  private static String webapp = "a1server_war";
//  private static String webapp = "";
  private static String URL = "http://" + ip + ":" + port + "/" + webapp;
  private static String outputFile = "output/output-32thread.csv";
//private static String outputFile = "output/output-64thread.csv";
//private static String outputFile = "output/output-128thread.csv";
//  private static String outputFile = "output/output-256thread.csv";
//private static String outputFile = "output/output-1000thread.csv";


  private static int numThread = 8;
  private static int numSkiers = 10;
  private static int numLifts = 10;
  private static int skiDay = DEFAULT_SKI_DAY_NUM;
  private static String resortName = DEFAULT_RESORT_NAME;
  private static Logger logger = Logger.getLogger(Thread.class.getName());


  public static void main(String[] args) {
//    DEFAULT_NUM_OF_SKIERS = 128;
    if (args.length == 2) {
      ip = args[0];
      port = Integer.parseInt(args[1]);
    } else if (args.length == 7) {
      ip = args[0];
      port = Integer.parseInt(args[1]);
      validateParams(Integer.parseInt(args[2]), Integer.parseInt(args[3]),
          Integer.parseInt(args[4]), Integer.parseInt(args[5]), String.valueOf(args[6]));
      numThread = Integer.parseInt(args[2]);
      numSkiers = Integer.parseInt(args[3]);
      numLifts = Integer.parseInt(args[4]);
      skiDay = Integer.parseInt(args[5]);
      resortName = String.valueOf(args[6]);
    } else {
      System.err.println("Missing argument parameters");
      System.exit(1);
    }
    URL = "http://" + ip + ":" + port + "/" + webapp;

    // hard code the params for the ease of stress test
    if (!validateParams(numThread, numSkiers, numLifts, skiDay, resortName)) {
      throw new IllegalArgumentException(
          "Unable to create SkierClient due to invalid parameters. It requires:\n"
              + "maximal number of threads to run (numThreads - max 256)\n"
              + "number of skier to generate lift rides for (numSkiers - max 50000)\n"
              + "number of ski lifts (numLifts - range 5-60, default 40)\n"
              + "ski day number default to 1\n"
              + "resort name default to SilverMt\n"
      );
    }

    int numThreadPhase1 = numThread / 4;
    int numThreadPhase2 = numThread;
    int numThreadPhase3 = numThread / 4;
    int timeStartPhase1 = 0;
    int timeEndPhase1 = 90;
    int timeStartPhase2 = 91;
    int timeEndPhase2 = 360;
    int timeStartPhase3 = 361;
    int timeEndPhase3 = 420;
    int numOfPOSTRequestPhase1 = 1000;
    int numOfGETSkierDayVerticalRequestPhase1 = 5;
    int numOfGETSkierResortVerticalRequestPhase1 = 0;
    int numOfPOSTRequestPhase2 = 1000;
    int numOfGETSkierDayVerticalRequestPhase2 = 5;
    int numOfGETSkierResortVerticalRequestPhase2 = 0;
    int numOfPOSTRequestPhase3 = 1000;
    int numOfGETSkierDayVerticalRequestPhase3 = 10;
    int numOfGETSkierResortVerticalRequestPhase3 = 10;


    CountDownLatch latch1 = new CountDownLatch(0);
    CountDownLatch latch2 = new CountDownLatch(numThread / 4 / 10);
    CountDownLatch latch3 = new CountDownLatch(numThread / 10);

    BlockingQueue<SkierThread> allThreads = new ArrayBlockingQueue<>(numThread * 2);

    try {
      long start = System.currentTimeMillis();
      ExecutorService executorService1 =  createPhase(
          numThreadPhase1,
          numSkiers,
          timeStartPhase1,
          timeEndPhase1,
          numOfPOSTRequestPhase1,
          numOfGETSkierDayVerticalRequestPhase1,
          numOfGETSkierResortVerticalRequestPhase1,
          numLifts,
          resortName,
          skiDay,
          latch1,
          latch2,
          allThreads);
      ExecutorService executorService2 =  createPhase(
          numThreadPhase2,
          numSkiers,
          timeStartPhase2,
          timeEndPhase2,
          numOfPOSTRequestPhase2,
          numOfGETSkierDayVerticalRequestPhase2,
          numOfGETSkierResortVerticalRequestPhase2,
          numLifts,
          resortName,
          skiDay,
          latch2,
          latch3,
          allThreads);
      ExecutorService executorService3 =  createPhase(
          numThreadPhase3,
          numSkiers,
          timeStartPhase3,
          timeEndPhase3,
          numOfPOSTRequestPhase3,
          numOfGETSkierDayVerticalRequestPhase3,
          numOfGETSkierResortVerticalRequestPhase3,
          numLifts,
          resortName,
          skiDay,
          latch3,
          null,
          allThreads);
      executorService1.awaitTermination(TIME_OUT, TimeUnit.MINUTES);
      executorService2.awaitTermination(TIME_OUT, TimeUnit.MINUTES);
      executorService3.awaitTermination(TIME_OUT, TimeUnit.MINUTES);
      long end = System.currentTimeMillis();
      long wallTime = end - start;
      int successCount = 0;
      int failureCount = 0;
      for (SkierThread thread : allThreads) {
        successCount += thread.getSuccess();
        failureCount += thread.getFailure();
      }
      long throughput = (successCount + failureCount) * 1000 / wallTime;
      System.out.println("-----------------Part 1------------------");
      System.out.println("number of successful requests sent: " + successCount);
      System.out.println("number of unsuccessful requests sent: " + failureCount);
      System.out.println("Wall time: " + wallTime);
      System.out.println("Throughput: " + throughput);

      MetricsReporter metricsReporter1 = new MetricsReporter(allThreads, wallTime, (successCount + failureCount), "SkierPOST");
      metricsReporter1.writeFile(outputFile);
      metricsReporter1.calculate();
      metricsReporter1.printMetrics();

      MetricsReporter metricsReporter2 = new MetricsReporter(allThreads, wallTime, (successCount + failureCount), "SkierDayVerticalGET");
      metricsReporter2.writeFile(outputFile);
      metricsReporter2.calculate();
      metricsReporter2.printMetricsForSkierDayVert();

      MetricsReporter metricsReporter3 = new MetricsReporter(allThreads, wallTime, (successCount + failureCount), "SkierResortVerticalGET");
      metricsReporter3.writeFile(outputFile);
      metricsReporter3.calculate();
      metricsReporter3.printMetricsForSkierResortVert();
    } catch (Exception e) {
      logger.info("ExecutorService was interrupted with message: " + e.getMessage());
    }
  }

  private static ExecutorService createPhase(
      int threadNum,
      int skierNum,
      // in miliseconds
      int startTime,
      int endTime,
      int postRequestNum,
      int getDayVerticalRequestNum,
      int getResortVerticalRequestNum,
      int liftNum,
      String resortID,
      int dayID,
      CountDownLatch startLatch,
      CountDownLatch countingLatch,
      BlockingQueue<SkierThread> allThreads) {
    int skierRange = skierNum/threadNum;
    ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
    for (int i = 0; i < threadNum; i++) {
      SkierThread thread = new SkierThread(
          URL,
          i * skierRange + 1,
          (i + 1) * skierRange,
          startTime,
          endTime,
          postRequestNum,
          getDayVerticalRequestNum,
          getResortVerticalRequestNum,
          liftNum,
          resortID,
          dayID,
          startLatch,
          countingLatch,
          logger);
      executorService.execute(thread);
      allThreads.add(thread);
    }
    executorService.shutdown();
    return executorService;
  }

  private static boolean validateParams(int numThreads, int numSkiers, int numLifts, int skiDay, String resortName) {
    return numThreads > MIN_NUM_OF_THREAD && numThreads <= 1000
        && numSkiers >= 0 && numSkiers <= MAX_NUM_OF_SKIERS
        && numLifts >= MIN_NUM_OF_SKI_LIFT && numLifts <= MAX_NUM_OF_SKI_LIFT
        && skiDay == DEFAULT_SKI_DAY_NUM
        && resortName.equals(DEFAULT_RESORT_NAME);
  }
}


