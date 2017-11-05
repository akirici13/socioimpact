package com.dmpact.openxcdmpact.dummy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.dmpact.openxcdmpact.R;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    public static final String PREFS_NAME = "SessionRecords";

    public int levelUp = 1000;

    SharedPreferences settings;

    private RatingBar mRatingBar;

    private TextView mTotalPointsSum;
    private TextView mLevelText;
    private TextView mTotalTravels;
    private TextView mFirstBest;
    private TextView mSecondBest;
    private TextView mThirdBest;
    private TextView mFirstWorst;
    private TextView mSecondWorst;
    private TextView mThirdWorst;

    private int[] pointHistory;
    private double[] odometerHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_left_menu_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        mTotalPointsSum = (TextView) findViewById(R.id.totalPointsSum);
        mLevelText = (TextView) findViewById(R.id.levelText);
        mTotalTravels = (TextView) findViewById(R.id.totalTravels);
        mFirstBest = (TextView) findViewById(R.id.firstBest);
        mSecondBest = (TextView) findViewById(R.id.secondBest);
        mThirdBest = (TextView) findViewById(R.id.thirdBest);
        mFirstWorst = (TextView) findViewById(R.id.firstWorst);
        mSecondWorst = (TextView) findViewById(R.id.secondWorst);
        mThirdWorst = (TextView) findViewById(R.id.thirdWorst);

        settings = getSharedPreferences(PREFS_NAME, 0);
        int order = settings.getInt("order",0);

        mTotalTravels.setText("Total Travels: "+order);

        pointHistory = new int[order];
        odometerHistory = new double[order];
        int totalPoints = 0;
        for(int i = 0; i < order; i++) {
            pointHistory[i] = (int) Double.parseDouble(settings.getString("point"+i, "point"+i));
            totalPoints += pointHistory[i];
            odometerHistory[i] = Double.parseDouble(settings.getString("odometer"+i, "odometer"+i));
        }

        mTotalPointsSum.setText("Points Needed for Next Level: " + (levelUp - totalPoints));
        mRatingBar.setRating((totalPoints/levelUp) * mRatingBar.getNumStars());

        int min = Integer.MIN_VALUE;
        int max = min;
        int index = -1;
        for(int i = 0; i < order; i++) {
            if(pointHistory[i] > max) {
                max = pointHistory[i];
                index = i;
            }
        }

        mFirstBest.setText("1. " + max + " Points in "+String.format("%.2f", odometerHistory[index])+" kms");

        int second_max = min;
        index = -1;
        for(int i = 0; i < order; i++) {
            if(pointHistory[i] > second_max && pointHistory[i] < max) {
                second_max = pointHistory[i];
                index = i;
            }
        }

        mSecondBest.setText("2. "+second_max+" Points in "+String.format("%.2f", odometerHistory[index])+" kms");

        int third_max = min;
        index = -1;
        for(int i = 0; i < order; i++) {
            if(pointHistory[i] > third_max && pointHistory[i] < second_max) {
                third_max = pointHistory[i];
                index = i;
            }
        }

        mThirdBest.setText("3. "+third_max+" Points in "+String.format("%.2f", odometerHistory[index])+" kms");

        max = Integer.MAX_VALUE;
        min = max;
        index = -1;
        for(int i = 0; i < order; i++) {
            if(pointHistory[i] < min) {
                min = pointHistory[i];
                index = i;
            }
        }

        mFirstWorst.setText("1. "+min+" Points in "+String.format("%.2f", odometerHistory[index])+" kms");

        int second_min = max;
        index = -1;
        for(int i = 0; i < order; i++) {
            if(pointHistory[i] < second_min && pointHistory[i] > min) {
                second_min = pointHistory[i];
                index = i;
            }
        }

        mSecondWorst.setText("2. "+second_min+" Points in " +String.format("%.2f", odometerHistory[index])+ " kms");

        int third_min = max;
        index = -1;
        for(int i = 0; i < order; i++) {
            if(pointHistory[i] < third_min && pointHistory[i] > second_min) {
                third_min = pointHistory[i];
                index = i;
            }
        }

        mThirdWorst.setText("3. "+third_min+" Points in "+String.format("%.2f", odometerHistory[index]) + " kms");
    }

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_current) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_rewards) {
            Intent i = new Intent(getApplicationContext(),RewardsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_society) {
            Intent i = new Intent(getApplicationContext(),SocietyActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_history) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.left_menu, menu);
        return true;
    }
}
