package part2;

public class ResponseStat {
  private long start;
  private long end;
  private int httpCode;
  private String requestType;


  public ResponseStat(long start, long end, int httpCode, String requestType) {
    this.start = start;
    this.end = end;
    this.httpCode = httpCode;
    this.requestType = requestType;
  }

  public long getStart() {
    return start;
  }

  public long getLatency() {
    return end - start;
  }

  public int getHttpCode() {
    return httpCode;
  }

  public String getRequestType() {
    return requestType;
  }
}
