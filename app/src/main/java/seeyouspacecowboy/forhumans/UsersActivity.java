package seeyouspacecowboy.forhumans;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        ArrayList<String> usersList = new ArrayList<>();
        usersList.add("JJ");
        usersList.add("Jake");
        usersList.add("Blake");

        ListView usersListView = (ListView) findViewById(R.id.users_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, usersList
        );

        usersListView.setAdapter(adapter);

        UsersAsyncTask task = new UsersAsyncTask();
        task.execute();
    }

    private void updateUi(Event[] users){
        ArrayList<String> usersList = new ArrayList<>();
        for(int i = 0; i < users.length; i++){
            usersList.add(users[i].username);
        }

        ListView usersListView = (ListView) findViewById(R.id.users_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, usersList
        );

        usersListView.setAdapter(adapter);
    }

    private class UsersAsyncTask extends AsyncTask<URL, Void, Event[]> {
        private final String FOR_HUMANS_URL = "http://forhumans.herokuapp.com/v1/users";

        @Override
        protected Event[] doInBackground(URL... urls) {
            URL url = createUrl(FOR_HUMANS_URL);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {}

            Event[] users = extractFeatureFromJson(jsonResponse);

            return users;
        }

        protected void onPostExecute(Event[] users){
            if( users == null) return;
            updateUi(users);
        }


        private Event[] extractFeatureFromJson(String usersJSON) {
            try{
                JSONArray root = new JSONArray(usersJSON);

                JSONObject firstUser = root.getJSONObject(0);

                if (root != null) {
                    Event[] userEvents = new Event[root.length()];

                    for(int i = 0; i < root.length(); i++){
                        JSONObject user = root.getJSONObject(i);
                        userEvents[i] = new Event(user.getString("first_name"));
                    }

                    return userEvents;
                }
            } catch(JSONException e){}

            return null;
        }

        private URL createUrl(String stringURL){
            URL url = null;
            try {
                url = new URL(stringURL);
            } catch (MalformedURLException exception) {
                Log.e("HTTP URL request", "Error with creatign URL", exception);
            }

            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try{
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);

            } catch(IOException e) {} finally {
                if(urlConnection != null) urlConnection.disconnect();
                if(inputStream != null) inputStream.close();
            }

            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException{
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while(line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }

            return output.toString();
        }
    }
}
