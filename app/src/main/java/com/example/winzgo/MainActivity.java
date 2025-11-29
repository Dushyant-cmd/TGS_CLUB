package com.example.winzgo;

import static com.example.winzgo.utils.Constants.setDarkMode;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.winzgo.databinding.ActivityMainBinding;
import com.example.winzgo.fragments.CoinAndTradeWalletFragment;
import com.example.winzgo.fragments.DashboardFragment;
import com.example.winzgo.fragments.settings.SettingsCoinAndTradeXFragment;
import com.example.winzgo.fragments.settings.SettingsFragment;
import com.example.winzgo.fragments.wingo.HomeFragment;
import com.example.winzgo.fragments.wingo.MoneyFragment;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    public static ActivityMainBinding binding;
    private String oldTitle = "";

    public int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadFragment(new DashboardFragment(), false, "Home");
        setListeners();
    }

    private void setListeners() {
        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //item id that was selected or clicked
                int id = item.getItemId();
                if (id == R.id.homeItem) {
                    loadFragment(new HomeFragment(), true, "Win-Go");
                } else if (id == R.id.moneyItem) {
                    loadFragment(new MoneyFragment(), true, "Money");
                } else if (id == R.id.settingsItem) {
                    loadFragment(new SettingsFragment(), true, "Settings");
                }
                return true;
            }
        });

        binding.cardWallet.setOnClickListener(v -> {
            Fragment currFragment = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
            int type = 0;// 0 trade, 1 coin, 2 win-go
            if (currFragment.toString().contains("CoinPredictionFragment")) {
                type = 1;
            }
            Bundle bundle = new Bundle();
            bundle.putInt("type", type);
            CoinAndTradeWalletFragment fragment = new CoinAndTradeWalletFragment();
            fragment.setArguments(bundle);
            loadFragment(fragment, true, "Wallet");
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

        binding.btnDarkMode.setOnClickListener(v -> {
            boolean isDarkMode = SessionSharedPref.getBoolean(this, Constants.DARK_MODE_KEY, false);
            setDarkMode(this, !isDarkMode);
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        });
    }

    public void loadFragment(Fragment fragment, boolean isAddToBackstack, String title) {
        setupHeader(title);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        if (isAddToBackstack)
            ft.addToBackStack(null);

        ft.commit();
    }

    public void setupHeader(String title) {
        oldTitle = binding.tvTitle.getText().toString();
        binding.tvTitle.setText(title);
        MainActivity.binding.mainHeaderLy.setVisibility(View.VISIBLE);

        if (title.equalsIgnoreCase("home")) {
            binding.cardWallet.setVisibility(View.GONE);
            binding.btnHome.setVisibility(View.GONE);
            binding.btnDarkMode.setVisibility(View.VISIBLE);
            binding.btnSettings.setVisibility(View.VISIBLE);
        } else if (title.equalsIgnoreCase("crypto streak") || title.equalsIgnoreCase("trade pro")) {
            binding.cardWallet.setVisibility(View.VISIBLE);
            binding.btnHome.setVisibility(View.GONE);
            binding.btnDarkMode.setVisibility(View.GONE);
            binding.btnSettings.setVisibility(View.GONE);
        } else if (title.equalsIgnoreCase("refer")) {
            MainActivity.binding.mainHeaderLy.setVisibility(View.GONE);
        } else {
            binding.cardWallet.setVisibility(View.GONE);
            binding.btnHome.setVisibility(View.VISIBLE);
            binding.btnDarkMode.setVisibility(View.GONE);
            binding.btnSettings.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currFragment = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);

        if (currFragment.toString().contains("DashboardFragment")) {
            if (i == 1) {
                getSupportFragmentManager().popBackStack("", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                finish();
            } else {
                Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> {
                    i = 0;
                }, 2000);
            }

            i++;
        } else if (currFragment.toString().contains("HomeFragment")) {
            loadFragment(new DashboardFragment(), false, "Home");
        } else super.onBackPressed();
    }

    public void popCurrent() {
        if (!isFinishing())
            getSupportFragmentManager().popBackStackImmediate();
    }
}