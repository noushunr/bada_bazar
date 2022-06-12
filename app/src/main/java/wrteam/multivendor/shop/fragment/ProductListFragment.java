package wrteam.multivendor.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import wrteam.multivendor.shop.adapter.ProductLoadMoreAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Product;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static wrteam.multivendor.shop.helper.ApiConfig.GetSettings;


public class ProductListFragment extends Fragment {
    public static ArrayList<Product> productArrayList;
    @SuppressLint("StaticFieldLeak")
    public static ProductLoadMoreAdapter mAdapter;
    View root;
    Session session;
    int total;
    NestedScrollView nestedScrollView;
    Activity activity;
    int offset = 0;
    String id, filterBy, from;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;
    int filterIndex;
    TextView tvAlert;
    boolean isSort = false, isLoadMore = false;
    boolean isGrid = false;
    int resource;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_product_list, container, false);
        setHasOptionsMenu(true);
        offset = 0;
        activity = getActivity();

        session = new Session(activity);

        from = requireArguments().getString(Constant.FROM);
        id = getArguments().getString(Constant.ID);

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

        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        nestedScrollView = root.findViewById(R.id.nestedScrollView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        GetSettings(activity);

        filterIndex = -1;

        if (ApiConfig.isConnected(activity)) {
            switch (from) {
                case "regular":
                case "sub_cate":
                    GetData();
                    isSort = true;
                    break;
                case "similar":
                    GetSimilarData();
                    break;
                case "section":
                    GetSectionData();
                    break;
            }
        }

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeLayout.setOnRefreshListener(() -> {
            if (productArrayList != null && productArrayList.size() > 0) {
                offset = 0;
                swipeLayout.setRefreshing(false);
                productArrayList.clear();
                switch (from) {
                    case "regular":
                    case "sub_cate":
                        GetData();
                        break;
                    case "similar":
                        GetSimilarData();
                        break;
                    case "section":
                        GetSectionData();
                        break;
                }
            }
        });

        return root;
    }

    private void GetSectionData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.GET_ALL_SECTIONS, Constant.GetVal);
        params.put(Constant.SECTION_ID, id);
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {

                        JSONObject object = new JSONObject(response);
                        productArrayList = ApiConfig.GetProductList(object.getJSONArray(Constant.SECTIONS).getJSONObject(0).getJSONArray(Constant.PRODUCTS));
                        mAdapter = new ProductLoadMoreAdapter(activity, productArrayList, resource, from);
                        recyclerView.setAdapter(mAdapter);
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.GET_SECTION_URL, params, false);
    }

    void GetData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
        params.put(Constant.SUB_CATEGORY_ID, id);
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        params.put(Constant.OFFSET, "" + offset);
        if (filterIndex != -1) {
            params.put(Constant.SORT, filterBy);
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        if (offset == 0) {
                            productArrayList = new ArrayList<>();
                            tvAlert.setVisibility(View.GONE);
                        }
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        try {
                            productArrayList.addAll(ApiConfig.GetProductList(jsonArray));
                        } catch (Exception e) {
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        if (offset == 0) {
                            mAdapter = new ProductLoadMoreAdapter(activity, productArrayList, resource, from);
                            mAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(mAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                    if (productArrayList.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == productArrayList.size() - 1) {
                                                //bottom of list!
                                                productArrayList.add(null);
                                                mAdapter.notifyItemInserted(productArrayList.size() - 1);

                                                offset += Integer.parseInt("" + Constant.LOAD_ITEM_LIMIT);
                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
                                                params1.put(Constant.SUB_CATEGORY_ID, id);
                                                if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                                                    params1.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                                                }
                                                params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
                                                params1.put(Constant.OFFSET, "" + offset);
                                                if (filterIndex != -1) {
                                                    params1.put(Constant.SORT, filterBy);
                                                }

                                                ApiConfig.RequestToVolley((result1, response1) -> {

                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {

                                                                JSONObject object1 = new JSONObject(response1);
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);
                                                                productArrayList.remove(productArrayList.size() - 1);
                                                                mAdapter.notifyItemRemoved(productArrayList.size());
                                                                try {
                                                                    productArrayList.addAll(ApiConfig.GetProductList(jsonArray1));
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                mAdapter.notifyDataSetChanged();
                                                                mAdapter.setLoaded();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();

                                                        }
                                                    }
                                                }, activity, Constant.GET_PRODUCTS_URL, params1, false);
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
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }

    void GetSimilarData() {
        productArrayList = new ArrayList<>();
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
        params.put(Constant.PRODUCT_ID, id);
        params.put(Constant.CATEGORY_ID, requireArguments().getString("cat_id"));
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        params.put(Constant.OFFSET, "" + offset);

        ApiConfig.RequestToVolley((result, response) -> {

            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        try {
                            try {
                                productArrayList.addAll(ApiConfig.GetProductList(jsonArray));
                            } catch (Exception e) {
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        if (offset == 0) {
                            mAdapter = new ProductLoadMoreAdapter(activity, productArrayList, resource, from);
                            mAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(mAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                    if (productArrayList.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == productArrayList.size() - 1) {
                                                //bottom of list!
                                                productArrayList.add(null);
                                                mAdapter.notifyItemInserted(productArrayList.size() - 1);

                                                offset += Integer.parseInt("" + Constant.LOAD_ITEM_LIMIT);
                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
                                                params1.put(Constant.PRODUCT_ID, id);
                                                params1.put(Constant.CATEGORY_ID, requireArguments().getString("cat_id"));
                                                params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                                                    params1.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                                                }
                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
                                                params1.put(Constant.OFFSET, "" + offset);

                                                ApiConfig.RequestToVolley((result1, response1) -> {

                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {

                                                                JSONObject object1 = new JSONObject(response1);
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);
                                                                productArrayList.remove(productArrayList.size() - 1);
                                                                mAdapter.notifyItemRemoved(productArrayList.size());

                                                                try {
                                                                    productArrayList.addAll(ApiConfig.GetProductList(jsonArray1));
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }

                                                                mAdapter.notifyDataSetChanged();
                                                                mAdapter.setLoaded();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();

                                                        }
                                                    }
                                                }, activity, Constant.GET_PRODUCTS_URL, params1, false);
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
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_sort) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getResources().getString(R.string.filter_by));
            builder.setSingleChoiceItems(Constant.filterValues, filterIndex, (dialog, item1) -> {
                filterIndex = item1;
                switch (item1) {
                    case 0:
                        filterBy = Constant.NEW;
                        break;
                    case 1:
                        filterBy = Constant.OLD;
                        break;
                    case 2:
                        filterBy = Constant.HIGH;
                        break;
                    case 3:
                        filterBy = Constant.LOW;
                        break;
                }
                if (item1 != -1)
                    GetData();
                dialog.dismiss();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_sort).setVisible(isSort);
        menu.findItem(R.id.toolbar_search).setVisible(true);
        menu.findItem(R.id.toolbar_cart).setIcon(ApiConfig.buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity));

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = requireArguments().getString("name");
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
    public void onPause() {
        super.onPause();
        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
    }
}