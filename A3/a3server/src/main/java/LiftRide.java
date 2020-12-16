public class LiftRide {
  private String resortId;
  private String dayId;
  private String skierId;
  private String time;
  private String liftId;

  public LiftRide( String resortId, String dayId, String skierId, String time, String liftId) {
    this.skierId = skierId;
    this.resortId = resortId;
    this.dayId = dayId;
    this.time = time;
    this.liftId = liftId;
  }

  public String getSkierId() {
    return skierId;
  }

  public String getResortId() {
    return resortId;
  }

  public String getDayId() {
    return dayId;
  }

  public String getTime() {
    return time;
  }

  public String getLiftId() {
    return liftId;
  }
}
