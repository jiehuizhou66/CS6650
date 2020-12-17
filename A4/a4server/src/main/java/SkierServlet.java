import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.temporal.ValueRange;


@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
  private final LiftRideDao liftRideDao = new LiftRideDao();

  protected void doPost(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);
    res.setContentType("application/json");


    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0 || urlPath.equals("/")) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(new Gson().toJson("missing parameter"));
      return;
    }

    // Parse the request json body
    String resortID;
    Integer dayID;
    Integer skierID;
    Integer time;
    Integer liftID;

    String line = req.getReader().readLine();
    String bodyJson = "";
    while (line != null) {
      bodyJson += line;
      line = req.getReader().readLine();
    }
//    System.out.println(bodyJson);
    JsonParser parser = new JsonParser();
    JsonElement element = parser.parse(bodyJson);
    JsonObject jsonObject = element.getAsJsonObject();

    resortID = jsonObject.get("resortID").getAsString();
    dayID = jsonObject.get("dayID").getAsInt();
    skierID = jsonObject.get("skierID").getAsInt();
    time = jsonObject.get("time").getAsInt();
    liftID = jsonObject.get("liftID").getAsInt();
    req.getReader().close();
    if (resortID == null || dayID == null || skierID == null || liftID == null || time == null) {
      sendPostFailure(res, "Request body is invalid for Skiers Post request");
      return;
    }

    // and now validate url path and return the response status code
    if (isValidSkierPostUrl(urlPath)) {
      boolean isSuccess = false;
      try {
        isSuccess = liftRideDao.createLiftRide(new LiftRide(resortID, dayID.toString(), skierID.toString(), time.toString(), liftID.toString()));
//        isSuccess = liftRideDao.createLiftRideTry(new LiftRide(resortID, dayID.toString(), skierID.toString(), time.toString(), liftID.toString()));
      }catch (NullPointerException e) {
        e.printStackTrace();
      }
      if (isSuccess) {
        sendPostSuccess(res);
      } else {
        sendPostFailure(res, "Fail to write in DB for Skiers Post request");
      }
    } else {
      sendPostFailure(res, "Wrong url for Skiers Post request");
    }
  }

  private boolean isValidSkierPostUrl(String urlPath) {
    return urlPath.matches("/liftrides");
  }

  private void sendPostSuccess(HttpServletResponse res) throws IOException {
    res.setStatus(HttpServletResponse.SC_CREATED);
//    res.getWriter().write(new Gson().toJson("Received Skiers Post request /liftrides"));
  }

  private void sendPostFailure(HttpServletResponse res, String msg) throws IOException {
    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    res.getWriter().write(new Gson().toJson(msg));
  }

  protected void doGet(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);
    res.setContentType("application/json");

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0 || urlPath.equals("/")) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//      res.getWriter().write(new Gson().toJson("{'message': 'missing parameters'}"));
      res.getWriter().write("{'message': 'missing parameters'}");
      return;
    }

    // validate url path and return the response status code
    String[] urlParts = urlPath.split("/");

    if (isValidSkierGetVerticalByDayUrl(urlPath)) {
      // e.g: /Mission Ridge/days/32/skiers/7889
      // days range 1 - 366
      String resortID = urlParts[1].toString();
      String dayID = urlParts[3].toString();
      String skierID = urlParts[5].toString();

      String totalLiftJson = liftRideDao.queryLiftRideVerticalPerDay(skierID, dayID);
      if (totalLiftJson != null) {
        Integer totalVertical = Integer.parseInt(totalLiftJson) * 10;
        sendGetSuccess(res, "{'resortID' :" +  resortID + ","
            + " 'totalVert' :" +  totalVertical.toString() + "}");
      } else {
        sendGetFailure(res, "{'message': '400 bad request, No data entry found'}");
      }
    } else if(isValidSkierGetVerticalByResortUrl(urlPath)) {
      // /skier/{skierID}/vertical?resort=Vail
      String skierID = urlParts[1].toString();
//      System.out.println(skierID);
      String[] resortParam = req.getParameterValues("resort");
      String resortID = resortParam[0];
//      System.out.println(resortParam[0].getClass().getName());
//      System.out.println(resortParam[0].getClass().getTypeName());
//      System.out.println(resortParam[0]);
      String totalLiftJson = liftRideDao.queryLiftRideVerticalPerResort(skierID, resortID);
      if (totalLiftJson != null) {
        Integer totalVertical = Integer.parseInt(totalLiftJson) * 10;
        sendGetSuccess(res, "{'resortID' :" +  resortID + ","
            + " 'totalVert' :" +  totalVertical.toString() + "}");
      } else {
        sendGetFailure(res, "{'message': '400 bad request, No data entry found'}");
      }
    } else {
      sendGetFailure(res, "{'message': '400 bad request'}");
    }
  }

  private boolean isValidSkierGetVerticalByDayUrl(String urlPath) {
    // e.g: /Mission Ridge/days/32/skiers/7889
    // days range 1 - 366
    String[] urlParts = urlPath.split("/");
    int dayID;
    try {
      dayID = Integer.parseInt(urlParts[3]);
    } catch (Exception e) {
      return false;
    }

    return urlPath.matches(
        "/(.*)"
            + "/days/([0-9]+)"
            + "/skiers/([0-9]+)")
        && ValueRange.of(1, 366).isValidIntValue(dayID);
  }

  private boolean isValidSkierGetVerticalByResortUrl(String urlPath) {
    // 2. /skiers/"skierID"/vertical
    // e.g:
//          System.out.println(urlPath.matches("/(.*)/vertical(.*)"));
    return urlPath.matches("/(.*)/vertical(.*)");
  }

  private void sendGetSuccess(HttpServletResponse res, String body) throws IOException {
    res.setStatus(HttpServletResponse.SC_OK);
//    String body = "{'resortID' : 'SilverMt',"
//        + " 'totalVert' : 56734}";
//    res.getWriter().write(new Gson().toJson(
//        body));
    res.getWriter().write(
        body);
//    res.getWriter().write(new Gson().toJson(
//        "Received Skiers Get request /{resortID}/days/{dayID}/skiers/{skierID}"));
  }

  private void sendGetFailure(HttpServletResponse res, String msg) throws IOException {
    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//    res.getWriter().write(new Gson().toJson(
//        "Invalid params for Skiers Get request "
//        + "/{resortID}/days/{dayID}/skiers/{skierID}"));
//    res.getWriter().write(new Gson().toJson(
//        "{'message': '404 bad request'}"));
    res.getWriter().write(
        msg);
  }

}

