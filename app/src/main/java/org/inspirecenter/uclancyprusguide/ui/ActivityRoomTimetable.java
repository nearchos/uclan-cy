package org.inspirecenter.uclancyprusguide.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.inspirecenter.uclancyprusguide.R;
import org.inspirecenter.uclancyprusguide.data.TimetableSession;
import org.inspirecenter.uclancyprusguide.sync.Parser;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class ActivityRoomTimetable extends AppCompatActivity {

    public static final String TAG = "uclan-cy";

    private Intent scanBarcodeIntent;

    private ProgressBar progressBar;
    private TextView textView;
    private ListView listView;
    private Spinner roomSpinner;
    private String [] rooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_timetable);

        final ActionBar actionBar =  getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        roomSpinner = (Spinner) findViewById(R.id.activity_room_timetable_spinner);
        rooms = getResources().getStringArray(R.array.uclan_cy_rooms);
        final SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, rooms);
        roomSpinner.setAdapter(spinnerAdapter);
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0) asyncCall(rooms[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(ActivityRoomTimetable.this, R.string.Nothing_selected, Toast.LENGTH_SHORT).show();
            }
        });

        final Button scanBarcode = (Button) findViewById(R.id.activity_room_timetable_scan_barcode);
        assert scanBarcode != null;
        scanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(scanBarcodeIntent, 0);
            }
        });

        scanBarcodeIntent = new Intent("com.google.zxing.client.android.SCAN");
        scanBarcodeIntent.setPackage("com.google.zxing.client.android");
        scanBarcodeIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");

        // if activity is not available, hide scan button
        if(scanBarcodeIntent.resolveActivity(getPackageManager()) == null) {
            scanBarcode.setVisibility(View.GONE);
        }

        progressBar = (ProgressBar) findViewById(R.id.activity_room_timetable_progress_bar);
        textView = (TextView) findViewById(R.id.activity_room_timetable_text_view);
        listView = (ListView) findViewById(R.id.activity_room_timetable_list_view);
    }

    public static final String SERVICE_PREFIX_TIMETABLE = "http://uclan-cy.appspot.com/timetable?room=";

    private int findRoom(final String roomCode) {
        if(rooms != null) {
            for(int i = 0; i < rooms.length; i++) {
                final String room = rooms[i];
                if(room.equalsIgnoreCase(roomCode)) return i;
            }
        }
        return -1;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
                Toast.makeText(this, "Scanned: " + contents + ", of format: " + format, Toast.LENGTH_SHORT).show();
                if(contents != null && contents.startsWith(SERVICE_PREFIX_TIMETABLE)) {
                    // handle scanned url
                    final String roomCode = contents.substring(SERVICE_PREFIX_TIMETABLE.length());
                    final int position = findRoom(roomCode);
                    if(position == -1) {
                        Toast.makeText(this, getString(R.string.Unknown_room, roomCode), Toast.LENGTH_SHORT).show();
                    } else {
                        roomSpinner.setSelection(position, true);
                        asyncCall(roomCode);
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Toast.makeText(this, R.string.Cancelled, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void asyncCall(final String room) {
        new AsyncFetchTimetableForRoom().execute(room);
    }

    private class AsyncFetchTimetableForRoom extends AsyncTask<String, Void, String> {
        @Override protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            textView.setText(R.string.Fetching_timetable);
        }

        @Override protected String doInBackground(String... params) {
            return fetchTimetableForRoom(params[0]);
        }
        @Override protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            final Locale currentLocale = getResources().getConfiguration().locale;
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, LLL d", currentLocale);
            textView.setText(getString(R.string.Showing_data_for, simpleDateFormat.format(new Date())));
            updateContent(result);
        }
    }

    public static final String BASE_URL = "https://cyprustimetable.uclan.ac.uk/TimetableAPI/TimetableWebService.asmx/getRoomTimetable?securityToken=$$SECURITY_TOKEN$$&ROOM_CODE=";

    private String fetchTimetableForRoom(final String roomCode) {

        final String securityToken = getString(R.string.timetable_security_token);

        // form and encode URL (only parameters need encoding)
        String encodedURL = BASE_URL.replace("$$SECURITY_TOKEN$$", securityToken) + roomCode; // URLEncoder.encode(getString(R.string.magic), "UTF-8");
        // send get
        HttpURLConnection httpURLConnection = null;
        try {
            final URL url = new URL(encodedURL);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            final InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

            // handle response
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(reader.readLine()).append("\n");
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            inputStream.close();
            return stringBuilder.toString();
        } catch (IOException ioe) {
            Log.e(TAG, "I/O error while fetching timetable for room: " + roomCode, ioe);
            return "I/O error while fetching timetable for room: " + roomCode;
        } finally {
            if(httpURLConnection != null) httpURLConnection.disconnect();
        }
    }

    private void updateContent(final String xml) {
        // check if XML is null or empty
        if(xml == null || xml.isEmpty()) {
            Log.e(TAG, "Error: null or empty reply: " + xml);
        } else if(!xml.startsWith("{") || !xml.startsWith("[")) {
//            Log.d(TAG, "JSON inside XML reply: " + xml);
            final String json = Parser.extractJSON(xml);
            Log.d(TAG, "json: " + json);
            if(json == null) {
                textView.setText(R.string.No_sessions_found_for_this_room_today);
            } else {
                updateContentWithJson(json);
            }
        }
    }

    private void updateContentWithJson(final String json) {
        try {
            final TimetableSession [] timetableSessions = Parser.getRoomTimetableSessions(json);
            Log.d(TAG, "timetableSessions: " + Arrays.toString(timetableSessions));
            if(timetableSessions == null || timetableSessions.length == 0) {
                textView.setText(R.string.No_sessions_found_for_this_room_today);
            } else {
                Arrays.sort(timetableSessions);
                Log.d(TAG, "sorted timetableSessions: " + Arrays.toString(timetableSessions));
                final TimetableSessionAdapter timetableSessionAdapter = new TimetableSessionAdapter(this, timetableSessions);
                Log.d(TAG, "timetableSessionAdapter: " + timetableSessionAdapter);
                listView.setAdapter(timetableSessionAdapter);
                listView.invalidate();
            }
        } catch (JSONException jsone) {
            Log.d(TAG, "timetableSessionAdapter: " + jsone.getMessage());
            textView.setText(getString(R.string.Error_for, jsone.getMessage()));
        }

    }
}