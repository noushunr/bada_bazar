package wrteam.multivendor.shop.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.adapter.FavoriteLoadMoreAdapter;
import wrteam.multivendor.shop.adapter.OfflineFavoriteAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.DatabaseHelper;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Favorite;
import wrteam.multivendor.shop.model.Product;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static wrteam.multivendor.shop.helper.ApiConfig.GetSettings;


public class FavoriteFragment extends Fragment {
    public static ArrayList<Favorite> favoriteArrayList;
    public static ArrayList<Product> productArrayList;
    public static FavoriteLoadMoreAdapter favoriteLoadMoreAdapter;
    public static OfflineFavoriteAdapter offlineFavoriteAdapter;
    public static RecyclerView recyclerView;
    public static RelativeLayout tvAlert;
    View root;
    Session session;
    int total;
    NestedScrollView nestedScrollView;
    Activity activity;
    boolean isLogin;
    DatabaseHelper databaseHelper;
    int offset = 0;
    SwipeRefreshLayout swipeLayout;
    boolean isLoadMore = false;
    boolean isGrid = false;
    int resource;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_favorite, container, false);
        setHasOptionsMenu(true);

        activity = getActivity();

        session = new Session(activity);

        if (session.getGrid("grid")) {
            resource = R.layout.lyt_item_grid;
            isGrid = true;

            recyclerView = root.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));

        } else {
            resource = R.layout.lyt_item_list;
            isGrid = false;

            recyclerView = root.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        }

        isLogin = session.getBoolean(Constant.IS_USER_LOGIN);
        databaseHelper = new DatabaseHelper(activity);

        swipeLayout = root.findViewById(R.id.swipeLayout);

        tvAlert = root.findViewById(R.id.tvAlert);
        nestedScrollView = root.findViewById(R.id.nestedScrollView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);


        GetSettings(activity);

        if (ApiConfig.isConnected(activity)) {
            if (isLogin) {
                ApiConfig.getWalletBalance(activity, new Session(activity));
                GetData();
            } else {
                GetOfflineData();
            }
        }

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeLayout.setOnRefreshListener(() -> {
            if (ApiConfig.isConnected(activity)) {

                if (new Session(activity).getBoolean(Constant.IS_USER_LOGIN)) {
                    ApiConfig.getWalletBalance(activity, new Session(activity));
                }
                if (isLogin) {
                    if (Constant.CartValues.size() > 0) {
                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                    }
                    offset = 0;
                    GetData();
                } else {
                    GetOfflineData();
                }
            }
            swipeLayout.setRefreshing(false);
        });

        return root;
    }

    void GetData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_FAVORITES, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        params.put(Constant.OFFSET, offset + "");

        ApiConfig.RequestToVolley((result, response) -> {

            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        if (offset == 0) {
                            favoriteArrayList = new ArrayList<>();
                            recyclerView.setVisibility(View.VISIBLE);
                            tvAlert.setVisibility(View.GONE);
                        }
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        favoriteArrayList.addAll(ApiConfig.GetFavoriteProductList(jsonArray));
                        if (offset == 0) {
                            favoriteLoadMoreAdapter = new FavoriteLoadMoreAdapter(getContext(), favoriteArrayList, resource);
                            favoriteLoadMoreAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(favoriteLoadMoreAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                    if (favoriteArrayList.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == favoriteArrayList.size() - 1) {
                                                //bottom of list!
                                                favoriteArrayList.add(null);
                                                favoriteLoadMoreAdapter.notifyItemInserted(favoriteArrayList.size() - 1);

                                                offset = offset + Constant.LOAD_ITEM_LIMIT;
                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.GET_FAVORITES, Constant.GetVal);
                                                params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                                                    params1.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                                                }
                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
                                                params1.put(Constant.OFFSET, offset + "");

                                                ApiConfig.RequestToVolley((result1, response1) -> {

                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {

                                                                JSONObject object1 = new JSONObject(response1);
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);
                                                                favoriteArrayList.remove(favoriteArrayList.size() - 1);
                                                                favoriteLoadMoreAdapter.notifyItemRemoved(favoriteArrayList.size());

                                                                favoriteArrayList.addAll(ApiConfig.GetFavoriteProductList(jsonArray1));
                                                                favoriteLoadMoreAdapter.notifyDataSetChanged();
                                                                favoriteLoadMoreAdapter.setLoaded();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                            mShimmerViewContainer.stopShimmer();
                                                            mShimmerViewContainer.setVisibility(View.GONE);
                                                            recyclerView.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                }, activity, Constant.GET_FAVORITES_URL, params1, false);
                                                isLoadMore = true;
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        if (offset == 0) {
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            tvAlert.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.GET_FAVORITES_URL, params, false);
    }


    void GetOfflineData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        if (databaseHelper.getFavourite().size() >= 1) {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GET_PRODUCTS_OFFLINE, Constant.GetVal);
            params.put(Constant.PRODUCT_IDs, databaseHelper.getFavourite().toString().replace("[", "").replace("]", "").replace("\"", ""));
            if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
            }
            ApiConfig.RequestToVolley((result, response) -> {

                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                            productArrayList = new ArrayList<>();
                            recyclerView.setVisibility(View.VISIBLE);
                            tvAlert.setVisibility(View.GONE);
                            offlineFavoriteAdapter = new OfflineFavoriteAdapter(getContext(), productArrayList, resource);
                            offlineFavoriteAdapter.setHasStableIds(true);
                            productArrayList.addAll(ApiConfig.GetProductList(jsonArray));
                            recyclerView.setAdapter(offlineFavoriteAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            tvAlert.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }, activity, Constant.GET_PRODUCTS_URL, params, false);
        } else {
            recyclerView.setVisibility(View.GONE);
            tvAlert.setVisibility(View.VISIBLE);
            mShimmerViewContainer.stopShimmer();
            mShimmerViewContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.title_fav);
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
