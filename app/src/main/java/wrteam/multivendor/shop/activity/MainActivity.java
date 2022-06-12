package wrteam.multivendor.shop.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import me.ibrahimsn.lib.SmoothBottomBar;
import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.fragment.AddressListFragment;
import wrteam.multivendor.shop.fragment.CartFragment;
import wrteam.multivendor.shop.fragment.CategoryFragment;
import wrteam.multivendor.shop.fragment.DrawerFragment;
import wrteam.multivendor.shop.fragment.FavoriteFragment;
import wrteam.multivendor.shop.fragment.HomeFragment;
import wrteam.multivendor.shop.fragment.OrderPlacedFragment;
import wrteam.multivendor.shop.fragment.PaymentFragment;
import wrteam.multivendor.shop.fragment.PinCodeFragment;
import wrteam.multivendor.shop.fragment.ProductDetailFragment;
import wrteam.multivendor.shop.fragment.SearchFragment;
import wrteam.multivendor.shop.fragment.SubCategoryFragment;
import wrteam.multivendor.shop.fragment.TrackOrderFragment;
import wrteam.multivendor.shop.fragment.TrackerDetailFragment;
import wrteam.multivendor.shop.fragment.WalletTransactionFragment;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.DatabaseHelper;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.OrderTracker;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PaymentResultListener {

    static final String TAG = "MAIN ACTIVITY";
    @SuppressLint("StaticFieldLeak")
    public static Toolbar toolbar;
    public static SmoothBottomBar bottomNavigationView;
    public static Fragment active;
    public static FragmentManager fm = null;
    public static Fragment homeFragment, categoryFragment, favoriteFragment, drawerFragment;
    public static boolean homeClicked = false, categoryClicked = false, favoriteClicked = false, drawerClicked = false;
    public Activity activity;
    public Session session;
    boolean doubleBackToExitPressedOnce = false;
    DatabaseHelper databaseHelper;
    String from;
    CardView cardViewHamburger;
    TextView toolbarTitle;
    public static PinCodeFragment pinCodeFragment;
    ImageView imageMenu, imageHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        cardViewHamburger = findViewById(R.id.cardViewHamburger);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu = findViewById(R.id.imageMenu);
        imageHome = findViewById(R.id.imageHome);

        activity = MainActivity.this;
        session = new Session(activity);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        from = getIntent().getStringExtra(Constant.FROM);
        databaseHelper = new DatabaseHelper(activity);

        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            ApiConfig.getCartItemCount(activity, session);
        } else {
            try {
                databaseHelper.getTotalItemOfCart(activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setAppLocal("en"); //Change you language code here

        fm = getSupportFragmentManager();

        homeFragment = new HomeFragment();
        categoryFragment = new CategoryFragment();
        favoriteFragment = new FavoriteFragment();
        drawerFragment = new DrawerFragment();


        Bundle bundle = new Bundle();
        bottomNavigationView.setItemActiveIndex(0);
        active = homeFragment;
        homeClicked = true;
        drawerClicked = false;
        favoriteClicked = false;
        categoryClicked = false;
        try {
            if (!getIntent().getStringExtra("json").isEmpty()) {
                bundle.putString("json", getIntent().getStringExtra("json"));
            }
            homeFragment.setArguments(bundle);
            fm.beginTransaction().add(R.id.container, homeFragment).commit();
        } catch (Exception e) {
            fm.beginTransaction().add(R.id.container, homeFragment).commit();
        }

        bottomNavigationView.setOnItemSelectedListener(i -> {

            switch (i) {
                case 0:
                    if (!homeClicked) {
                        fm.beginTransaction().add(R.id.container, homeFragment).show(homeFragment).hide(active).commit();
                        homeClicked = true;
                    } else {
                        fm.beginTransaction().show(homeFragment).hide(active).commit();
                    }
                    active = homeFragment;
                    break;
                case 1:
                    if (!categoryClicked) {
                        fm.beginTransaction().add(R.id.container, categoryFragment).show(categoryFragment).hide(active).commit();
                        categoryClicked = true;
                    } else {
                        fm.beginTransaction().show(categoryFragment).hide(active).commit();
                    }
                    active = categoryFragment;
                    break;
                case 2:
                    if (!favoriteClicked) {
                        fm.beginTransaction().add(R.id.container, favoriteFragment).show(favoriteFragment).hide(active).commit();
                        favoriteClicked = true;
                    } else {
                        fm.beginTransaction().show(favoriteFragment).hide(active).commit();
                    }
                    active = favoriteFragment;
                    break;

                case 3:
                    if (!drawerClicked) {
                        fm.beginTransaction().add(R.id.container, drawerFragment).show(drawerFragment).hide(active).commit();
                        drawerClicked = true;
                    } else {
                        fm.beginTransaction().show(drawerFragment).hide(active).commit();
                    }
                    active = drawerFragment;
                    break;
            }
            return false;
        });

        switch (from) {
            case "checkout":
                bottomNavigationView.setVisibility(View.GONE);
                ApiConfig.getCartItemCount(activity, session);
                Fragment fragment = new AddressListFragment();
                bundle = new Bundle();
                bundle.putString(Constant.FROM, "process");
                bundle.putDouble("total", Double.parseDouble(ApiConfig.StringFormat("" + Constant.FLOAT_TOTAL_AMOUNT)));
                fragment.setArguments(bundle);
                fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                break;
            case "share":
                Fragment fragment0 = new ProductDetailFragment();
                Bundle bundle0 = new Bundle();
                bundle0.putInt("variantPosition", getIntent().getIntExtra("variantPosition", 0));
                bundle0.putString("id", getIntent().getStringExtra("id"));
                bundle0.putString(Constant.FROM, "share");
                fragment0.setArguments(bundle0);
                fm.beginTransaction().add(R.id.container, fragment0).addToBackStack(null).commit();
                break;
            case "product":
                Fragment fragment1 = new ProductDetailFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putInt("variantPosition", getIntent().getIntExtra("variantPosition", 0));
                bundle1.putString("id", getIntent().getStringExtra("id"));
                bundle1.putString(Constant.FROM, "product");
                fragment1.setArguments(bundle1);
                fm.beginTransaction().add(R.id.container, fragment1).addToBackStack(null).commit();
                break;
            case "category":
                Fragment fragment2 = new SubCategoryFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString("id", getIntent().getStringExtra("id"));
                bundle2.putString("name", getIntent().getStringExtra("name"));
                bundle2.putString(Constant.FROM, "category");
                fragment2.setArguments(bundle2);
                fm.beginTransaction().add(R.id.container, fragment2).addToBackStack(null).commit();
                break;
            case "order":
                Fragment fragment3 = new TrackerDetailFragment();
                Bundle bundle3 = new Bundle();
                bundle3.putSerializable("model", "");
                bundle3.putString("id", getIntent().getStringExtra("id"));
                fragment3.setArguments(bundle3);
                fm.beginTransaction().add(R.id.container, fragment3).addToBackStack(null).commit();
                break;
            case "payment_success":
                fm.beginTransaction().add(R.id.container, new OrderPlacedFragment()).addToBackStack(null).commit();
                break;
            case "tracker":
                fm.beginTransaction().add(R.id.container, new TrackOrderFragment()).addToBackStack(null).commit();
                break;
            case "wallet":
                fm.beginTransaction().add(R.id.container, new WalletTransactionFragment()).addToBackStack(null).commit();
                break;
        }

        fm.addOnBackStackChangedListener(() -> {
            toolbar.setVisibility(View.VISIBLE);
            Fragment currentFragment = fm.findFragmentById(R.id.container);
            Objects.requireNonNull(currentFragment).onResume();
        });

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            session.setData(Constant.FCM_ID, token);
            Register_FCM(token);
        });

        GetProductsName();
    }

    public void setAppLocal(String languageCode) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(languageCode.toLowerCase()));
        resources.updateConfiguration(configuration, dm);
    }

    public void Register_FCM(String token) {
        Map<String, String> params = new HashMap<>();
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params.put(Constant.USER_ID, session.getData(Constant.ID));
        }
        params.put(Constant.FCM_ID, token);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.FCM_ID, token);
                    }
                } catch (JSONException ignored) {

                }

            }
        }, activity, Constant.REGISTER_DEVICE_URL, params, false);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        if (fm.getBackStackEntryCount() == 0) {
            Toast.makeText(this, getString(R.string.exit_msg), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_cart:
                MainActivity.fm.beginTransaction().add(R.id.container, new CartFragment()).addToBackStack(null).commit();
                break;
            case R.id.toolbar_search:
                MainActivity.fm.beginTransaction().add(R.id.container, new SearchFragment()).addToBackStack(null).commit();
                break;
            case R.id.toolbar_logout:
                session.logoutUserConfirmation(activity);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_search).setVisible(true);
        menu.findItem(R.id.toolbar_cart).setIcon(ApiConfig.buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity));

        if (fm.getBackStackEntryCount() > 0) {
            toolbarTitle.setText(Constant.TOOLBAR_TITLE);
            bottomNavigationView.setVisibility(View.GONE);

            cardViewHamburger.setCardBackgroundColor(getColor(R.color.colorPrimaryLight));
            imageMenu.setOnClickListener(v -> fm.popBackStack());

            imageMenu.setVisibility(View.VISIBLE);
            imageHome.setVisibility(View.GONE);
        } else {
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                toolbarTitle.setText(getString(R.string.hi) + session.getData(Constant.NAME) + "!");
            } else {
                toolbarTitle.setText(getString(R.string.hi_user));
            }
            bottomNavigationView.setVisibility(View.VISIBLE);
//            cardViewHamburger.setCardBackgroundColor(getColor(R.color.transparent));
            cardViewHamburger.setCardBackgroundColor(getColor(R.color.colorPrimary));
            imageMenu.setVisibility(View.GONE);
            imageHome.setVisibility(View.VISIBLE);
        }

        invalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        LatLng latLng = new LatLng(Double.parseDouble(session.getCoordinates(Constant.LATITUDE)), Double.parseDouble(session.getCoordinates(Constant.LONGITUDE)));
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Current Location"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(19));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        Objects.requireNonNull(fragment).onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        try {
            if (WalletTransactionFragment.payFromWallet) {
                WalletTransactionFragment.payFromWallet = false;
                new WalletTransactionFragment().AddWalletBalance(activity, new Session(activity), WalletTransactionFragment.amount, WalletTransactionFragment.msg, razorpayPaymentID);
            } else {
                PaymentFragment.razorPayId = razorpayPaymentID;
                new PaymentFragment().PlaceOrder(MainActivity.this, PaymentFragment.paymentMethod, PaymentFragment.razorPayId, true, PaymentFragment.sendParams, Constant.SUCCESS);
            }
        } catch (Exception e) {
            Log.d(TAG, "onPaymentSuccess  ", e);
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        try {
            try {
                if (PaymentFragment.dialog_ != null) {
                    PaymentFragment.dialog_.dismiss();
                }
            } catch (Exception ignore) {

            }
            Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d(TAG, "onPaymentError  ", e);
        }
    }

    public void GetProductsName() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ALL_PRODUCTS_NAME, Constant.GetVal);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.GET_ALL_PRODUCTS_NAME, jsonObject.getString(Constant.DATA));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }


    @Override
    protected void onPause() {
        invalidateOptionsMenu();
        super.onPause();
    }

    @Override
    protected void onResume() {
        ApiConfig.getWalletBalance(activity, session);
        super.onResume();
    }
}