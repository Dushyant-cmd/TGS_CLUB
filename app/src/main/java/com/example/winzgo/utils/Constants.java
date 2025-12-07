package com.example.winzgo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.winzgo.databinding.ProfileEditDialogBinding;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String RED = "red";
    public static final String GREEN = "green";
    public static final String BLUE = "blue";
    // sp key start
    public static final String WHATSAPP_GROUP = "whatsappGroup";
    public static final String WINZGO_AGENT_LINK = "winzgoAgentLink";
    public static final String TELEGRAM_LINK = "telegramLink";
    public static final String VIP_BETS_SUB = "vipBetSubs";
    public static final String APP_DOWNLOAD_LINK = "appDownloadLink";
    public static final String NOTIFICATION_PERMISSION = "notificationPermission";
    public static final String IS_INR = "isInr";
    public static final String USER_ID_KEY = "userId";
    public static final String NAME_KEY = "name";
    public static final String DOLLAR_CURRENCY = "dollarCurrency";
    // WalletType 0(win go), 1(coin), 2(trade x)
    public static final String WIN_GO_BALANCE_KEY = "balance";
    public static final String COIN_BALANCE_KEY = "coinBalance";
    public static final String TRADE_PRO_BALANCE_KEY = "tradeProBalance";
    public static final String DARK_MODE_KEY = "darkMode";
    // sp key end
    public static final String RUPEE_ICON = "\u20b9";
    public static final String USD_ICON = "$";


    // Interface start

    public interface ProfileEditListener {
        void submit(String name);
    }
    // Interface end

    public static Dialog showDialog(Context context, String message) {
        ProgressDialog dialog1 = new ProgressDialog(context);
        dialog1.setMessage(message);
        dialog1.show();
        return dialog1;
    }

    public static AlertDialog showAlerDialog(Context context, String message, String buttonName, UtilsInterfaces.Refresh listener) {
        AlertDialog dialog = new AlertDialog.Builder(context).setMessage(message).setTitle("TGS club")
                .setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.refresh();
                    }
                }).create();

        dialog.show();
        return dialog;
    }

    public static Dialog showProgressDialog(Context context) {
        ProgressDialog dialog1 = new ProgressDialog(context);
        dialog1.setMessage("Please wait...");
        dialog1.show();
        return dialog1;
    }

    public static Dialog showEditDialog(Activity activity, ProfileEditListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        ProfileEditDialogBinding binding = ProfileEditDialogBinding.inflate(activity.getLayoutInflater(), (ViewGroup) activity.getWindow().getDecorView().getRootView(), false);
        builder.setView(binding.getRoot());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        binding.ivClose.setOnClickListener(v -> {
            dialog.dismiss();
        });

        binding.confirmBtn.setOnClickListener(v -> {
            String name = binding.etName.getText().toString();
            if (!name.isEmpty()) {
                listener.submit(name);
                dialog.dismiss();
            } else {
                binding.etName.setError("Required");
            }
        });

        dialog.show();

        return dialog;
    }


    public static void showLogoutAlertDialog(Context context, UtilsInterfaces.Refresh listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Log-out?");//it is title in dialog
        builder.setMessage("Are you sure you want to Log-out?");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> listener.refresh());
        builder.setNegativeButton("No", (dialog, i) -> {
        });

        Dialog alertDialog = builder.create();
        alertDialog.show();
    }

    //check mobile device is connected to network or not.
    public static boolean isNetworkConnected(Activity activity) {
        if (activity != null) {
            if (!activity.isFinishing()) {
                ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

                return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
            }
        }

        return true;
    }

    public static void showSnackBarAction(View root, String message, String actionName, UtilsInterfaces.Refresh listener) {
        Snackbar bar = Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE);
        bar.setAction(actionName, view -> {
            bar.dismiss();
            listener.refresh();
        });

        bar.show();
    }

    public static void showSnackBar(View root, String message) {
        Snackbar bar = Snackbar.make(root, message, Snackbar.LENGTH_SHORT);
        bar.show();
    }

    public static void updateBalance(Activity activity, long amount, boolean isAdd, String walletType, UtilsInterfaces.Refresh listener) {
        try {
            if (isNetworkConnected(activity)) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                new Handler().post(() -> {
                    long currentBal = SessionSharedPref.getLong(activity, walletType, 0L);
                    long userId = SessionSharedPref.getLong(activity, Constants.USER_ID_KEY, 0L);
                    long amt = 0;
                    if (isAdd) {
                        amt = (currentBal + amount);
                    } else {
                        amt = (currentBal - amount);
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put(walletType, amt);

                    long finalAmt = amt;
                    firestore.collection("users").document(String.valueOf(userId))
                            .update(map).addOnSuccessListener(unused -> {
                                SessionSharedPref.setLong(activity, Constants.TRADE_PRO_BALANCE_KEY, finalAmt);
                                listener.refresh();
                            }).addOnFailureListener(e -> {
                            });
                });
            } else {
                showSnackBar(activity.getWindow().getDecorView().getRootView(), "No internet");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public static void showEditTextError(EditText et, String message) {
        et.setError(message);
        et.requestFocus();
    }

    /**
     * This method will set dark mode of theme
     */
    public static void setDarkMode(Context context, boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        SessionSharedPref.setBoolean(context, Constants.DARK_MODE_KEY, isDarkMode);
    }

    public static double changeBalanceToDiffCurrency(Context context, String balance, boolean isInrOrUsd) {
        long dollarCurrentValue = SessionSharedPref.getLong(context, Constants.DOLLAR_CURRENCY, 87L);
        boolean savedInrOrUsd = SessionSharedPref.getBoolean(context, Constants.IS_INR, false);

        DecimalFormat decimalFormat = new DecimalFormat("####0.00");
        String txt = decimalFormat.format((Double.parseDouble(balance)) / dollarCurrentValue);

        if (isInrOrUsd) {
            // inr
            double inrBal = Double.parseDouble(balance);
            if (!savedInrOrUsd) {
                inrBal = inrBal * dollarCurrentValue; // if saved currency is usd
            }
            return inrBal;
        } else {
            // usd
            return Double.parseDouble(txt);
        }
    }

    public static String checkAndReturnInSetCurrency(Context context, String amountStr) {
        try {
            DecimalFormat decimalFormat = new DecimalFormat("####0.00");
            long dollarCurrentValue = SessionSharedPref.getLong(context, Constants.DOLLAR_CURRENCY, 87L);
            boolean isInr = SessionSharedPref.getBoolean(context, Constants.IS_INR, false);
            double amount = Double.parseDouble(amountStr);

            String txt = Constants.RUPEE_ICON + ((long) amount); // inr
            if (!isInr) {
                // usd and not already converted to usd
                if (amountStr.contains("."))
                    txt = Constants.USD_ICON + amountStr;
                else
                    txt = Constants.USD_ICON + decimalFormat.format(amount / dollarCurrentValue);
            }

            return txt;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Constants.RUPEE_ICON + 0;
    }

    public static String getFullNameCoins(String coin) {
        if (coin.equalsIgnoreCase("btc"))
            return "Bitcoin";
        else if (coin.equalsIgnoreCase("eth"))
            return "Ethereum";
        else if (coin.equalsIgnoreCase("sol"))
            return "Solana";
        return coin;
    }
}
