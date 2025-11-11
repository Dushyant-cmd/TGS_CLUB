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
import android.view.View;
import android.widget.Toast;

import com.example.winzgo.databinding.ActivityMainBinding;
import com.example.winzgo.fragments.CoinAndTradeWalletFragment;
import com.example.winzgo.fragments.DashboardFragment;
import com.example.winzgo.fragments.settings.SettingsCoinAndTradeXFragment;
import com.example.winzgo.fragments.wingo.HomeFragment;
import com.example.winzgo.fragments.wingo.MoneyFragment;
import com.example.winzgo.fragments.settings.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNav.setSelectedItemId(R.id.homeItem);
        getSupportFragmentManager().beginTransaction().add(R.id.container, new DashboardFragment()).commit();

        setListeners();
    }

    private void setListeners() {
        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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

        binding.cardWallet.setOnClickListener(v -> {
            loadFragment(new CoinAndTradeWalletFragment(), true, "Wallet");
        });

        binding.btnSettings.setOnClickListener(v -> {
            loadFragment(new SettingsCoinAndTradeXFragment(), true, "Settings");
        });

        binding.btnHome.setOnClickListener(v -> {
            loadFragment(new DashboardFragment(), true, "Home");
        });

        binding.btnBack.setOnClickListener(v -> {
            super.onBackPressed();
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    public void loadFragment(Fragment fragment, boolean isAddToBackstack, String title) {
        binding.tvTitle.setText(title);
        if(title.equalsIgnoreCase("home")) {
            binding.cardWallet.setVisibility(View.GONE);
            binding.btnHome.setVisibility(View.GONE);
            binding.btnDarkMode.setVisibility(View.VISIBLE);
            binding.btnSettings.setVisibility(View.VISIBLE);
        } else if(title.equalsIgnoreCase("coin prediction") || title.equalsIgnoreCase("crypto streak")) {
            binding.cardWallet.setVisibility(View.VISIBLE);
            binding.btnHome.setVisibility(View.GONE);
            binding.btnDarkMode.setVisibility(View.GONE);
            binding.btnSettings.setVisibility(View.GONE);
        } else {
            binding.cardWallet.setVisibility(View.GONE);
            binding.btnHome.setVisibility(View.VISIBLE);
            binding.btnDarkMode.setVisibility(View.GONE);
            binding.btnSettings.setVisibility(View.GONE);
        }

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