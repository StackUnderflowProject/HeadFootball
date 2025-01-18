package si.um.feri.project.map.utils;

import com.badlogic.gdx.Gdx;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import si.um.feri.project.map.model.Event;
import si.um.feri.project.map.model.Host;
import si.um.feri.project.map.model.Match;
import si.um.feri.project.map.model.Stadium;
import si.um.feri.project.map.model.TeamRecord;

public class Api {
    public static ArrayList<Match> fetchEvents(String sportType, String startDate, String endDate) throws URISyntaxException, IOException {
        String urlString = "http://" + Constants.SERVER_IP + ":3000/" + sportType + "/filterByDateRange/" + startDate + "/" + endDate;
        String jsonString = getJsonResponse(urlString);
        return parseEvents(jsonString);
    }

    private static String getJsonResponse(String urlString) throws URISyntaxException, IOException {
        URL url = new URI(urlString).toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }

        // Read response
        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        connection.disconnect();

        return response.toString();
    }

    private static ArrayList<Match> parseEvents(String jsonString) {
        ArrayList<Match> matches = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            Match match = new Match();
            match._id = jsonObject.getString("_id");

            try {
                match.date = dateFormat.parse(jsonObject.getString("date"));
            } catch (ParseException e) {
                Gdx.app.log("Event", "Failed to parse date: " + jsonObject.getString("date"));
            }

            match.time = jsonObject.optString("time", "");
            match.score = jsonObject.optString("score", "");
            match.location = jsonObject.optString("location", "");
//            match.season = jsonObject.getInt("season");

            JSONObject homeTeam = jsonObject.getJSONObject("home");
            match.home = new TeamRecord();
            match.home._id = homeTeam.getString("_id");
            match.home.name = homeTeam.getString("name");
            match.home.logoPath = homeTeam.getString("logoPath");

            JSONObject awayTeam = jsonObject.getJSONObject("away");
            match.away = new TeamRecord();
            match.away._id = awayTeam.getString("_id");
            match.away.name = awayTeam.getString("name");
            match.away.logoPath = awayTeam.getString("logoPath");

            JSONObject stadiumObject = jsonObject.getJSONObject("stadium");
            match.stadium = new Stadium();
            match.stadium._id = stadiumObject.getString("_id");
            match.stadium.name = stadiumObject.optString("name", "");
//            match.stadium.season = stadiumObject.getInt("season");

            JSONObject locationObject = stadiumObject.getJSONObject("location");
            JSONArray coordinates = locationObject.getJSONArray("coordinates");
            match.stadium.location = new Geolocation(coordinates.getDouble(0), coordinates.getDouble(1));

            matches.add(match);
        }

        return matches;
    }

    public static ArrayList<Event> fetchUserEvents() throws URISyntaxException, IOException {
        String urlString = "http://localhost:3000/events/upcoming";
        String jsonString = getJsonResponse(urlString);
        return parseUserEvents(jsonString);
    }

    private static ArrayList<Event> parseUserEvents(String jsonString) {
        ArrayList<Event> events = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            Event event = new Event();
            event._id = jsonObject.getString("_id");
            event.name = jsonObject.getString("name");
            event.description = jsonObject.getString("description");
            event.activity = jsonObject.getString("activity");
            event.date = jsonObject.getString("date");
            event.time = jsonObject.getString("time");
//            event.image = jsonObject.getString("image") != null ? jsonObject.getString("image") : "";
            event.predicted_count = jsonObject.getInt("predicted_count");

            // Parse host
            JSONObject hostObj = jsonObject.getJSONObject("host");
            Host host = new Host();
            host._id = hostObj.getString("_id");
            host.username = hostObj.getString("username");
            host.email = hostObj.getString("email");
            event.host = host;

            // Parse location
            JSONObject locationObj = jsonObject.getJSONObject("location");
            JSONArray coordinates = locationObj.getJSONArray("coordinates");
            event.location = new Geolocation(coordinates.getDouble(1), coordinates.getDouble(0));

            // Parse followers
            JSONArray followersArray = jsonObject.getJSONArray("followers");
            event.followers = new ArrayList<>();
            for (int j = 0; j < followersArray.length(); j++) {
                event.followers.add(followersArray.getString(j));
            }

            events.add(event);
        }

        System.out.println(events.size());

        return events;
    }
}
