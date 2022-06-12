package wrteam.multivendor.shop.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.fragment.PaymentFragment;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.PaymentModelClass;
import wrteam.multivendor.shop.helper.Session;

@SuppressWarnings("ALL")
public class PayPalWebActivity extends AppCompatActivity {
    Toolbar toolbar;
    WebView webView;
    String url;
    PaymentModelClass paymentModelClass;
    boolean isTxnInProcess = true;
    String orderId;
    Session session;
    Map<String, String> sendParams;
    String from;

    CardView cardViewHamburger;
    TextView toolbarTitle;
    ImageView imageMenu;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        sendParams = (Map<String, String>) getIntent().getSerializableExtra(Constant.PARAMS);
        orderId = getIntent().getStringExtra(Constant.ORDER_ID);
        from = getIntent().getStringExtra(Constant.FROM);
        session = new Session(PayPalWebActivity.this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cardViewHamburger = findViewById(R.id.cardViewHamburger);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu = findViewById(R.id.imageMenu);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        toolbarTitle.setText(getString(R.string.paypal));

        imageMenu.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        cardViewHamburger.setOnClickListener(view -> onBackPressed());
        webView = findViewById(R.id.webView);

        paymentModelClass = new PaymentModelClass(PayPalWebActivity.this);

        url = getIntent().getStringExtra("url");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith(Constant.MainBaseUrl)) {
                    GetTransactionResponse(url);
                    return true;
                } else
                    isTxnInProcess = true;
                return false;
            }

        });
        webView.loadUrl(url);
    }

    public void GetTransactionResponse(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    isTxnInProcess = false;
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        AddTransaction(PayPalWebActivity.this, orderId, getString(R.string.paypal), orderId, status, "", sendParams);
                    } catch (JSONException e) {
                            e.printStackTrace();

                    }
                },
                error -> {
                    try {
                        PaymentFragment.dialog_.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        ApiConfig.getInstance().getRequestQueue().getCache().clear();
        ApiConfig.getInstance().addToRequestQueue(stringRequest);

    }

    public void AddTransaction(Activity activity, String orderId, String paymentType, String txnid, final String status, String message, Map<String, String> sendParams) {
        Map<String, String> transparams = new HashMap<>();
        transparams.put(Constant.ADD_TRANSACTION, Constant.GetVal);
        transparams.put(Constant.USER_ID, sendParams.get(Constant.USER_ID));
        transparams.put(Constant.ORDER_ID, orderId);
        transparams.put(Constant.TYPE, paymentType);
        transparams.put(Constant.TRANS_ID, txnid);
        transparams.put(Constant.AMOUNT, sendParams.get(Constant.FINAL_TOTAL));
        transparams.put(Constant.STATUS, status);
        transparams.put(Constant.MESSAGE, message);
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        transparams.put("transaction_date", df.format(c));
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {

                        if (from.equals(Constant.WALLET)) {
                            onBackPressed();
                            ApiConfig.getWalletBalance(activity, session);
                            Toast.makeText(activity, "Amount will be credited in wallet very soon.", Toast.LENGTH_LONG).show();
                        } else if (from.equals(Constant.PAYMENT)) {
                            if (status.equals(Constant.SUCCESS) || status.equals(Constant.AWAITING_PAYMENT)) {
                                finish();
                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(Constant.FROM, "payment_success");
                                activity.startActivity(intent);
                            } else {
                                finish();
                            }
                        }
                    }
                } catch (JSONException e) {
                        e.printStackTrace();

                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, transparams, true);
    }


    @Override
    public void onBackPressed() {
        if (isTxnInProcess) {
            ProcessAlertDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void ProcessAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(PayPalWebActivity.this);
        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.txn_cancel_msg));

        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();
        alertDialog.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            DeleteTransaction(PayPalWebActivity.this, getIntent().getStringExtra(Constant.ORDER_ID));
            alertDialog1.dismiss();
        }).setNegativeButton(getString(R.string.no), (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();
    }

    public void DeleteTransaction(Activity activity, String orderId) {
        Map<String, String> transparams = new HashMap<>();
        transparams.put(Constant.DELETE_ORDER, Constant.GetVal);
        transparams.put(Constant.ORDER_ID, orderId);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    PaymentFragment.dialog_.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PayPalWebActivity.super.onBackPressed();
            }
        }, activity, Constant.ORDER_PROCESS_URL, transparams, false);
    }
}
