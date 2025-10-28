package com.example.winzgo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.winzgo.fragments.DashboardFragment;
import com.example.winzgo.fragments.HomeFragment;
import com.example.winzgo.fragments.MoneyFragment;
import com.example.winzgo.R;
import com.example.winzgo.fragments.SettingsFragment;
import com.example.winzgo.models.UserDocumentModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bNView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String TAG, mPhone;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bNView = findViewById(R.id.bottomNav);
        bNView.setSelectedItemId(R.id.homeItem);
        TAG = "MainActivity.java";
        mPhone = getIntent().getStringExtra("phone");
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setTitle("Fetching Data");
        dialog.setMessage("Please wait...");
        getSupportFragmentManager().beginTransaction().add(R.id.container, new DashboardFragment()).commit();

        bNView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //item id that was selected or clicked
                int id = item.getItemId();
                if (id == R.id.homeItem) {
                    loadFragment(new HomeFragment());
                } else if (id == R.id.moneyItem) {
                    loadFragment(new MoneyFragment());
                } else if (id == R.id.settingsItem) {
                    loadFragment(new SettingsFragment());
                }
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    public void loadFragment(Fragment fragment, boolean isAddToBackstack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        if(isAddToBackstack)
            ft.addToBackStack(null);

        ft.commit();
    }

    public static int i = 0;

    //onBackPressed() method invoked everytime when user click on system navigation pattern back button.
    @Override
    public void onBackPressed() {
        if (i != 101) {
            i++;
            if (i == 1) {
                Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            } else {
                i = 0;
                //It clear current backstack of fragment( which is in resumed state ).
                getSupportFragmentManager().popBackStack("", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                super.onBackPressed();
            }
        } else {
        }
    }

    public void popCurrent() {
        if (!isFinishing())
            getSupportFragmentManager().popBackStackImmediate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("MainActivity.java", "paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("MainActivity.java", "stopped");
    }
}