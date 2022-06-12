package wrteam.multivendor.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.adapter.ItemsAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.OrderTracker;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class TrackerDetailFragment extends Fragment {
    public static Button btnReorder;
    View root;
    OrderTracker order;
    TextView tvOrderOTP, tvItemTotal, tvDeliveryCharge, tvTotal, tvPromoCode, tvPCAmount, tvWallet, tvFinalTotal, tvDPercent, tvDAmount;
    TextView tvOtherDetail, tvOrderId, tvOrderDate;
    RecyclerView recyclerView;
    RelativeLayout relativeLyt;
    LinearLayout lytPromo, lytWallet, lytPriceDetail, lytOTP;
    double totalAfterTax = 0.0;
    Activity activity;
    String id;
    Session session;
    HashMap<String, String> hashMap;
    private ShimmerFrameLayout mShimmerViewContainer;
    ScrollView scrollView;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tracker_detail, container, false);
        activity = getActivity();
        session = new Session(activity);

        lytPriceDetail = root.findViewById(R.id.lytPriceDetail);
        lytPromo = root.findViewById(R.id.lytPromo);
        lytWallet = root.findViewById(R.id.lytWallet);
        tvItemTotal = root.findViewById(R.id.tvItemTotal);
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge);
        tvDAmount = root.findViewById(R.id.tvDAmount);
        tvDPercent = root.findViewById(R.id.tvDPercent);
        tvTotal = root.findViewById(R.id.tvTotal);
        tvPromoCode = root.findViewById(R.id.tvPromoCode);
        tvPCAmount = root.findViewById(R.id.tvPCAmount);
        tvWallet = root.findViewById(R.id.tvWallet);
        tvFinalTotal = root.findViewById(R.id.tvFinalTotal);
        tvOrderId = root.findViewById(R.id.tvOrderId);
        tvOrderDate = root.findViewById(R.id.tvOrderDate);
        relativeLyt = root.findViewById(R.id.relativeLyt);
        tvOtherDetail = root.findViewById(R.id.tvOtherDetail);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        btnReorder = root.findViewById(R.id.btnReorder);
        tvOrderOTP = root.findViewById(R.id.tvOrderOTP);
        lytOTP = root.findViewById(R.id.lytOTP);
        scrollView = root.findViewById(R.id.scrollView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        hashMap = new HashMap<>();

        id = requireArguments().getString("id");
        if (id.equals("")) {
            order = (OrderTracker) getArguments().getSerializable("model");
            id = order.getId();
            SetData(order);
        } else {
            getOrderDetails(id);
        }


        setHasOptionsMenu(true);

        btnReorder.setOnClickListener(view -> new AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.re_order))
                .setMessage(getString(R.string.reorder_msg))
                .setPositiveButton(getString(R.string.proceed), (dialog, which) -> {
                    if (getContext() != null) {
                        GetReOrderData();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss()).show());

        return root;
    }

    public void GetReOrderData() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_REORDER_DATA, Constant.GetVal);
        params.put(Constant.ID, id);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONObject(Constant.DATA).getJSONArray(Constant.ITEMS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        hashMap.put(jsonArray.getJSONObject(i).getString(Constant.PRODUCT_VARIANT_ID), jsonArray.getJSONObject(i).getString(Constant.QUANTITY));
                    }
                    ApiConfig.AddMultipleProductInCart(session, activity, hashMap);
                } catch (JSONException e) {
                        e.printStackTrace();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }

    public void getOrderDetails(String id) {
        scrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ORDERS, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.ORDER_ID, id);

        //  System.out.println("=====params " + params.toString());
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        JSONObject jsonObject = jsonObject1.getJSONArray(Constant.DATA).getJSONObject(0);
                        SetData(ApiConfig.OrderTracker(jsonObject));
                    } else {
                        scrollView.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.setVisibility(View.GONE);
                        mShimmerViewContainer.stopShimmer();
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                    scrollView.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmer();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }

    @SuppressLint("SetTextI18n")
    public void SetData(OrderTracker order) {
        try {
            String[] date = order.getDate_added().split("\\s+");
            tvOrderId.setText(order.getId());
            if (order.getOtp().equals("0")) {
                lytOTP.setVisibility(View.GONE);
            } else {
                tvOrderOTP.setText(order.getOtp());
            }
            tvOrderDate.setText(date[0]);
            tvOtherDetail.setText(getString(R.string.name_1) + order.getUsername() + getString(R.string.mobile_no_1) + order.getMobile() + getString(R.string.address_1) + order.getAddress());
            totalAfterTax = (Double.parseDouble(order.getTotal()) + Double.parseDouble(order.getDelivery_charge()));
            tvItemTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getTotal()));
            tvDeliveryCharge.setText("+ " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getDelivery_charge()));
            tvDPercent.setText(getString(R.string.discount) + "(" + order.getdPercent() + "%) :");
            tvDAmount.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getdAmount()));
            tvTotal.setText(session.getData(Constant.CURRENCY) + totalAfterTax);
            tvPCAmount.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getPromoDiscount()));
            tvWallet.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getWalletBalance()));
            tvFinalTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getFinal_total()));

            scrollView.setVisibility(View.VISIBLE);
            mShimmerViewContainer.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmer();

            recyclerView.setAdapter(new ItemsAdapter(activity, order.getItemsList(), "detail"));
            relativeLyt.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.order_track_detail);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
    }
}