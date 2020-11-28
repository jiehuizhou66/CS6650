package part1;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import part2.ResponseStat;

public class SkierThread extends Thread {
  private int startID;
  private int endID;
  // in miliseconds
  private int startTime;
  private int endTime;
  private int postRequestNum;
  private int getSkierDayVerticalRequestNum;
  private int getSkierResortVerticalRequestNum;
  private int numLifts;
  private String resortID;
  private int dayID;
  private CountDownLatch startLatch;
  private CountDownLatch countingLatch;
  private String urlBase;
  private Logger logger;
  private List<ResponseStat> stats;
  // for A2 report each Get api
  private List<ResponseStat> getSkierDayVerticalRequestStats;
  private List<ResponseStat> getSkierResortVerticalRequestStats;
  private int success;
  private int failure;

  public SkierThread(
      String urlBase,
      int startID,
      int endID,
      int startTime,
      int endTime,
      int postRequestNum,
      int getSkierDayVerticalRequestNum,
      int getSkierResortVerticalRequestNum,
      int numLifts,
      String resortID,
      int dayID,
      CountDownLatch latch1,
      CountDownLatch latch2,
      Logger logger) {
    this.startID = startID;
    this.endID = endID;
    this.startTime = startTime;
    this.endTime = endTime;
    this.postRequestNum = postRequestNum;
    this.getSkierDayVerticalRequestNum = getSkierDayVerticalRequestNum;
    this.getSkierResortVerticalRequestNum = getSkierResortVerticalRequestNum;
    this.numLifts = numLifts;
    this.resortID = resortID;
    this.dayID = dayID;
    this.startLatch = latch1;
    this.countingLatch = latch2;
    this.logger = logger;
    this.urlBase = urlBase;
    this.stats = new ArrayList<>((postRequestNum+getSkierDayVerticalRequestNum+getSkierResortVerticalRequestNum));
    this.getSkierDayVerticalRequestStats = new ArrayList<>((getSkierDayVerticalRequestNum+getSkierResortVerticalRequestNum));
    this.getSkierResortVerticalRequestStats = new ArrayList<>((getSkierDayVerticalRequestNum+getSkierResortVerticalRequestNum));
    this.success = 0;
    this.failure = 0;
  }

  public void run() {
    try {
      startLatch.await();
      SkiersApi apiInstance = new SkiersApi();
      ApiClient client = apiInstance.getApiClient();
      client.setBasePath(urlBase);
      //Try
      client.setConnectTimeout(0);
      client.setReadTimeout(0);
      client.setWriteTimeout(0);
      //Try

      for (int i = 0; i < postRequestNum; i++) {
        sendPost(apiInstance);
      }
      for (int i = 0; i < getSkierDayVerticalRequestNum; i++) {
        sendGetSkierDayVertical(apiInstance);
      }
      for (int i = 0; i < getSkierResortVerticalRequestNum; i++) {
        sendGetSkierResortVertical(apiInstance);
      }
    } catch (InterruptedException e) {
      logger.info("Thread has InterruptedException: " + e.getMessage());
      failure++;
      e.printStackTrace();
    } finally {
      if (countingLatch != null) countingLatch.countDown();
    }
  }

  private void sendPost(SkiersApi apiInstance) {
    Integer skierID = createRandomIntFromRange(startID, endID + 1);
    Integer time = createRandomIntFromRange(startTime, endTime + 1);
    Integer liftID = createRandomIntFromRange(0, numLifts + 1);

    try {
      long before = System.currentTimeMillis();
      LiftRide body = new LiftRide();
      body.resortID(this.resortID);
      body.dayID(String.valueOf(this.dayID));
      body.skierID(skierID.toString());
      body.time(time.toString());
      body.liftID(liftID.toString());
      ApiResponse<Void> response = apiInstance.writeNewLiftRideWithHttpInfo(
          body);

      long after = System.currentTimeMillis();
      stats.add(new ResponseStat(before, after, response.getStatusCode(), "POST"));
      if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
//        System.out.println("success: \n" + response.toString());
        success++;
      } else {
        failure++;
        logger.info("Call on API writeNewLiftRideWithHttpInfo failed with code: " + response.getStatusCode());
//        System.out.println("Call on API writeNewLiftRideWithHttpInfo failed with code: " + response.getStatusCode());
      }
    } catch (ApiException e) {
      logger.info("ApiException: " + e.getMessage());
//      System.out.println("ApiException: " + e.getMessage() + "\nbody: " + e.getResponseBody() + "\ncode: " + e.getCode());
      failure++;
      e.printStackTrace();
    } catch (Exception e) {
      logger.info("Exception: " + e.getMessage());
      failure++;
      e.printStackTrace();
    }
  }

//    /skiers/{resortID}/days/{dayID}/skiers/{skierID}
  private void sendGetSkierDayVertical(SkiersApi apiInstance) {
    Integer skierID = createRandomIntFromRange(startID, endID + 1);
//    String skierID = "7788";
//    String resortID = "SilverMt";
//    String dayID = "1";
    try {
      long before = System.currentTimeMillis();
      ApiResponse<SkierVertical> response = apiInstance.getSkierDayVerticalWithHttpInfo(
          resortID, String.valueOf(dayID), skierID.toString());
//      SkierVertical response = apiInstance.getSkierDayVertical(
//          resortID, String.valueOf(dayID), skierID.toString());

      long after = System.currentTimeMillis();
      stats.add(new ResponseStat(before, after, response.getStatusCode(), "GET"));
      getSkierDayVerticalRequestStats.add(new ResponseStat(before, after, response.getStatusCode(), "GET"));
      if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
//        System.out.println("success: \n" + response.toString());
        success++;
      } else {
        failure++;
        logger.info("Call on API getSkierDayVerticalWithHttpInfo failed with code: " + response.getStatusCode());
//        System.out.println("Call on API getSkierDayVerticalWithHttpInfo failed with code: " + response.getStatusCode());
      }
    } catch (ApiException e) {
      logger.info("ApiException: " + e.getMessage());
//      System.out.println("ApiException: " + e.getMessage() + "\nbody: " + e.getResponseBody() + "\ncode: " + e.getCode());
      failure++;
      e.printStackTrace();
    } catch (Exception e) {
      logger.info("Exception: " + e.getMessage());
      failure++;
      e.printStackTrace();
    }
  }

//  /skiers/{skierID}/vertical
private void sendGetSkierResortVertical(SkiersApi apiInstance) {
  Integer skierID = createRandomIntFromRange(startID, endID + 1);
  List<String> resorts = Arrays.asList(this.resortID);
  try {
    long before = System.currentTimeMillis();
    ApiResponse<SkierVertical> response = apiInstance.getSkierResortTotalsWithHttpInfo(
        skierID.toString(), resorts);
//      SkierVertical response = apiInstance.getSkierDayVertical(
//          resortID, String.valueOf(dayID), skierID.toString());

    long after = System.currentTimeMillis();
    stats.add(new ResponseStat(before, after, response.getStatusCode(), "GET"));
    getSkierResortVerticalRequestStats.add(new ResponseStat(before, after, response.getStatusCode(), "GET"));
    if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
//        System.out.println("success: \n" + response.toString());
      success++;
    } else {
      failure++;
      logger.info("Call on API getSkierResortTotalsWithHttpInfo failed with code: " + response.getStatusCode());
//      System.out.println("Call on API getSkierResortTotalsWithHttpInfo failed with code: " + response.getStatusCode());
    }
  } catch (ApiException e) {
    logger.info("ApiException: " + e.getMessage());
//    System.out.println("ApiException: " + e.getMessage() + "\nbody: " + e.getResponseBody() + "\ncode: " + e.getCode());
    failure++;
    e.printStackTrace();
  } catch (Exception e) {
    logger.info("Exception: " + e.getMessage());
    failure++;
    e.printStackTrace();
  }
}


  public static int createRandomIntFromRange(int min, int max) {
    Random r = new Random();
    return r.nextInt((max - min) + 1) + min;
  }

  public List<ResponseStat> getStats() {
    return stats;
  }

  public List<ResponseStat> getGetSkierResortVerticalRequestStats() {
    return getSkierResortVerticalRequestStats;
  }

  public List<ResponseStat> getGetSkierDayVerticalRequestStats() {
    return getSkierDayVerticalRequestStats;
  }

  public int getSuccess() {
    return success;
  }

  public int getFailure() {
    return failure;
  }
}
