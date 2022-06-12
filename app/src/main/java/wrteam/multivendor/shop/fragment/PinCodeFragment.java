package wrteam.multivendor.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.activity.MainActivity;
import wrteam.multivendor.shop.adapter.PinCodeAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.PinCode;

public class PinCodeFragment extends DialogFragment {
    View root;
    RecyclerView recyclerView;
    ArrayList<PinCode> pinCodes;
    PinCodeAdapter pinCodeAdapter;
    NestedScrollView scrollView;
    TextView tvAlert;
    LinearLayoutManager linearLayoutManager;
    Activity activity;
    Session session;
    int offset = 0;
    boolean isLoadMore = false;
    int total = 0;
    EditText searchView;
    TextView tvSearch, tvPinCode;
    ProgressBar progressBar;
    String from;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_pincode, container, false);

        activity = getActivity();
        session = new Session(activity);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        assert getArguments() != null;
        from = getArguments().getString(Constant.FROM);

        searchView = root.findViewById(R.id.searchView);
        recyclerView = root.findViewById(R.id.recyclerView);
        scrollView = root.findViewById(R.id.scrollView);
        progressBar = root.findViewById(R.id.progressBar);
        tvAlert = root.findViewById(R.id.tvAlert);
        tvSearch = root.findViewById(R.id.tvSearch);
        tvPinCode = root.findViewById(R.id.tvPinCode);

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchView.getText().toString().trim().length() > 0) {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close, 0);
                } else {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (from.equals("home")) {
            tvPinCode.setVisibility(View.VISIBLE);
        } else {
            tvPinCode.setVisibility(View.GONE);
        }

        tvPinCode.setOnClickListener(v -> {
            session.setBoolean(Constant.GET_SELECTED_PINCODE, true);
            session.setData(Constant.GET_SELECTED_PINCODE_ID, "0");
            session.setData(Constant.GET_SELECTED_PINCODE_NAME, activity.getString(R.string.all));
            if (HomeFragment.tvLocation != null) {
                HomeFragment.tvLocation.setText(activity.getString(R.string.all));
            }
            if (CartFragment.tvLocation != null) {
                CartFragment.tvLocation.setText(activity.getString(R.string.all));
            }

            if (from.equals("home")) {
                HomeFragment.refreshListener.onRefresh();
            } else {
                CartFragment.refreshListener.onRefresh();
            }

            MainActivity.pinCodeFragment.dismiss();
        });

        tvSearch.setOnClickListener(v -> GetData(searchView.getText().toString().trim()));


        searchView.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (searchView.getText().toString().trim().length() > 0) {
                    if (event.getRawX() >= (searchView.getRight() - searchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                        searchView.setText("");
                        GetData("");
                    }
                    return true;
                }
            }
            return false;
        });


        GetData("");
        return root;
    }

    void GetData(String search) {
        pinCodes = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_PIN_CODES, Constant.GetVal);
        params.put(Constant.SEARCH, search);
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 20));

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        try {

                            total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));

                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                            Gson g = new Gson();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                PinCode pinCode = g.fromJson(jsonObject1.toString(), PinCode.class);
                                pinCodes.add(pinCode);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (offset == 0) {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            tvAlert.setVisibility(View.GONE);
                            pinCodeAdapter = new PinCodeAdapter(activity, pinCodes, from);
                            pinCodeAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(pinCodeAdapter);
                            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    if (pinCodes.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == pinCodes.size() - 1) {
                                                //bottom of list!
                                                pinCodes.add(null);
                                                pinCodeAdapter.notifyItemInserted(pinCodes.size() - 1);
                                                offset += Constant.LOAD_ITEM_LIMIT + 20;

                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.GET_PIN_CODES, Constant.GetVal);
                                                params1.put(Constant.SEARCH, search);
                                                params1.put(Constant.OFFSET, "" + offset);
                                                params1.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 20));

                                                ApiConfig.RequestToVolley((result1, response1) -> {
                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {
                                                                pinCodes.remove(pinCodes.size() - 1);
                                                                pinCodeAdapter.notifyItemRemoved(pinCodes.size());

                                                                JSONObject object = new JSONObject(response1);
                                                                JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                                                                Gson g = new Gson();
                                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                                                    PinCode pinCode = g.fromJson(jsonObject2.toString(), PinCode.class);
                                                                    pinCodes.add(pinCode);
                                                                }
                                                                pinCodeAdapter.notifyDataSetChanged();
                                                                pinCodeAdapter.setLoaded();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }, activity, Constant.GET_LOCATIONS_URL, params1, false);

                                            }
                                            isLoadMore = true;
                                        }

                                    }
                                }
                            });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        tvAlert.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_LOCATIONS_URL, params, false);
    }
}