package org.secuso.privacyfriendlytraining.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.secuso.privacyfriendlytraining.R;
import org.secuso.privacyfriendlytraining.services.TimerService;

import java.util.ArrayList;


public class MainActivity extends BaseActivity {

    // General
    private SharedPreferences settings = null;
    Intent intent = null;

    // Timer values
    private int blockPeriodizationSets = 1;
    private long blockPeriodizationTime = 150; // 2:30 min
    private long blockPeriodizationTimeMax = 300; // 5:00 min
    private boolean isBlockPeriodization = false;
    private final long startTime = 10;
    private long workoutTime = 0;
    private long restTime = 0;
    private int sets = 0;

    // GUI text
    private TextView workoutIntervalText = null;
    private TextView restIntervalText = null;
    private TextView setsText = null;

    //Service variables
    private TimerService timerService = null;
    private boolean serviceBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = PreferenceManager.getDefaultSharedPreferences(this);


        //Default values for  the workout configuration
        workoutTime = 10;
        restTime = 20;
        sets = 5;

        this.workoutIntervalText = (TextView) this.findViewById(R.id.main_workout_interval_time);
        this.restIntervalText = (TextView) this.findViewById(R.id.main_rest_interval_time);
        this.setsText = (TextView) this.findViewById(R.id.main_sets_amount);

        this.workoutIntervalText.setText(formatTime(workoutTime));
        this.restIntervalText.setText(formatTime(restTime));
        this.setsText.setText(Integer.toString(sets));


//        PFASQLiteHelper database = new PFASQLiteHelper(getBaseContext());
//        database.addSampleData(new PFASampleDataType(0, "eins.de", "hugo1", 11));
//        database.addSampleData(new PFASampleDataType(0, "zwei.de", "hugo2", 12));
//        database.addSampleData(new PFASampleDataType(0, "drei.de", "hugo3", 13));
//        database.addSampleData(new PFASampleDataType(0, "vier.de", "hugo4", 14));

//        DatabaseExporter porter = new DatabaseExporter(getBaseContext().getDatabasePath(PFASQLiteHelper.DATABASE_NAME).toString(), "PF_EXAMPLE_DB");
//
//        try {
//            porter.dbToJSON();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }



        // Use the a button to display the welcome screen
//        Button b = (Button) findViewById(R.id.button_welcomedialog);
//        if(b != null) {
//            b.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    WelcomeDialog welcomeDialog = new WelcomeDialog();
//                    welcomeDialog.show(getFragmentManager(), "WelcomeDialog");
//                    PrefManager prefManager = new PrefManager(getBaseContext());
//                    prefManager.setFirstTimeLaunch(true);
//                    Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
//            });
//        }
//
       overridePendingTransition(0, 0);
       startService(new Intent(this, TimerService.class));
    }



    /**
     * This method connects the Activity to the menu item
     * @return ID of the menu item it belongs to
     */
    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_example;
    }


//    public static class WelcomeDialog extends DialogFragment {
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//        }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//            LayoutInflater i = getActivity().getLayoutInflater();
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setView(i.inflate(R.layout.welcome_dialog, null));
//            builder.setIcon(R.mipmap.icon);
//            builder.setTitle(getActivity().getString(R.string.welcome));
//            builder.setPositiveButton(getActivity().getString(R.string.okay), null);
//            builder.setNegativeButton(getActivity().getString(R.string.viewhelp), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    ((MainActivity)getActivity()).goToNavigationItem(R.id.nav_help);
//                }
//            });
//
//            return builder.create();
//        }
//    }


    //Intervals
    //http://www.dtb-online.de/portal/verband/service-fuer-mitglieder/ratgeber-gesundheit/funktionelles-zirkeltraining.html
    //http://www.sportunterricht.de/lksport/circuitkraft.html
    //Added additional 15 seconds to rest and 30 seconds to exercise for user convenience
    //Usual maximum of sets is 12 but I added additional 4 just in case
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.main_workout_interval_minus:
                this.workoutTime = (workoutTime <= 10) ? 90 : this.workoutTime - 10;
                this.workoutIntervalText.setText(formatTime(workoutTime));
                break;
            case R.id.main_workout_interval_plus:
                this.workoutTime = (workoutTime >= 90) ? 10 : this.workoutTime + 10;
                this.workoutIntervalText.setText(formatTime(workoutTime));
                break;
            case R.id.main_rest_interval_minus:
                this.restTime = (restTime <= 0) ? 60 : this.restTime - 10;
                this.restIntervalText.setText(formatTime(restTime));
                break;
            case R.id.main_rest_interval_plus:
                this.restTime = (restTime >= 60) ? 0 : this.restTime + 10;
                this.restIntervalText.setText(formatTime(restTime));
                break;
            case R.id.main_sets_minus:
                this.sets = (sets <= 1) ? 16 : this.sets - 1;
                this.setsText.setText(Integer.toString(sets));
                break;
            case R.id.main_sets_plus:
                this.sets = (sets >= 16) ? 1 : this.sets + 1;
                this.setsText.setText(Integer.toString(sets));
                break;
            case R.id.main_block_periodization:
                AlertDialog finishedAlert = buildBlockAlert();
                finishedAlert.show();
                break;
            case R.id.start_workout:
                intent = new Intent(this, WorkoutActivity.class);

                if (settings != null && settings.getBoolean("pref_start_timer_switch", true)) {
                    timerService.startWorkout(workoutTime, restTime, startTime, sets,
                            isBlockPeriodization, blockPeriodizationTime, blockPeriodizationSets);
                }
                else {
                    timerService.startWorkout(workoutTime, restTime, 0, sets,
                            isBlockPeriodization, blockPeriodizationTime, blockPeriodizationSets);
                }

                this.startActivity(intent);
                break;
            default:
        }
    }

    /** Defines callbacks for service binding, passed to bindService()*/
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };



    /*Build an AlertDialog for the block periodization*/
    private AlertDialog buildBlockAlert(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.block_periodization, null);

        Button timeButtonPlus = (Button)dialogLayout.findViewById(R.id.main_block_periodization_time_plus);
        Button timeButtonMinus = (Button)dialogLayout.findViewById(R.id.main_block_periodization_time_minus);
        Button setButtonPlus = (Button)dialogLayout.findViewById(R.id.main_block_periodization_sets_plus);
        Button setButtonMinus = (Button)dialogLayout.findViewById(R.id.main_block_periodization_sets_minus);

        final TextView setsText = (TextView)dialogLayout.findViewById(R.id.main_block_periodization_sets_amount);
        final TextView timeText = (TextView)dialogLayout.findViewById(R.id.main_block_periodization_time);

        setsText.setText(Integer.toString(blockPeriodizationSets));
        timeText.setText(formatTime(blockPeriodizationTime));

        setButtonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(blockPeriodizationSets <= sets){ blockPeriodizationSets += 1; }
                setsText.setText(Integer.toString(blockPeriodizationSets));
            }
        });

        setButtonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(blockPeriodizationSets > 1){ blockPeriodizationSets -= 1; }
                setsText.setText(Integer.toString(blockPeriodizationSets));
            }
        });


        timeButtonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(blockPeriodizationTime < blockPeriodizationTimeMax){ blockPeriodizationTime += 10; }
                timeText.setText(formatTime(blockPeriodizationTime));
            }
        });

        timeButtonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(blockPeriodizationTime > 10){ blockPeriodizationTime -= 10; }
                timeText.setText(formatTime(blockPeriodizationTime));
            }
        });


        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setView(dialogLayout);

        final CharSequence[] item = { getResources().getString(R.string.main_block_periodization) };
        final boolean[] selection = { isBlockPeriodization };
        final ArrayList selectedItem = new ArrayList();

        alertBuilder.setTitle(getResources().getString(R.string.main_block_periodization_headline))
                .setMultiChoiceItems(item, selection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        isBlockPeriodization = isChecked;
                        if (isChecked) {
                            selectedItem.add(indexSelected);
                        } else if (selectedItem.contains(indexSelected)) {
                            selectedItem.remove(Integer.valueOf(indexSelected));
                        }
                    }
                }).setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                });

        return alertBuilder.create();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    public void onDestroy() {
        timerService.startNotifying(false);
        stopService(new Intent(this, TimerService.class));
        super.onDestroy();
    }

    //Helper methods
    private String formatTime(long seconds){
        long min = seconds/60;
        long sec = seconds%60;

        String time = String.format("%02d : %02d", min,sec);

        return time;
    }
}