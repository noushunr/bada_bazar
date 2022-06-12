package wrteam.multivendor.shop.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import co.paystack.android.PaystackSdk;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.model.Favorite;
import wrteam.multivendor.shop.model.OrderTracker;
import wrteam.multivendor.shop.model.PriceVariation;
import wrteam.multivendor.shop.model.Product;
import wrteam.multivendor.shop.model.Slider;

@SuppressWarnings("deprecation")
public class ApiConfig extends Application {

    public static final String TAG = ApiConfig.class.getSimpleName();
    static ApiConfig mInstance;
    static AppEnvironment appEnvironment;
    static boolean isDialogOpen = false;
    RequestQueue mRequestQueue;

    public static String VolleyErrorMessage(VolleyError error) {
        String message = "";
        try {
            if (error instanceof NetworkError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (error instanceof ServerError) {
                message = "The server could not be found. Please try again after some time!";
            } else if (error instanceof AuthFailureError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again after some time!";
            } else if (error instanceof TimeoutError) {
                message = "Connection TimeOut! Please check your internet connection.";
            } else
                message = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    @Deprecated
    public static void displayLocationSettingsRequest(final Activity activity) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity).addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:

                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(activity, 110);
                    } catch (IntentSender.SendIntentException e) {
                        Log.i("TAG", "PendingIntent unable to execute request.");
                    }
                    break;
            }
        });
    }

    public static OrderTracker OrderTracker(JSONObject jsonObject) {
        OrderTracker orderTracker = null;
        try {
            ArrayList<OrderTracker> itemList = new ArrayList<>();
            JSONArray ItemsArray = jsonObject.getJSONArray("items");

            for (int j = 0; j < ItemsArray.length(); j++) {

                JSONObject itemsObject = ItemsArray.getJSONObject(j);

                JSONArray statusArray1 = itemsObject.getJSONArray("status");
                ArrayList<OrderTracker> statusList = new ArrayList<>();

                for (int k = 0; k < statusArray1.length(); k++) {
                    JSONArray sArray = statusArray1.getJSONArray(k);
                    String sName = sArray.getString(0);
                    String sDate = sArray.getString(1);
                    statusList.add(new OrderTracker(sName, sDate));
                }

                itemList.add(new OrderTracker(itemsObject.getString(Constant.ID),
                        itemsObject.getString(Constant.ORDER_ID),
                        itemsObject.getString(Constant.USER_ID),
                        itemsObject.getString(Constant.PRODUCT_VARIANT_ID),
                        itemsObject.getString(Constant.QUANTITY),
                        itemsObject.getString(Constant.PRICE),
                        itemsObject.getString(Constant.DISCOUNT),
                        itemsObject.getString(Constant.SUB_TOTAL),
                        itemsObject.getString(Constant.NAME),
                        itemsObject.getString(Constant.IMAGE),
                        itemsObject.getString(Constant.MEASUREMENT),
                        itemsObject.getString(Constant.UNIT),
                        jsonObject.getString(Constant.PAYMENT_METHOD),
                        itemsObject.getString(Constant.ACTIVE_STATUS),
                        itemsObject.getString(Constant.DATE_ADDED),
                        statusList,
                        itemsObject.getString(Constant.RETURN_STATUS),
                        itemsObject.getString(Constant.RETURN_DAYS),
                        itemsObject.getString(Constant.CANCELLABLE_STATUS),
                        itemsObject.getString(Constant.TILL_STATUS),
                        itemsObject.getString(Constant.DISCOUNTED_PRICE),
                        itemsObject.getString(Constant.TAX_PERCENT)));
            }

            orderTracker = new OrderTracker(
                    jsonObject.getString(Constant.ID),
                    jsonObject.getString(Constant.OTP),
                    jsonObject.getString(Constant.USER_ID),
                    jsonObject.getString(Constant.DATE_ADDED),
                    jsonObject.getString(Constant.MOBILE),
                    jsonObject.getString(Constant.DELIVERY_CHARGE),
                    jsonObject.getString(Constant.PAYMENT_METHOD),
                    jsonObject.getString(Constant.ADDRESS),
                    jsonObject.getString(Constant.TOTAL),
                    jsonObject.getString(Constant.FINAL_TOTAL),
                    jsonObject.getString(Constant.TAX_AMOUNT),
                    jsonObject.getString(Constant.TAX_PERCENT),
                    jsonObject.getString(Constant.KEY_WALLET_BALANCE),
                    jsonObject.getString(Constant.PROMO_CODE),
                    jsonObject.getString(Constant.PROMO_DISCOUNT),
                    jsonObject.getString(Constant.DISCOUNT),
                    jsonObject.getString(Constant.DISCOUNT_AMT),
                    jsonObject.getString(Constant.USER_NAME), itemList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderTracker;
    }

    @SuppressLint("DefaultLocale")
    public static String StringFormat(String number) {
        return String.format("%.2f", Double.parseDouble(number));
    }

    public static String getMonth(int monthNo, Activity activity) {
        String month = "";
        String[] month_ = {activity.getString(R.string.jan), activity.getString(R.string.feb), activity.getString(R.string.mar), activity.getString(R.string.apr), activity.getString(R.string.may), activity.getString(R.string.jun), activity.getString(R.string.jul), activity.getString(R.string.aug), activity.getString(R.string.sep), activity.getString(R.string.oct), activity.getString(R.string.nov), activity.getString(R.string.dec)};
        if (monthNo != 0) {
            month = month_[monthNo - 1];
        }
        return month;
    }

    public static String getDayOfWeek(int dayNo, Activity activity) {
        String day = "";

        String[] day_ = {activity.getString(R.string.sun), activity.getString(R.string.mon), activity.getString(R.string.tue), activity.getString(R.string.wed), activity.getString(R.string.thu), activity.getString(R.string.fri), activity.getString(R.string.sat)};
        if (dayNo != 0) {
            day = day_[dayNo - 1];
        }
        return day;
    }

    public static ArrayList<String> getDates(String startDate, String endDate) {
        ArrayList<String> dates = new ArrayList<>();
        @SuppressLint("SimpleDateFormat")
        DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1.parse(startDate);
            date2 = df1.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        assert date1 != null;
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        assert date2 != null;
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            dates.add(cal1.get(Calendar.DATE) + "-" + (cal1.get(Calendar.MONTH) + 1) + "-" + cal1.get(Calendar.YEAR) + "-" + cal1.get(Calendar.DAY_OF_WEEK));
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }

    public static void removeAddress(final Activity activity, String addressId) {

        Map<String, String> params = new HashMap<>();
        params.put(Constant.DELETE_ADDRESS, Constant.GetVal);
        params.put(Constant.ID, addressId);

        ApiConfig.RequestToVolley((result, response) -> {
        }, activity, Constant.GET_ADDRESS_URL, params, false);
    }

    public static void getCartItemCount(final Activity activity, Session session) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        Constant.TOTAL_CART_ITEM = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                    } else {
                        Constant.TOTAL_CART_ITEM = 0;
                    }
                    activity.invalidateOptionsMenu();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.CART_URL, params, false);
    }

    public static void AddOrRemoveFavorite(Activity activity, Session session, String productID, boolean isAdd) {
        Map<String, String> params = new HashMap<>();
        if (isAdd) {
            params.put(Constant.ADD_TO_FAVORITES, Constant.GetVal);
        } else {
            params.put(Constant.REMOVE_FROM_FAVORITES, Constant.GetVal);
        }
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.PRODUCT_ID, productID);

        ApiConfig.RequestToVolley((result, response) -> {
        }, activity, Constant.GET_FAVORITES_URL, params, false);
    }

    public static void RequestToVolley(final VolleyCallback callback, final Activity activity, final String url, final Map<String, String> params, final boolean isProgress) {
        if (ProgressDisplay.mProgressBar != null) {
            ProgressDisplay.mProgressBar.setVisibility(View.GONE);
        }
        final ProgressDisplay progressDisplay = new ProgressDisplay(activity);
        progressDisplay.hideProgress();
        if (ApiConfig.isConnected(activity)) {
            if (isProgress)
                progressDisplay.showProgress();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
                callback.onSuccess(true, response);
                if (isProgress)
                    progressDisplay.hideProgress();
            }, error -> {
                if (isProgress)
                    progressDisplay.hideProgress();
                callback.onSuccess(false, "");
                String message = VolleyErrorMessage(error);
                if (!message.equals(""))
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params1 = new HashMap<>();
                    params1.put(Constant.AUTHORIZATION, "Bearer " + createJWT("eKart", "eKart Authentication"));
                    return params1;
                }

                @Override
                protected Map<String, String> getParams() {
                    params.put(Constant.AccessKey, Constant.AccessKeyVal);
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
            ApiConfig.getInstance().getRequestQueue().getCache().clear();
            ApiConfig.getInstance().addToRequestQueue(stringRequest);
        }

    }

    public static void RequestToVolley(final VolleyCallback callback, final String url, final Map<String, String> params) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> callback.onSuccess(true, response), error -> callback.onSuccess(false, "")) {
            @Override
            protected Map<String, String> getParams() {
                params.put(Constant.AccessKey, Constant.AccessKeyVal);
                return params;
            }
        };
        getInstance().getRequestQueue().getCache().clear();
        getInstance().addToRequestQueue(stringRequest);
    }

    public static String toTitleCase(String str) {
        if (str == null) {
            return null;
        }
        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    public static String createJWT(String issuer, String subject) {
        try {
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            byte[] apiKeySecretBytes = Constant.JWT_KEY.getBytes();
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
            JwtBuilder builder = Jwts.builder()
                    .setIssuedAt(now)
                    .setSubject(subject)
                    .setIssuer(issuer)
                    .signWith(signatureAlgorithm, signingKey);

            return builder.compact();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean CheckValidation(String item, boolean isMailValidation, boolean isMobileValidation) {
        boolean result = false;
        if (item.length() == 0) {
            result = true;
        } else if (isMailValidation) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(item).matches()) {
                result = true;
            }
        } else if (isMobileValidation) {
            if (!android.util.Patterns.PHONE.matcher(item).matches()) {
                result = true;
            }
        }
        return result;
    }

    @SuppressLint("DefaultLocale")
    public static String GetDiscount(String oldPrice, String newPrice) {
        return " (" + String.format("%.2f", (((Double.parseDouble(newPrice) / Double.parseDouble(oldPrice)) - 1) * 100)) + "%)";
    }

    public static void AddMultipleProductInCart(final Session session, final Activity activity, HashMap<String, String> map) {
        if (map.size() > 0) {
            String ids = map.keySet().toString().replace("[", "").replace("]", "").replace(" ", "");
            String qty = map.values().toString().replace("[", "").replace("]", "").replace(" ", "");

            Map<String, String> params = new HashMap<>();
            params.put(Constant.ADD_MULTIPLE_ITEMS, Constant.GetVal);
            params.put(Constant.USER_ID, session.getData(Constant.ID));
            params.put(Constant.PRODUCT_VARIANT_ID, ids);
            params.put(Constant.QTY, qty);

            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    getCartItemCount(activity, session);
                }
            }, activity, Constant.CART_URL, params, false);
        }
    }

    public static void addMarkers(int currentPage, ArrayList<Slider> imageList, LinearLayout
            mMarkersLayout, Context context) {

        if (context != null) {
            TextView[] markers = new TextView[imageList.size()];

            mMarkersLayout.removeAllViews();

            for (int i = 0; i < markers.length; i++) {
                markers[i] = new TextView(context);
                markers[i].setText(Html.fromHtml("&#8226;"));
                markers[i].setTextSize(35);
                markers[i].setTextColor(context.getResources().getColor(R.color.gray));
                mMarkersLayout.addView(markers[i]);
            }
            if (markers.length > 0)
                markers[currentPage].setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
    }

    public static Drawable buildCounterDrawable(int count, Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.counter_menuitem_layout, null);
        TextView textView = view.findViewById(R.id.count);
        RelativeLayout lytCount = view.findViewById(R.id.lytCount);
        if (count == 0) {
            lytCount.setVisibility(View.GONE);
        } else {
            lytCount.setVisibility(View.VISIBLE);
            textView.setText("" + count);
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(activity.getResources(), bitmap);
    }

    public static void GetSettings(final Activity activity) {
        Session session = new Session(activity);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(Constant.GET_TIMEZONE, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        JSONObject object = jsonObject.getJSONObject(Constant.SETTINGS);

                        session.setData(Constant.minimum_version_required, object.getString(Constant.minimum_version_required));
                        session.setData(Constant.is_version_system_on, object.getString(Constant.is_version_system_on));

                        session.setData(Constant.CURRENCY, object.getString(Constant.CURRENCY));

                        session.setData(Constant.min_order_amount, object.getString(Constant.min_order_amount));
                        session.setData(Constant.max_cart_items_count, object.getString(Constant.max_cart_items_count));
                        session.setData(Constant.area_wise_delivery_charge, object.getString(Constant.area_wise_delivery_charge));

                        session.setData(Constant.is_refer_earn_on, object.getString(Constant.is_refer_earn_on));
                        session.setData(Constant.refer_earn_bonus, object.getString(Constant.refer_earn_bonus));
                        session.setData(Constant.refer_earn_bonus, object.getString(Constant.refer_earn_bonus));
                        session.setData(Constant.refer_earn_method, object.getString(Constant.refer_earn_method));
                        session.setData(Constant.max_refer_earn_amount, object.getString(Constant.max_refer_earn_amount));

                        session.setData(Constant.max_product_return_days, object.getString(Constant.max_product_return_days));
                        session.setData(Constant.user_wallet_refill_limit, object.getString(Constant.user_wallet_refill_limit));
                        session.setData(Constant.min_refer_earn_order_amount, object.getString(Constant.min_refer_earn_order_amount));

                        if (!session.getBoolean("update_skip")) {
                            String versionName = "";
                            try {
                                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                                versionName = packageInfo.versionName;
                            } catch (PackageManager.NameNotFoundException ignore) {
                            }
                            if (ApiConfig.compareVersion(versionName, session.getData(Constant.minimum_version_required)) < 0) {
                                ApiConfig.OpenBottomDialog(activity);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    public static void OpenBottomDialog(final Activity activity) {
        try {
            @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_update_app, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetTheme);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.show();
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            ImageView imgClose = sheetView.findViewById(R.id.imgClose);
            Button btnNotNow = sheetView.findViewById(R.id.btnNotNow);
            Button btnUpdateNow = sheetView.findViewById(R.id.btnUpdateNow);
            if (new Session(activity).getData(Constant.is_version_system_on).equals("0")) {
                {
                    btnNotNow.setVisibility(View.VISIBLE);
                    imgClose.setVisibility(View.VISIBLE);
                    mBottomSheetDialog.setCancelable(true);
                }
            } else {
                mBottomSheetDialog.setCancelable(false);
            }


            imgClose.setOnClickListener(v -> {
                if (mBottomSheetDialog.isShowing())
                    new Session(activity).setBoolean("update_skip", true);
                mBottomSheetDialog.dismiss();
            });
            btnNotNow.setOnClickListener(v -> {
                new Session(activity).setBoolean("update_skip", true);
                if (mBottomSheetDialog.isShowing())
                    mBottomSheetDialog.dismiss();
            });

            btnUpdateNow.setOnClickListener(view -> activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.PLAY_STORE_LINK + activity.getPackageName()))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public static void getWalletBalance(final Activity activity, Session session) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GET_USER_DATA, Constant.GetVal);
            params.put(Constant.USER_ID, session.getData(Constant.ID));
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            JSONObject jsonObject = object.getJSONArray(Constant.DATA).getJSONObject(0);
                            Constant.WALLET_BALANCE = Double.parseDouble(jsonObject.getString(Constant.BALANCE));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.USER_DATA_URL, params, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("SetTextI18n")
    public static void setOrderTrackerLayout(Activity activity, OrderTracker order, RecyclerView.ViewHolder holder) {
        for (int i = 0; i < order.getOrderStatusArrayList().size(); i++) {
            View v = holder.itemView;
            int img = v.getResources().getIdentifier("img" + i, "id", activity.getPackageName());
            int view = v.getResources().getIdentifier("l" + i, "id", activity.getPackageName());
            int txt = v.getResources().getIdentifier("txt" + i, "id", activity.getPackageName());
            int textview = v.getResources().getIdentifier("txt" + i + "" + i, "id", activity.getPackageName());

            if (img != 0 && v.findViewById(img) != null) {
                ImageView imageView = v.findViewById(img);
                imageView.setColorFilter(activity.getResources().getColor(R.color.colorAccent));
            }
            if (view != 0 && v.findViewById(view) != null) {
                View view1 = v.findViewById(view);
                view1.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
            }
            if (txt != 0 && v.findViewById(txt) != null) {
                TextView view1 = v.findViewById(txt);
                view1.setTextColor(activity.getResources().getColor(R.color.black));
            }
            if (textview != 0 && v.findViewById(textview) != null) {
                TextView view1 = v.findViewById(textview);
                String str = order.getOrderStatusArrayList().get(i).getStatusdate();
                String[] split = str.split("\\s+");
                view1.setText(split[0] + "\n" + split[1]);
            }
        }
    }

    public static String getAddress(double lat, double lng, Activity activity) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            return addresses.get(0).getAddressLine(0);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int compareVersion(String version1, String version2) {
        String[] arr1 = version1.split("\\.");
        String[] arr2 = version2.split("\\.");

        int i = 0;
        while (i < arr1.length || i < arr2.length) {
            if (i < arr1.length && i < arr2.length) {
                if (Integer.parseInt(arr1[i]) < Integer.parseInt(arr2[i])) {
                    return -1;
                } else if (Integer.parseInt(arr1[i]) > Integer.parseInt(arr2[i])) {
                    return 1;
                }
            } else if (i < arr1.length) {
                if (Integer.parseInt(arr1[i]) != 0) {
                    return 1;
                }
            } else {
                if (Integer.parseInt(arr2[i]) != 0) {
                    return -1;
                }
            }

            i++;
        }

        return 0;
    }

    public static synchronized ApiConfig getInstance() {
        return mInstance;
    }

    public static Boolean isConnected(final Activity activity) {
        boolean check = false;
        try {
            ConnectivityManager ConnectionManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = ConnectionManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                check = true;
            } else {
                try {
                    if (!isDialogOpen) {
                        @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_no_internet, null);
                        ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
                        if (parentViewGroup != null) {
                            parentViewGroup.removeAllViews();
                        }

                        final Dialog mBottomSheetDialog = new Dialog(activity);
                        mBottomSheetDialog.setContentView(sheetView);
                        mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        mBottomSheetDialog.show();
                        isDialogOpen = true;
                        Button btnRetry = sheetView.findViewById(R.id.btnRetry);
                        mBottomSheetDialog.setCancelable(false);

                        btnRetry.setOnClickListener(view -> {
                            if (isConnected(activity)) {
                                isDialogOpen = false;
                                mBottomSheetDialog.dismiss();
                            }
                        });
                    }
                } catch (Exception ignored) {

                }
            }
        } catch (Exception ignored) {

        }
        return check;
    }


    public static ArrayList<Product> GetProductList(JSONArray jsonArray) {
        ArrayList<Product> productArrayList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ArrayList<PriceVariation> priceVariations = new ArrayList<>();
                    JSONArray priceArray = jsonObject.getJSONArray(Constant.VARIANT);

                    for (int j = 0; j < priceArray.length(); j++) {
                        JSONObject obj = priceArray.getJSONObject(j);
                        String discountpercent = "0";
                        if (!obj.getString(Constant.DISCOUNTED_PRICE).equals("0")) {
                            discountpercent = ApiConfig.GetDiscount(obj.getString(Constant.PRICE), obj.getString(Constant.DISCOUNTED_PRICE));
                        }
                        priceVariations.add(new PriceVariation(obj.getString(Constant.CART_ITEM_COUNT), obj.getString(Constant.ID), obj.getString(Constant.PRODUCT_ID), obj.getString(Constant.TYPE), obj.getString(Constant.MEASUREMENT), obj.getString(Constant.MEASUREMENT_UNIT_ID), obj.getString(Constant.PRICE), obj.getString(Constant.DISCOUNTED_PRICE), obj.getString(Constant.SERVE_FOR), obj.getString(Constant.STOCK), obj.getString(Constant.STOCK_UNIT_ID), obj.getString(Constant.MEASUREMENT_UNIT_NAME), obj.getString(Constant.STOCK_UNIT_NAME), discountpercent));
                    }
                    productArrayList.add(new Product(jsonObject.getString(Constant.SELLER_NAME), jsonObject.getString(Constant.RETURN_DAYS), jsonObject.getString(Constant.IS_APPROVED), jsonObject.getString(Constant.SELLER_ID), jsonObject.getString(Constant.TAX_ID), jsonObject.getString(Constant.TAX_PERCENT), jsonObject.getString(Constant.ROW_ORDER), jsonObject.getString(Constant.TILL_STATUS), jsonObject.getString(Constant.CANCELLABLE_STATUS), jsonObject.getString(Constant.MANUFACTURER), jsonObject.getString(Constant.MADE_IN), jsonObject.getString(Constant.RETURN_STATUS), jsonObject.getString(Constant.ID), jsonObject.getString(Constant.NAME), jsonObject.getString(Constant.SLUG), jsonObject.getString(Constant.SUC_CATE_ID), jsonObject.getString(Constant.IMAGE), jsonObject.getJSONArray(Constant.OTHER_IMAGES), jsonObject.getString(Constant.DESCRIPTION), jsonObject.getString(Constant.STATUS), jsonObject.getString(Constant.DATE_ADDED), jsonObject.getBoolean(Constant.IS_FAVORITE), jsonObject.getString(Constant.CATEGORY_ID), priceVariations, jsonObject.getString(Constant.INDICATOR)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productArrayList;

    }

    public static ArrayList<Favorite> GetFavoriteProductList(JSONArray jsonArray) {
        ArrayList<Favorite> productArrayList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ArrayList<PriceVariation> priceVariations = new ArrayList<>();
                    JSONArray priceArray = jsonObject.getJSONArray(Constant.VARIANT);

                    for (int j = 0; j < priceArray.length(); j++) {
                        JSONObject obj = priceArray.getJSONObject(j);
                        String discountpercent = "0";
                        if (!obj.getString(Constant.DISCOUNTED_PRICE).equals("0")) {
                            discountpercent = ApiConfig.GetDiscount(obj.getString(Constant.PRICE), obj.getString(Constant.DISCOUNTED_PRICE));
                        }
                        priceVariations.add(new PriceVariation(obj.getString(Constant.CART_ITEM_COUNT), obj.getString(Constant.ID), obj.getString(Constant.PRODUCT_ID), obj.getString(Constant.TYPE), obj.getString(Constant.MEASUREMENT), obj.getString(Constant.MEASUREMENT_UNIT_ID), obj.getString(Constant.PRICE), obj.getString(Constant.DISCOUNTED_PRICE), obj.getString(Constant.SERVE_FOR), obj.getString(Constant.STOCK), obj.getString(Constant.STOCK_UNIT_ID), obj.getString(Constant.MEASUREMENT_UNIT_NAME), obj.getString(Constant.STOCK_UNIT_NAME), discountpercent));
                    }
                    productArrayList.add(new Favorite(jsonObject.getString(Constant.PRODUCT_ID), jsonObject.getString(Constant.CANCELLABLE_STATUS), jsonObject.getString(Constant.TILL_STATUS), jsonObject.getString(Constant.MANUFACTURER), jsonObject.getString(Constant.MADE_IN), jsonObject.getString(Constant.RETURN_STATUS), jsonObject.getString(Constant.ID), jsonObject.getString(Constant.NAME), jsonObject.getString(Constant.SLUG), jsonObject.getString(Constant.SUC_CATE_ID), jsonObject.getString(Constant.IMAGE), jsonObject.getJSONArray(Constant.OTHER_IMAGES), jsonObject.getString(Constant.DESCRIPTION), jsonObject.getString(Constant.STATUS), jsonObject.getString(Constant.DATE_ADDED), jsonObject.getBoolean(Constant.IS_FAVORITE), jsonObject.getString(Constant.CATEGORY_ID), priceVariations, jsonObject.getString(Constant.INDICATOR)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productArrayList;
    }

    public static void SetAppEnvironment(Activity activity) {
        if (Constant.PAYUMONEY_MODE.equals("production")) {
            appEnvironment = AppEnvironment.PRODUCTION;
        } else if (Constant.PAYUMONEY_MODE.equals("sandbox")) {
            appEnvironment = AppEnvironment.SANDBOX;
        } else {
            appEnvironment = AppEnvironment.SANDBOX;
        }
        PaystackSdk.initialize(activity);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        FirebaseApp.initializeApp(this);
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
}