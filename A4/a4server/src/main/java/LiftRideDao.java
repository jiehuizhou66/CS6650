import java.sql.*;
import org.apache.commons.dbcp2.*;

public class LiftRideDao {
  private static BasicDataSource dataSource;
  private static BasicDataSource dataSource2;
  private static int DBSHARDNUMBER = 10000;

  public LiftRideDao() {
    dataSource = DBCPDataSource.getDataSource();
    dataSource2 = DBCPDataSource.getDataSource2();
  }

  public boolean createLiftRide(LiftRide newLiftRide) {
    boolean isSuccess = false;
    Connection conn = null;
    PreparedStatement preparedStatement = null;

    int skierID = Integer.parseInt(newLiftRide.getSkierId());
    String insertQueryStatement;
    if (skierID <= 5000) {
      insertQueryStatement = "INSERT INTO LiftRides1 (resortId, dayId, skierId, time, liftId) " +
          "VALUES (?,?,?,?,?)";
    } else if (skierID > 5000 && skierID <= 10000){
      insertQueryStatement = "INSERT INTO LiftRides2 (resortId, dayId, skierId, time, liftId) " +
          "VALUES (?,?,?,?,?)";
    } else if (skierID > 10000 && skierID <= 15000){
      insertQueryStatement = "INSERT INTO LiftRides3 (resortId, dayId, skierId, time, liftId) " +
          "VALUES (?,?,?,?,?)";
    } else {
      insertQueryStatement = "INSERT INTO LiftRides4 (resortId, dayId, skierId, time, liftId) " +
          "VALUES (?,?,?,?,?)";
    }

    try {
      if (skierID <= 10000) {
        conn = dataSource.getConnection();
      } else {
        conn = dataSource2.getConnection();
      }
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setString(1, newLiftRide.getResortId());
      preparedStatement.setString(2, newLiftRide.getDayId());
      preparedStatement.setString(3, newLiftRide.getSkierId());
      preparedStatement.setString(4, newLiftRide.getTime());
      preparedStatement.setString(5, newLiftRide.getLiftId());

      // execute insert SQL statement
      preparedStatement.executeUpdate();
      isSuccess = true;
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
      return isSuccess;
    }
  }

  public String queryLiftRideVerticalPerDay(String skierID, String dayID) {
//    String queryclause
//        = "SELECT SUM(liftID) as totalVertical FROM LiftRides"
//        + " WHERE skierID = " + skierID
//        + " AND dayID = " + dayID
//        + ";";

    String queryclause;
    int skierNumber = Integer.parseInt(skierID);
    if (skierNumber <= 5000) {
      queryclause = "SELECT SUM(liftID) as totalVertical FROM LiftRides1 USE INDEX(DAY_INDEX)" + " WHERE skierId = " + skierID
          + " AND dayId = " + dayID + ";";
    } else if (skierNumber > 5000 && skierNumber <= 10000){
      queryclause = "SELECT SUM(liftID) as totalVertical FROM LiftRides2 USE INDEX(DAY_INDEX)" + " WHERE skierId = " + skierID
          + " AND dayId = " + dayID + ";";
    } else if (skierNumber > 10000 && skierNumber <= 15000){
      queryclause = "SELECT SUM(liftID) as totalVertical FROM LiftRides3 USE INDEX(DAY_INDEX)" + " WHERE skierId = " + skierID
          + " AND dayId = " + dayID + ";";
    } else {
      queryclause = "SELECT SUM(liftID) as totalVertical FROM LiftRides4 USE INDEX(DAY_INDEX)" + " WHERE skierId = " + skierID
          + " AND dayId = " + dayID + ";";
    }

//    System.out.println(queryclause);
    boolean isSuccess = false;
    int retryNum = 0;
    Integer output = -1;
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    while (!isSuccess && retryNum < 2) {
      try {
        if (skierNumber <= 10000) {
          conn = dataSource.getConnection();
        } else {
          conn = dataSource2.getConnection();
        }

        preparedStatement = conn.prepareStatement(queryclause);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
          output = resultSet.getInt(1);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        retryNum++;
        try {
          if (conn != null) {
            conn.close();
          }
          if (preparedStatement != null) {
            preparedStatement.close();
          }
        } catch (SQLException se) {
          se.printStackTrace();
        }
      }
    }
    return output == -1 ? null : output.toString();
  }


  public String queryLiftRideVerticalPerResort(String skierID, String resortID) {
//    String queryclause
//        = "SELECT SUM(liftID) as totalVertical FROM LiftRides"
//        + " WHERE skierID = " + skierID
//        + " AND resortID = \"" + resortID
//        + "\";";

    String queryclause;
    int skierNumber = Integer.parseInt(skierID);
    if (skierNumber <= 5000) {
      queryclause =
          "SELECT SUM(liftID) as totalVertical FROM LiftRides1 USE INDEX(RESORT_INDEX)" + " WHERE skierId = " + skierID
              + " AND resortId = \"" + resortID + "\";";
    } else if (skierNumber > 5000 && skierNumber <= 10000){
      queryclause =
          "SELECT SUM(liftID) as totalVertical FROM LiftRides2 USE INDEX(RESORT_INDEX)" + " WHERE skierId = " + skierID
              + " AND resortId = \"" + resortID + "\";";
    } else if (skierNumber > 10000 && skierNumber <= 15000){
      queryclause =
          "SELECT SUM(liftID) as totalVertical FROM LiftRides3 USE INDEX(RESORT_INDEX)" + " WHERE skierId = " + skierID
              + " AND resortId = \"" + resortID + "\";";
    } else {
      queryclause =
          "SELECT SUM(liftID) as totalVertical FROM LiftRides3 USE INDEX(RESORT_INDEX)" + " WHERE skierId = " + skierID
              + " AND resortId = \"" + resortID + "\";";
    }


//    System.out.println(queryclause);
    boolean isSuccess = false;
    int retryNum = 0;
    Integer output = -1;
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    while (!isSuccess && retryNum < 2) {
      try {
        if (skierNumber <= 10000) {
          conn = dataSource.getConnection();
        } else {
          conn = dataSource2.getConnection();
        }
        preparedStatement = conn.prepareStatement(queryclause);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
          output = resultSet.getInt(1);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        retryNum++;
        try {
          if (conn != null) {
            conn.close();
          }
          if (preparedStatement != null) {
            preparedStatement.close();
          }
        } catch (SQLException se) {
          se.printStackTrace();
        }
      }
    }
    return output == -1 ? null : output.toString();
  }

}
