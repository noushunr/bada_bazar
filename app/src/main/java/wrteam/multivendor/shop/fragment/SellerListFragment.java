package wrteam.multivendor.shop.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.adapter.SellerAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Seller;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class SellerListFragment extends Fragment {

    public static ArrayList<Seller> sellerArrayList;
    TextView tvNoData;
    RecyclerView sellerRecyclerView;
    SwipeRefreshLayout swipeLayout;
    View root;
    Activity activity;
    private ShimmerFrameLayout mShimmerViewContainer;
    Session session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_seller_list, container, false);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        activity = getActivity();
        session = new Session(activity);
        setHasOptionsMenu(true);

        tvNoData = root.findViewById(R.id.tvNoData);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        sellerRecyclerView = root.findViewById(R.id.sellerRecyclerView);

        sellerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), Constant.GRID_COLUMN));
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        swipeLayout.setOnRefreshListener(() -> {
            if (ApiConfig.isConnected(activity)) {
                if (new Session(activity).getBoolean(Constant.IS_USER_LOGIN)) {
                    ApiConfig.getWalletBalance(activity, new Session(activity));
                }
                GetSellerList();
            }
            swipeLayout.setRefreshing(false);
        });

        if (ApiConfig.isConnected(activity)) {
            GetSellerList();
        }

        return root;
    }

    void GetSellerList() {
        sellerRecyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SELLER_DATA, Constant.GetVal);
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }
        ApiConfig.RequestToVolley((result, response) -> {
            //System.out.println("======cate " + response);
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    sellerArrayList = new ArrayList<>();
                    if (!object.getBoolean(Constant.ERROR)) {
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        Gson gson = new Gson();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Seller seller = gson.fromJson(jsonObject.toString(), Seller.class);
                            sellerArrayList.add(seller);
                        }
                        sellerRecyclerView.setAdapter(new SellerAdapter(getContext(), activity, sellerArrayList, R.layout.lyt_seller, "category", 0));
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        sellerRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        tvNoData.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        sellerRecyclerView.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                            e.printStackTrace();
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    sellerRecyclerView.setVisibility(View.GONE);
                }
            }
        }, activity, Constant.SELLER_URL, params, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.seller);
        requireActivity().invalidateOptionsMenu();
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