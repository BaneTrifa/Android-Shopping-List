package branko.trifkovic.shoppinglist.other;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {

    private static final int SUCCESS = 200;

    /*HTTP get json Array*/
    public JSONArray getJSONArrayFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        java.net.URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        /*header fields*/
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        try {
            urlConnection.connect();
        } catch (IOException e) {
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        String jsonString = sb.toString();
        Log.d("HTTP GET", "JSON data- " + jsonString);
        int responseCode =  urlConnection.getResponseCode();
        urlConnection.disconnect();


        return responseCode == SUCCESS ? new JSONArray(jsonString) : null;
    }

    /*HTTP get json object*/
    public JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        java.net.URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        /*header fields*/
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        try {
            urlConnection.connect();
        } catch (IOException e) {
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        Log.d("HTTP GET", "JSON obj- " + jsonString);
        int responseCode =  urlConnection.getResponseCode();
        urlConnection.disconnect();
        return responseCode == SUCCESS ? new JSONObject(jsonString) : null;
    }

    /*HTTP post*/
    public boolean postJSONObjectFromURL(String urlString, JSONObject jsonObject) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        java.net.URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        urlConnection.setRequestProperty("Accept","application/json");
        /*needed when used POST or PUT methods*/
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        try {
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
        /*write json object*/
        os.writeBytes(jsonObject.toString());
        os.flush();
        os.close();
        int responseCode =  urlConnection.getResponseCode();
        Log.i("STATUS", String.valueOf(urlConnection.getResponseCode()));
        Log.i("MSG" , urlConnection.getResponseMessage());
        urlConnection.disconnect();
        return (responseCode==SUCCESS);
    }

    /*HTTP delete*/
    public boolean httpDelete(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        java.net.URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("DELETE");
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        urlConnection.setRequestProperty("Accept","application/json");
        try {
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        int responseCode = urlConnection.getResponseCode();

        Log.i("STATUS", String.valueOf(responseCode));
        Log.i("MSG" , urlConnection.getResponseMessage());
        urlConnection.disconnect();
        return (responseCode==SUCCESS);
    }
}
