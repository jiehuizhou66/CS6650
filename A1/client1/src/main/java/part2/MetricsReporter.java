package part2;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import part1.SkierThread;

public class MetricsReporter {

  private long wallTime;
  private int requestNum;
  private List<ResponseStat> allStats;
  //  mean response time (millisecs)
  private double meanResponseTime;
  //  median response time (millisecs)
  private double medianResponseTime;
  //  throughput = total number of requests/wall time
  private double throughput;
  //  p99 (99th percentile) response time.
  private double p99;
  //  max response time
  private double maxResponseTime;

  public MetricsReporter(BlockingQueue<SkierThread> threadList, long wallTime, int requestNum) {
    allStats = new ArrayList<>(threadList.size() * threadList.peek().getStats().size());
    for (SkierThread thread : threadList) {
      allStats.addAll(thread.getStats());
    }
    this.wallTime = wallTime;
    this.requestNum = requestNum;
  }

  public void calculate() {
    if (allStats.size() > 0) {
      Collections.sort(allStats, (a, b) -> (int) (a.getLatency() - b.getLatency()));
      this.meanResponseTime = allStats.stream().mapToLong(ResponseStat::getLatency).average().getAsDouble();
      this.medianResponseTime = allStats.size() % 2 == 0 ?
          (allStats.get(allStats.size() / 2 - 1).getLatency() + allStats.get(allStats.size() / 2).getLatency()) / 2.0
          : allStats.get(allStats.size() / 2).getLatency();
      this.throughput = (requestNum * 1000 / (double) wallTime);
      this.maxResponseTime = allStats.get(allStats.size() - 1).getLatency();

      long sum = allStats.stream().mapToLong(ResponseStat::getLatency).sum();
      double cut = sum * 0.99;
      sum = allStats.get(0).getLatency();
      int i = 0;
      while (sum < cut) {
        i++;
        sum += allStats.get(i).getLatency();
      }
      this.p99 = allStats.get(i).getLatency();
    } else {
      System.out.println("There is no data for part 2.");
    }
  }

  public void printMetrics() {
    System.out.println("-----------------Part 2------------------");
    System.out.println("mean response time (millisecs): " + meanResponseTime);
    System.out.println("median response time (millisecs): " + medianResponseTime);
    System.out.println("throughput (total number of requests/wall time): " + throughput);
    System.out.println("p99 response time (99th percentile, millisecs): " + p99);
    System.out.println("max response time(millisecs): " + maxResponseTime);
  }

  public void writeFile(String outputFilename) {
    try {
      FileWriter fileWriter = new FileWriter(outputFilename);
      PrintWriter printWriter = new PrintWriter(fileWriter);
      printWriter.printf("start time,request type (ie POST),latency,response code\n");
      for (ResponseStat stat : allStats) {
        String content = stat.getStart() + "," + stat.getRequestType() + "," + stat.getLatency() + "," + stat.getHttpCode();
        printWriter.printf("%s\n", content);
      }
      printWriter.close();
    } catch (FileNotFoundException ex) {
      System.out.println(
          "Cannot to open file '" +
              outputFilename + "'");
    } catch (IOException ex) {
      System.out.println(
          "Error when writing to file '"
              + outputFilename + "'");
    }
  }
}
