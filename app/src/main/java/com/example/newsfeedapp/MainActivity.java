package com.example.newsfeedapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    ListView myListView;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> newsItems = new ArrayList<>();
    ArrayList<String> articleCodes = new ArrayList<>();

    MyAPIGetter myAPIGetter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myListView = (ListView) findViewById(R.id.myListView);
        myAPIGetter = new MyAPIGetter();
        myAPIGetter.execute(" https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        newsItems.add("Example Article");

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, newsItems);
        myListView.setAdapter(arrayAdapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ArticleViewer.class);
                startActivity(intent);
            }
        });
    }

    public class MyAPIGetter extends AsyncTask<String, Void, String> {

        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        @Override
        protected String doInBackground(String... urls) {

            try {
                System.out.println("doInBackground()");
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }


                return result;


            } catch (Exception e) {
                e.printStackTrace();
                return "failed";
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            s = s.substring(1, s.length() -1);
            Log.i("JSON", s);
            String[] arr = s.split(",");
            String code;

            for (int i = 0; i < arr.length; i++) {
               code = arr[i].trim();
               articleCodes.add(code);
            }

//            System.out.println(s);

            System.out.println("onPostExecute()");
////
//            try {
//                JSONObject jsonObject = new JSONObject(s);
////                String weatherInfo = jsonObject.getString("weather");
////                Log.i("weather info", weatherInfo);
//                JSONArray array = new JSONArray(jsonObject);
//
////                String articleCode;
//
//                for (int i = 0; i < array.length(); i++) {
//                    JSONObject jsonPart = array.getJSONObject(i);
//                    Log.i("code123", jsonPart.toString());
////                    Log.i("description", jsonPart.getString("description"));
//
////                    main = jsonPart.getString("main");
////                    description = jsonPart.getString("description");
////
////                    System.out.println("is this working " + main + " " + description);
//
//
                }
//
//                //here where to do something with the finished code
////                myMultiLine.setText(weatherDesc);
//

//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }
}