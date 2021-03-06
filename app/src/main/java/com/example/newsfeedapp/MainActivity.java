package com.example.newsfeedapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.Replaceable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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
    ArrayList<String> jsonList = new ArrayList<>();

    MyCodeGetter myCodeGetter;
    MyAPIGetter myAPIGetter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpListView();
        getAPIs();
    }

    private void getAPIs() {
        myCodeGetter = new MyCodeGetter();
        myCodeGetter.execute(" https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
    }

    private void setUpListView() {

        myListView = (ListView) findViewById(R.id.myListView);
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

    public class MyAPIGetter extends AsyncTask<String, Void, ArrayList<String>> {
        ArrayList<String> result = new ArrayList<>();
        String resultString = "";
        URL url;
        HttpURLConnection urlConnection = null;

        @Override
        protected ArrayList<String> doInBackground(String... urls) {



            try {
//                System.out.println("doInBackground()");
                for (int i = 0; i < urls.length; i++) {

                    url = new URL(urls[i]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);
                    int data = reader.read();

                    while (data != -1) {
                        char current = (char) data;
                        resultString += current;
                        data = reader.read();
                    }

                    if(i == 0) {
                        Log.i("RESULT STRING = ", resultString);
                        result.add(resultString);
                    }
//                    System.out.println("==================IS THIS WORKING=================" + resultString);

                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                ArrayList<String> failedList = new ArrayList<>();
                failedList.add("failed");
                return failedList;
            }

        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);
            SQLiteDatabase db = getApplicationContext().openOrCreateDatabase("JsonInfo", MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS articleList (id INTEGER PRIMARY KEY, article_id INT(8), title VARCHAR, articleURL NVARCHAR)");
//            System.out.println("==================ONPOSTEXECUTE() WORKING=================" + s.toString());


            for (String json: s) {

            try {
                JSONObject jsonObject = new JSONObject(json);
                String url = jsonObject.getString("url");
                int id = jsonObject.getInt("id");
                String title = jsonObject.getString("title");
                title = DatabaseUtils.sqlEscapeString(title);
                title = title.replaceAll(":","");

                Log.i("title123", title);
                Log.i("url123", url);
                Log.i("id123", String.valueOf(id));

                //===NEED TO FIX THIS LINE====
                db.execSQL("INSERT INTO articleList (article_id, title, articleURL) VALUES ("+id+", "+title+", "+url+" )");


                newsItems.add(title);
                arrayAdapter.notifyDataSetChanged();

                Cursor c = db.rawQuery("SELECT * FROM articleList", null);
                int idIndex = c.getColumnIndex("id");
                int article_idIndex = c.getColumnIndex("article_id");
                int titleIndex = c.getColumnIndex("title");
                int articleURL = c.getColumnIndex("articleURL");

                c.moveToFirst();
                while(!c.isAfterLast()){
                    Log.i("db123",
                            " " + c.getInt(idIndex)
                                    + ": " + c.getInt(article_idIndex)
                                    + ": " + c.getString(titleIndex )
                                    + ": " + c.getString(articleURL ) );

                    c.moveToNext();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            }

//            System.out.println("==============API CODE============== " + s);

        }
    }






    public class MyCodeGetter extends AsyncTask<String, Void, String> {

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

            s = s.trim();
            s = s.substring(1, s.length() - 1);
//            Log.i("JSON", s);
            String[] arr = s.split(",");
            String code;
            String[] articleURLs = new String[50];

            for (int i = 0; i < 50; i++) {
                code = arr[i].trim();
                articleURLs[i] = "https://hacker-news.firebaseio.com/v0/item/"+ code +".json?print=pretty";
                Log.i("urlList", articleURLs[i]);
            }

            myAPIGetter = new MyAPIGetter();
            myAPIGetter.execute(articleURLs);
        }
    }
}