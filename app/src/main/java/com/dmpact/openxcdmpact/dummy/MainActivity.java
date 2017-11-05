package com.dmpact.openxcdmpact.dummy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmpact.openxcdmpact.R;
import com.openxc.VehicleManager;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.VehicleSpeed;

import java.util.ArrayList;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private static final String TAG = "StarterActivity";

    SharedPreferences settings;

    private boolean carRunning = false;
    private boolean sessionSaved = true;

    private VehicleManager mVehicleManager;

    private LinearLayout mPointLayout;
    private LinearLayout mAvgVehicleSpeedLayout;
    private LinearLayout mPedalPositionLayout;
    private LinearLayout mCO2EmissionLayout;
    private LinearLayout mFuelConsumptionLayout;
    private LinearLayout mOdometerLayout;

    private TextView mPointView;
    private TextView mAvgVehicleSpeedView;
    private TextView mPedalPositionView;
    private TextView mCO2EmissionView;
    private TextView mFuelConsumptionView;
    private TextView mOdometerView;

    private ArrayList<VehicleSpeed> vehicleSpeeds;
    private ArrayList<AcceleratorPedalPosition> pedalPositions;
    private ArrayList<Odometer> odometers;
    private ArrayList<FuelConsumed> fuels;

    private final int CO2EmissionCoefficient = 2640; //GramPerLiter

    private final double criteriaAvgVehicleSpeed = 90;
    private final double criteriaPedalPosition = 50;
    private final double criteriaCO2Emission = 150;
    private final double criteriaFuelConsumption = 0.067; //LitersPerKilometer
    private final double criteriaMinimumDistance = 2;

    private final double pointAvgVehicleSpeed = 5;
    private final double pointPedalPosition = 5;
    private final double pointCO2Emission = 10;
    private final double pointFuelConsumption = 10;
    private final double pointMinimumDistance = 5;

    private double avgVehicleSpeedFinal;
    private double pedalPositionFinal;
    private double CO2EmissionFinal;
    private double fuelConsumptionFinal;
    private double odometerFinal;

    public static final String PREFS_NAME = "SessionRecords";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_left_menu);
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
        // grab a reference to the engine speed text object in the UI, so we can
        // manipulate its value later from Java code

        mPointLayout = (LinearLayout) findViewById(R.id.pointListView);
        mAvgVehicleSpeedLayout = (LinearLayout) findViewById(R.id.avgSpeedListView);
        mPedalPositionLayout = (LinearLayout) findViewById(R.id.pedalPositionListView);
        mCO2EmissionLayout = (LinearLayout) findViewById(R.id.CO2EmissionListView);
        mFuelConsumptionLayout = (LinearLayout) findViewById(R.id.fuelConsumptionView);
        mOdometerLayout = (LinearLayout) findViewById(R.id.odometerListView);

        mPointView = (TextView) findViewById(R.id.pointValue);
        mAvgVehicleSpeedView = (TextView) findViewById(R.id.avgSpeedValue);
        mPedalPositionView = (TextView) findViewById(R.id.pedalPositionValue);
        mCO2EmissionView = (TextView) findViewById(R.id.CO2EmissionValue);
        mFuelConsumptionView = (TextView) findViewById(R.id.fuelConsumptionValue);
        mOdometerView = (TextView) findViewById(R.id.odometerValue);

        settings = getSharedPreferences(PREFS_NAME, 0);

        int lastIndex = settings.getInt("order",0);
        if(lastIndex != 0) {
            final double lastRecordedPoint = Double.parseDouble(settings.getString("points" + lastIndex, "-1"));
            final double lastRecordedAvgVehicle = Double.parseDouble(settings.getString("avg_vehicle" + lastIndex, "-1"));
            final double lastRecordedPedal = Double.parseDouble(settings.getString("pedal" + lastIndex, "-1"));
            final double lastRecordedCO2Emission = Double.parseDouble(settings.getString("co2_emission" + lastIndex, "-1"));
            final double lastRecordedFuelConsumption = Double.parseDouble(settings.getString("fuel_consumption" + lastIndex, "-1"));
            final double lastRecordedOdomoter = Double.parseDouble(settings.getString("odometer" + lastIndex, "-1"));

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mPointView.setText(String.format("%.2f", lastRecordedPoint));
                    mAvgVehicleSpeedView.setText(String.format("%.2f", lastRecordedAvgVehicle));
                    mPedalPositionView.setText(String.format("%.2f", lastRecordedPedal));
                    mCO2EmissionView.setText(String.format("%.2f", lastRecordedCO2Emission));
                    mFuelConsumptionView.setText(String.format("%.2f", lastRecordedFuelConsumption));
                    mOdometerView.setText(String.format("%.2f", lastRecordedOdomoter));

                    if(lastRecordedPoint > 0) {
                        mPointLayout.setBackgroundColor(Color.GREEN);
                    }else{
                        mPointLayout.setBackgroundColor(Color.RED);
                    }

                    if(lastRecordedAvgVehicle < criteriaAvgVehicleSpeed) {
                        mAvgVehicleSpeedLayout.setBackgroundColor(Color.GREEN);
                    }else{
                        mAvgVehicleSpeedLayout.setBackgroundColor(Color.RED);
                    }

                    if(lastRecordedPedal < criteriaPedalPosition) {
                        mPedalPositionLayout.setBackgroundColor(Color.GREEN);
                    }else{
                        mPedalPositionLayout.setBackgroundColor(Color.RED);
                    }

                    if(lastRecordedCO2Emission < criteriaCO2Emission) {
                        mCO2EmissionLayout.setBackgroundColor(Color.GREEN);
                    }else{
                        mCO2EmissionLayout.setBackgroundColor(Color.RED);
                    }

                    if(lastRecordedFuelConsumption < criteriaFuelConsumption) {
                        mFuelConsumptionLayout.setBackgroundColor(Color.GREEN);
                    }else{
                        mFuelConsumptionLayout.setBackgroundColor(Color.RED);
                    }

                    if(lastRecordedOdomoter < criteriaMinimumDistance) {
                        mOdometerLayout.setBackgroundColor(Color.GREEN);
                    }else{
                        mOdometerLayout.setBackgroundColor(Color.RED);
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if (mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            // Remember to remove your listeners, in typical Android
            // fashion.
            mVehicleManager.removeListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.removeListener(IgnitionStatus.class, mIgnitionStatusListener);
            mVehicleManager.removeListener(AcceleratorPedalPosition.class, mAcceleratorPedalPositionListener);
            mVehicleManager.removeListener(FuelConsumed.class, mFuelConsumedListener);
            mVehicleManager.removeListener(Odometer.class, mOdometerListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if (mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        int lastIndex = settings.getInt("order",0);
        final double lastRecordedPoint = Double.parseDouble(settings.getString("points"+lastIndex, "-1"));
        final double lastRecordedAvgVehicle = Double.parseDouble(settings.getString("avg_vehicle"+lastIndex, "-1"));
        final double lastRecordedPedal = Double.parseDouble(settings.getString("pedal"+lastIndex, "-1"));
        final double lastRecordedCO2Emission = Double.parseDouble(settings.getString("co2_emission"+lastIndex, "-1"));
        final double lastRecordedFuelConsumption = Double.parseDouble(settings.getString("fuel_consumption"+lastIndex, "-1"));
        final double lastRecordedOdomoter = Double.parseDouble(settings.getString("odometer"+lastIndex, "-1"));

        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mPointView.setText(String.format("%.2f",lastRecordedPoint));
                mAvgVehicleSpeedView.setText(String.format("%.2f",lastRecordedAvgVehicle));
                mPedalPositionView.setText(String.format("%.2f",lastRecordedPedal));
                mCO2EmissionView.setText(String.format("%.2f",lastRecordedCO2Emission));
                mFuelConsumptionView.setText(String.format("%.2f",lastRecordedFuelConsumption));
                mOdometerView.setText(String.format("%.2f",lastRecordedOdomoter));

                if(lastRecordedPoint > 0) {
                    mPointLayout.setBackgroundColor(Color.GREEN);
                }else{
                    mPointLayout.setBackgroundColor(Color.RED);
                }

                if(lastRecordedAvgVehicle < criteriaAvgVehicleSpeed) {
                    mAvgVehicleSpeedLayout.setBackgroundColor(Color.GREEN);
                }else{
                    mAvgVehicleSpeedLayout.setBackgroundColor(Color.RED);
                }

                if(lastRecordedPedal < criteriaPedalPosition) {
                    mPedalPositionLayout.setBackgroundColor(Color.GREEN);
                }else{
                    mPedalPositionLayout.setBackgroundColor(Color.RED);
                }

                if(lastRecordedCO2Emission < criteriaCO2Emission) {
                    mCO2EmissionLayout.setBackgroundColor(Color.GREEN);
                }else{
                    mCO2EmissionLayout.setBackgroundColor(Color.RED);
                }

                if(lastRecordedFuelConsumption < criteriaFuelConsumption) {
                    mFuelConsumptionLayout.setBackgroundColor(Color.GREEN);
                }else{
                    mFuelConsumptionLayout.setBackgroundColor(Color.RED);
                }

                if(lastRecordedOdomoter < criteriaMinimumDistance) {
                    mOdometerLayout.setBackgroundColor(Color.GREEN);
                }else{
                    mOdometerLayout.setBackgroundColor(Color.RED);
                }
            }
        });
    }

    VehicleSpeed.Listener mVehicleSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            if (vehicleSpeeds != null) {
                vehicleSpeeds.add(speed);
            }
        }
    };

    IgnitionStatus.Listener mIgnitionStatusListener = new IgnitionStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final IgnitionStatus status = (IgnitionStatus) measurement;
            if (status.getValue().enumValue() == IgnitionStatus.IgnitionPosition.START) {
                Log.d("Ignition", "Ignition status: start");
                vehicleSpeeds = new ArrayList<>();
                carRunning = true;
            } else if (status.getValue().enumValue() == IgnitionStatus.IgnitionPosition.OFF) {
                Log.d("Ignition", "Ignition status: off");

                if (vehicleSpeeds != null && carRunning) {
                    int totalSpeed = 0;
                    for (int i = 0; i < vehicleSpeeds.size(); i++) {
                        totalSpeed += vehicleSpeeds.get(i).getValue().intValue();
                    }
                    final int speed = totalSpeed;
                    avgVehicleSpeedFinal = (double) speed / vehicleSpeeds.size();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            // Finally, we've got a new value and we're running on the
                            // UI thread - we set the text of the EngineSpeed view to
                            // the latest value
                            mAvgVehicleSpeedView.setText(String.format("%.2f",
                                    + (double) speed / vehicleSpeeds.size()));
                        }
                    });
                }
                carRunning = false;
            }
        }
    };

    AcceleratorPedalPosition.Listener mAcceleratorPedalPositionListener = new AcceleratorPedalPosition.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final AcceleratorPedalPosition pedalPosition = (AcceleratorPedalPosition) measurement;
            if(carRunning) {
                if (pedalPosition.getValue().intValue() > 0) {
                    if (pedalPositions == null) {
                        pedalPositions = new ArrayList<>();
                    }
                    pedalPositions.add(pedalPosition);
                }
            } else {
                if (pedalPositions != null && pedalPositions.size() != 0) {
                    int totalPedalPosition = 0;
                    for (int i = 0; i < pedalPositions.size(); i++) {
                        totalPedalPosition += pedalPositions.get(i).getValue().doubleValue();
                    }
                    final double position = totalPedalPosition;
                    pedalPositionFinal = position / pedalPositions.size();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            // Finally, we've got a new value and we're running on the
                            // UI thread - we set the text of the AcceleratorPedalPosition view to
                            // the latest value
                            mPedalPositionView.setText(String.format("%.2f", pedalPositionFinal));
                        }
                    });
                }
            pedalPositions = new ArrayList<>();
        }
        }
    };

    FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener() {
        @Override
        public void receive(Measurement measurement) {
        FuelConsumed fuelConsumed = (FuelConsumed) measurement;
        if (carRunning) {
            if (fuelConsumed.getValue().doubleValue() > 0) {
                if (fuelConsumed == null) {
                    fuels = new ArrayList<>();
                }
                fuels.add(fuelConsumed);
            }
        }else{
            if (fuels != null && fuels.size() != 0) {
                double maxFuelConsumption = fuels.get(fuels.size() - 1).getValue().doubleValue();
                double minFuelConsumption = fuels.get(0).getValue().doubleValue();
                final double totalFuelConsumed = maxFuelConsumption - minFuelConsumption;
                fuelConsumptionFinal = totalFuelConsumed / odometerFinal;
                CO2EmissionFinal = ((double) CO2EmissionCoefficient) * fuelConsumptionFinal;
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mFuelConsumptionView.setText(String.format("%.2f", fuelConsumptionFinal*100));
                        mCO2EmissionView.setText(String.format("%.2f", ((double) CO2EmissionCoefficient) * fuelConsumptionFinal));
                    }
                });
            }
            fuels = new ArrayList<>();
        }
        }
    };

    Odometer.Listener mOdometerListener = new Odometer.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Odometer odometer = (Odometer) measurement;
            if (carRunning) {
                if (odometer.getValue().intValue() != 0) {
                    if (odometer == null) {
                        odometers = new ArrayList<>();
                    }
                    odometers.add(odometer);
                }
            } else {
                if (odometers != null && odometers.size() != 0) {
                    double maxOdometer = odometers.get(odometers.size() - 1).getValue().doubleValue();
                    double minOdometer = odometers.get(0).getValue().doubleValue();
                    final double distanceTravelled = maxOdometer - minOdometer;
                    odometerFinal = distanceTravelled;
                    sessionSaved = FALSE;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            // Finally, we've got a new value and we're running on the
                            // UI thread - we set the text of the Odometer view to
                            // the latest value
                            mOdometerView.setText(String.format("%.2f", distanceTravelled));
                        }
                    });
                }
                odometers = new ArrayList<>();
                pointAssessment();
            }
        }
    };

    private void pointAssessment() {
        if (!carRunning && !sessionSaved) {
            final double lastSessionPoint = comparison(criteriaAvgVehicleSpeed, avgVehicleSpeedFinal, pointAvgVehicleSpeed, mAvgVehicleSpeedLayout)
            + comparison(criteriaPedalPosition, pedalPositionFinal, pointPedalPosition, mPedalPositionLayout)
            + comparison(criteriaCO2Emission, CO2EmissionFinal, pointCO2Emission, mCO2EmissionLayout)
            + comparison(criteriaFuelConsumption, fuelConsumptionFinal, pointFuelConsumption, mFuelConsumptionLayout)
            + comparison(criteriaMinimumDistance, odometerFinal, pointMinimumDistance, mOdometerLayout);

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    // Finally, we've got a new value and we're running on the
                    // UI thread - we set the text of the Odometer view to
                    // the latest value
                    if(lastSessionPoint > 0) {
                        mPointLayout.setBackgroundColor(Color.GREEN);
                    }else{
                        mPointLayout.setBackgroundColor(Color.RED);
                    }
                    mPointView.setText(String.format("%.2f", lastSessionPoint));
                }
            });
            int order;
            if(!settings.contains("order")) {
                order = 1;
            }else {
                order = settings.getInt("order", 0);
            }
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("point"+order, Double.toString(lastSessionPoint));
            editor.putString("avg_vehicle"+order, Double.toString(avgVehicleSpeedFinal));
            editor.putString("pedal"+order, Double.toString(pedalPositionFinal));
            editor.putString("co2_emission"+order, Double.toString(CO2EmissionFinal));
            editor.putString("fuel_consumption"+order, Double.toString(fuelConsumptionFinal));
            editor.putString("odometer"+order, Double.toString(odometerFinal));

            if(!settings.contains("order")) {
                editor.putInt("order", order + 1);
            }else{
                editor.remove("order");
                editor.putInt("order", order + 1);
            }
            editor.commit();
            sessionSaved = TRUE;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

            b.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_menu_camera)
                    .setTicker("Hearty365")
                    .setContentTitle("You just got a chance to make world a better place!")
                    .setContentText("Look at your travel performance and assess your environment-friendliness")
                    .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntent)
                    .setContentInfo("Info");

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, b.build());
        }
    }

    private double comparison(double criteria, double finalValue, double point, LinearLayout layout) {
        final LinearLayout layout2 = layout;
        if(criteria > finalValue) {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    layout2.setBackgroundColor(Color.GREEN);
                }
            });
            return point;
        }else{
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    layout2.setBackgroundColor(Color.RED);
                }
            });
            return -point;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is
        // established, i.e. bound.
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes
            mVehicleManager.addListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.addListener(IgnitionStatus.class, mIgnitionStatusListener);
            mVehicleManager.addListener(AcceleratorPedalPosition.class, mAcceleratorPedalPositionListener);
            mVehicleManager.addListener(FuelConsumed.class, mFuelConsumedListener);
            mVehicleManager.addListener(Odometer.class, mOdometerListener);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

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

        } else if (id == R.id.nav_rewards) {
            Intent i = new Intent(getApplicationContext(),RewardsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_society) {
            Intent i = new Intent(getApplicationContext(),SocietyActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_history) {
            Intent i = new Intent(getApplicationContext(),ProfileActivity.class);
            startActivity(i);
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