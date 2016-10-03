package org.inspirecenter.uclancyprusguide.ui;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.inspirecenter.uclancyprusguide.R;

public class ActivityHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "uclan-cy";

    public static final int FRAGMENT_ID_NEWS    = 0x010;
    public static final int FRAGMENT_ID_GALLERY = 0x020;
    public static final int FRAGMENT_ID_CONTACT = 0x030;

    public static final String SELECTED_FRAGMENT_KEY = "selected-fragment";

    final FragmentManager fragmentManager = getFragmentManager();

    private int selectedFragment = 0;

    private FragmentNews fragmentNews = new FragmentNews();
    private FragmentGallery fragmentGallery = new FragmentGallery();
    private FragmentContact fragmentContact = new FragmentContact();

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(selectedFragment == 0) {
            selectedFragment = PreferenceManager.getDefaultSharedPreferences(this).getInt(SELECTED_FRAGMENT_KEY, FRAGMENT_ID_NEWS);
            selectFragment();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SELECTED_FRAGMENT_KEY, selectedFragment).apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_news) {
            selectedFragment = FRAGMENT_ID_NEWS;
            selectFragment();
        } else if (id == R.id.nav_gallery) {
            selectedFragment = FRAGMENT_ID_GALLERY;
            selectFragment();
        } else if (id == R.id.nav_contact) {
            selectedFragment = FRAGMENT_ID_CONTACT;
            selectFragment();
        } else if (id == R.id.action_personal_timetable) {
            Toast.makeText(ActivityHome.this, R.string.Not_available_yet, Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_room_timetable) {
            startActivity(new Intent(this, ActivityRoomTimetable.class));
        } else if (id == R.id.action_attendance) {
            startActivity(new Intent(this, ActivityAttendance.class));
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, ActivityAbout.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null) drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectFragment() {
        if (selectedFragment == FRAGMENT_ID_NEWS) {
            // Handle the news action
            toolbar.setSubtitle(getString(R.string.News));
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentNews).commit();
            fragmentManager.executePendingTransactions();
        } else if (selectedFragment == FRAGMENT_ID_GALLERY) {
            toolbar.setSubtitle(getString(R.string.Gallery));
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentGallery).commit();
            fragmentManager.executePendingTransactions();
Toast.makeText(ActivityHome.this, R.string.Not_available_yet, Toast.LENGTH_SHORT).show();
        } else if (selectedFragment == FRAGMENT_ID_CONTACT) {
            toolbar.setSubtitle(getString(R.string.Contact));
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentContact).commit();
            fragmentManager.executePendingTransactions();
        } else {
            Log.e(TAG, "Unknown selected fragment: " + selectedFragment);
        }
    }
}
