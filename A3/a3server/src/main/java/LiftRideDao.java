import java.sql.*;
import org.apache.commons.dbcp2.*;

public class LiftRideDao {
  private static BasicDataSource dataSource;

  public LiftRideDao() {
    dataSource = DBCPDataSource.getDataSource();
  }

  public boolean createLiftRide(LiftRide newLiftRide) throws SQLException {
    boolean isSuccess = false;
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement = "INSERT INTO LiftRides (resortId, dayId, skierId, time, liftId) " +
        "VALUES (?,?,?,?,?)";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setString(1, newLiftRide.getResortId());
      preparedStatement.setString(2, newLiftRide.getDayId());
      preparedStatement.setString(3, newLiftRide.getSkierId());
      preparedStatement.setString(4, newLiftRide.getTime());
      preparedStatement.setString(5, newLiftRide.getLiftId());

      // execute insert SQL statement
      preparedStatement.executeUpdate();
      isSuccess = true;
    }
//    catch (SQLException e) {
//      e.printStackTrace();
//    }
    finally {
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

    String queryclause
        = "SELECT SUM(liftID) as totalVertical FROM LiftRides USE INDEX(dayIndex)"
        + " WHERE skierId = " + skierID
        + " AND dayId = " + dayID
        + ";";
//    System.out.println(queryclause);
    boolean isSuccess = false;
    int retryNum = 0;
    Integer output = -1;
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    while (!isSuccess && retryNum < 2) {
      try {
        conn = dataSource.getConnection();
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

    String queryclause
        = "SELECT SUM(liftID) as totalVertical FROM LiftRides USE INDEX(resortIndex)"
        + " WHERE skierId = " + skierID
        + " AND resortId = \"" + resortID
        + "\";";

//    System.out.println(queryclause);
    boolean isSuccess = false;
    int retryNum = 0;
    Integer output = -1;
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    while (!isSuccess && retryNum < 2) {
      try {
        conn = dataSource.getConnection();
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
