package de.igelstudios.substitution;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import de.igelstudios.substitution.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //private static final Map<Integer,String> permissionIds = Map.of(0,Manifest.permission.POST_NOTIFICATIONS,1,Manifest.permission.ACCESS_NETWORK_STATE);

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private static MainActivity instance;
    public static Color textColor;
    public Table SUBSTITUTION_TABLE = null;
    public Fetcher FETCHER = null;
    public Notifier NOTIFIER = null;
    public RequestedCourses COURSES = null;
    public boolean second = false;
    public boolean settings = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.POST_NOTIFICATIONS},0);
        }

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            textColor = Color.valueOf(0xFFFFFFFF);
        } else {
            textColor = Color.valueOf(0);
        }

        if(SUBSTITUTION_TABLE == null){
            SUBSTITUTION_TABLE = new Table(this.getApplicationContext());
            FETCHER = new Fetcher(this.getApplicationContext(),"substitution");
            NOTIFIER = new Notifier(this.getApplicationContext());
            COURSES = new RequestedCourses(this.getApplicationContext(),"requested_courses");
            new Config(this.getApplicationContext());
        }

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        /*getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();*/

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(settings)navController.navigate(R.id.action_settingsFragment_to_fullTableFragment);
                else if(second) navController.navigate(R.id.action_SecondFragment_to_fullTableFragment);
                else*/ navController.navigate(R.id.action_FirstFragment_to_fullTableFragment);
            }
        });

        Scheduler.schedule(this.getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Intent intent = new Intent(MainActivity.this.getApplicationContext(), SettingsActivity.class);
            //startActivity(intent);
            /*FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.action_FirstFragment_to_settingsFragment, new SettingsFragment());
            transaction.addToBackStack(null);
            transaction.commit();*/
            if(settings) Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.action_fullTableFragment_to_settingsFragment);
            else if(second) Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.action_SecondFragment_to_settingsFragment);
            else Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.action_FirstFragment_to_settingsFragment);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != 0) super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED)System.exit(1);
    }

    public static void requestPermissions(){
        if (ActivityCompat.checkSelfPermission(instance.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                Build.VERSION.SDK_INT >= 32) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.POST_NOTIFICATIONS},0);
        }
        if (ActivityCompat.checkSelfPermission(instance.getApplicationContext(), android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.ACCESS_NETWORK_STATE},0);
        }

        if (ActivityCompat.checkSelfPermission(instance.getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.INTERNET},0);
        }
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public ActivityMainBinding getBinding() {
        return binding;
    }
}