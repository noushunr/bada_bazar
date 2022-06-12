package wrteam.multivendor.shop.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.adapter.TransactionAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Transaction;


public class TransactionFragment extends Fragment {
    View root;
    RecyclerView recyclerView;
    ArrayList<Transaction> transactions;
    RelativeLayout tvAlert;
    SwipeRefreshLayout swipeLayout;
    NestedScrollView scrollView;
    TransactionAdapter transactionAdapter;
    int total = 0;
    Activity activity;
    TextView tvAlertTitle, tvAlertSubTitle;
    int offset = 0;
    Session session;
    boolean isLoadMore = false;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_transection, container, false);

        offset = 0;

        activity = getActivity();
        session = new Session(activity);

        setHasOptionsMenu(true);


        scrollView = root.findViewById(R.id.scrollView);
        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        tvAlertTitle = root.findViewById(R.id.tvAlertTitle);
        tvAlertSubTitle = root.findViewById(R.id.tvAlertSubTitle);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        tvAlertTitle.setText(getString(R.string.no_transaction_history_found));
        tvAlertSubTitle.setText(getString(R.string.you_have_not_any_transactional_history_yet));

        if (ApiConfig.isConnected(activity)) {
            getTransactionData();
        }

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeLayout.setOnRefreshListener(() -> {
            swipeLayout.setRefreshing(false);
            offset = 0;
            getTransactionData();
        });


        return root;
    }


    void getTransactionData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        transactions = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_TRANSACTION, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.TYPE, Constant.TYPE_TRANSACTION);
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        session.setData(Constant.TOTAL, String.valueOf(total));

                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                        Gson g = new Gson();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                            if (jsonObject1 != null) {
                                Transaction Transaction = g.fromJson(jsonObject1.toString(), Transaction.class);
                                transactions.add(Transaction);
                            } else {
                                break;
                            }

                        }
                        if (offset == 0) {
                            transactionAdapter = new TransactionAdapter(getContext(), activity, transactions);
                            transactionAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(transactionAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                                    if (transactions.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager1 != null && linearLayoutManager1.findLastCompletelyVisibleItemPosition() == transactions.size() - 1) {
                                                //bottom of list!
                                                transactions.add(null);
                                                transactionAdapter.notifyItemInserted(transactions.size() - 1);
                                                new Handler().postDelayed(() -> {

                                                    offset += Constant.LOAD_ITEM_LIMIT;
                                                    Map<String, String> params1 = new HashMap<>();
                                                    params1.put(Constant.GET_USER_TRANSACTION, Constant.GetVal);
                                                    params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                    params1.put(Constant.TYPE, Constant.TYPE_TRANSACTION);
                                                    params1.put(Constant.OFFSET, "" + offset);
                                                    params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

                                                    ApiConfig.RequestToVolley((result1, response1) -> {

                                                        if (result1) {
                                                            try {
                                                                // System.out.println("====product  " + response);
                                                                JSONObject jsonObject1 = new JSONObject(response1);
                                                                if (!jsonObject1.getBoolean(Constant.ERROR)) {

                                                                    session.setData(Constant.TOTAL, jsonObject1.getString(Constant.TOTAL));

                                                                    transactions.remove(transactions.size() - 1);
                                                                    transactionAdapter.notifyItemRemoved(transactions.size());

                                                                    JSONObject object1 = new JSONObject(response1);
                                                                    JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);

                                                                    Gson g1 = new Gson();


                                                                    for (int i = 0; i < jsonArray1.length(); i++) {
                                                                        JSONObject jsonObject2 = jsonArray1.getJSONObject(i);

                                                                        if (jsonObject2 != null) {
                                                                            Transaction Transaction = g1.fromJson(jsonObject2.toString(), Transaction.class);
                                                                            transactions.add(Transaction);
                                                                        } else {
                                                                            break;
                                                                        }

                                                                    }
                                                                    transactionAdapter.notifyDataSetChanged();
                                                                    transactionAdapter.setLoaded();
                                                                    isLoadMore = false;
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                                mShimmerViewContainer.stopShimmer();
                                                                mShimmerViewContainer.setVisibility(View.GONE);
                                                                recyclerView.setVisibility(View.VISIBLE);
                                                            }
                                                        }
                                                    }, activity, Constant.TRANSACTION_URL, params1, false);

                                                }, 0);
                                                isLoadMore = true;
                                            }

                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        tvAlert.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.TRANSACTION_URL, params, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.transaction_history);
        activity.invalidateOptionsMenu();
        Session.setCount(Constant.UNREAD_TRANSACTION_COUNT, 0, getContext());
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
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
    }

}