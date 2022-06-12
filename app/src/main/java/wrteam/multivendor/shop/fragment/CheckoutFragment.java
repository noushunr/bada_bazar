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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import wrteam.multivendor.shop.activity.MainActivity;
import wrteam.multivendor.shop.adapter.CheckoutItemListAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Cart;

import static android.content.Context.INPUT_METHOD_SERVICE;

@SuppressWarnings("ALL")
public class CheckoutFragment extends Fragment {
    public static String pCode = "", appliedCode = "", deliveryCharge = "0";
    public static double pCodeDiscount = 0.0, subtotal = 0.0, dCharge = 0.0; //, total = 0.0; //taxAmt = 0.0,
    public TextView tvConfirmOrder, tvPayment, tvDelivery;
    public ArrayList<String> variantIdList, qtyList;
    public TextView tvSaveAmount, tvAlert, tvTotalBeforeTax, tvDeliveryCharge, tvSubTotal, tvTotalItems;
    public LinearLayout processLyt;
    CardView lytSaveAmount;
    RecyclerView recyclerView;
    View root;
    RelativeLayout confirmLyt;
    boolean isApplied;
    ImageView imgRefresh;
    Button btnApply;
    EditText edtPromoCode;
    Session session;
    Activity activity;
    CheckoutItemListAdapter checkoutItemListAdapter;
    ArrayList<Cart> carts;
    float OriginalAmount = 0, DiscountedAmount = 0;
    private ShimmerFrameLayout mShimmerViewContainer;
    public static boolean OrderPlacable = false;
//    double totalWithoutTax = 0, totalTaxAmount = 0, totalTaxPercentage = 0;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_checkout, container, false);

        activity = getActivity();
        session = new Session(activity);
        tvDelivery = root.findViewById(R.id.tvSummary);
        tvPayment = root.findViewById(R.id.tvPayment);
        tvAlert = root.findViewById(R.id.tvAlert);
        edtPromoCode = root.findViewById(R.id.edtPromoCode);
        tvSubTotal = root.findViewById(R.id.tvSubTotal);
        tvTotalItems = root.findViewById(R.id.tvTotalItems);
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge);
        confirmLyt = root.findViewById(R.id.confirmLyt);
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder);
        processLyt = root.findViewById(R.id.processLyt);
        imgRefresh = root.findViewById(R.id.imgRefresh);
        tvTotalBeforeTax = root.findViewById(R.id.tvTotalBeforeTax);
        tvSaveAmount = root.findViewById(R.id.tvSaveAmount);
        lytSaveAmount = root.findViewById(R.id.lytSaveAmount);
        btnApply = root.findViewById(R.id.btnApply);
        recyclerView = root.findViewById(R.id.recyclerView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        carts = new ArrayList<>();

        OrderPlacable = false;

        setHasOptionsMenu(true);
        tvTotalItems.setText(Constant.TOTAL_CART_ITEM + " Items");

        Constant.FLOAT_TOTAL_AMOUNT = 0;

        tvConfirmOrder.setOnClickListener(view -> {
            if (subtotal != 0 && Constant.FLOAT_TOTAL_AMOUNT != 0) {
                if (!OrderPlacable) {
                    Fragment fragment = new PaymentFragment();
                    Bundle bundle = new Bundle();

                    if (subtotal > Constant.SETTING_MINIMUM_AMOUNT_FOR_FREE_DELIVERY) {
                        Constant.SETTING_DELIVERY_CHARGE = 0.0;
                    }

                    dCharge = tvDeliveryCharge.getText().toString().equals(getString(R.string.free)) ? 0.0 : Constant.SETTING_DELIVERY_CHARGE;
                    bundle.putDouble("subtotal", Double.parseDouble("" + (subtotal + dCharge)));
                    bundle.putDouble("total", Double.parseDouble("" + Constant.FLOAT_TOTAL_AMOUNT));
                    bundle.putDouble("pCodeDiscount", Double.parseDouble("" + pCodeDiscount));
                    bundle.putString("pCode", pCode);
                    bundle.putStringArrayList("variantIdList", variantIdList);
                    bundle.putStringArrayList("qtyList", qtyList);
//                        bundle.putString("totalTaxAmount", "" + totalTaxAmount);
//                        bundle.putString("totalTaxPercentage", "" + totalTaxPercentage);
                    bundle.putString(Constant.FROM, "process");
                    PaymentFragment.paymentMethod = "";
                    PaymentFragment.deliveryTime = "";
                    PaymentFragment.deliveryDay = "";
                    fragment.setArguments(bundle);
                    MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                } else {
                    Toast.makeText(activity, activity.getString(R.string.msg_order_can_not_placed), Toast.LENGTH_SHORT).show();
                }
            }
        });

        imgRefresh.setOnClickListener(view -> {
            if (isApplied) {
                btnApply.setEnabled(true);
                btnApply.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent));
                btnApply.setText("Apply");
                edtPromoCode.setText("");
                isApplied = false;
                appliedCode = "";
                pCode = "";
                pCodeDiscount = 0;
                subtotal = Constant.FLOAT_TOTAL_AMOUNT;
                SetDataTotal();
            }
        });


        btnApply.setOnClickListener(view -> {
            final String promoCode = edtPromoCode.getText().toString().trim();
            if (promoCode.isEmpty()) {
                tvAlert.setVisibility(View.VISIBLE);
                tvAlert.setText("Enter Promo Code");
            } else if (isApplied && promoCode.equals(appliedCode)) {
                Toast.makeText(getContext(), "promo code already applied", Toast.LENGTH_SHORT).show();
            } else {
                tvAlert.setVisibility(View.GONE);
                btnApply.setVisibility(View.INVISIBLE);
                Map<String, String> params = new HashMap<>();
                params.put(Constant.VALIDATE_PROMO_CODE, Constant.GetVal);
                params.put(Constant.USER_ID, session.getData(Constant.ID));
                params.put(Constant.PROMO_CODE, promoCode);
                params.put(Constant.TOTAL, String.valueOf((Constant.FLOAT_TOTAL_AMOUNT))); // taxAmt +

                ApiConfig.RequestToVolley((result, response) -> {
                    if (result) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean(Constant.ERROR)) {
                                pCode = object.getString(Constant.PROMO_CODE);
                                btnApply.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.light_green));
                                btnApply.setText("Applied");
                                btnApply.setEnabled(false);
                                isApplied = true;
                                appliedCode = edtPromoCode.getText().toString();
                                dCharge = tvDeliveryCharge.getText().toString().equals(getString(R.string.free)) ? 0.0 : Constant.SETTING_DELIVERY_CHARGE;
                                subtotal = (object.getDouble(Constant.DISCOUNTED_AMOUNT));
                                pCodeDiscount = Double.parseDouble(object.getString(Constant.DISCOUNT));
                                tvTotalBeforeTax.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subtotal));
                                tvSubTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (subtotal + dCharge)));
                                tvSaveAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + ((OriginalAmount - DiscountedAmount) + pCodeDiscount)));
                            } else {
                                btnApply.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent));
                                btnApply.setText("Apply");
                                btnApply.setEnabled(true);
                                tvAlert.setVisibility(View.VISIBLE);
                                tvAlert.setText(object.getString("message"));
                            }
                            btnApply.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            e.printStackTrace();
                        }
                    }
                }, activity, Constant.PROMO_CODE_CHECK_URL, params, true);

            }
        });

        if (ApiConfig.isConnected(activity)) {
            getCartData();
        }

        return root;
    }


    void getCartData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();

        ApiConfig.getCartItemCount(activity, session);
        subtotal = 0;
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.ADDRESS_ID, Constant.selectedAddressId);
        params.put(Constant.LIMIT, "" + Constant.TOTAL_CART_ITEM);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                    Gson gson = new Gson();
                    variantIdList = new ArrayList<>();
                    qtyList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            Cart cart = gson.fromJson(String.valueOf(jsonArray.getJSONObject(i)), Cart.class);
                            variantIdList.add(cart.getProduct_variant_id());
                            qtyList.add(cart.getQty());
                            float price;
                            int qty = Integer.parseInt(cart.getQty());
                            String taxPercentage = cart.getItems().get(0).getTax_percentage();

                            if (cart.getItems().get(0).getDiscounted_price().equals("0") || cart.getItems().get(0).getDiscounted_price().equals("")) {
                                price = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));

                            } else {
                                OriginalAmount += (Float.parseFloat(cart.getItems().get(0).getPrice()) * qty);
                                DiscountedAmount += (Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * qty);

                                price = ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                            }

                            Constant.FLOAT_TOTAL_AMOUNT += (price * qty);
                            subtotal = Constant.FLOAT_TOTAL_AMOUNT;
                            carts.add(cart);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    checkoutItemListAdapter = new CheckoutItemListAdapter(getContext(), getActivity(), carts);
                    recyclerView.setAdapter(checkoutItemListAdapter);
                    SetDataTotal();

                    confirmLyt.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                        e.printStackTrace();

                    confirmLyt.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                }
            }
        }, activity, Constant.CART_URL, params, false);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void SetDataTotal() {
        try {
            if ((OriginalAmount - DiscountedAmount) != 0) {
                lytSaveAmount.setVisibility(View.VISIBLE);
                if (pCodeDiscount != 0) {
                    tvSaveAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + ((OriginalAmount - DiscountedAmount) + pCodeDiscount)));
                } else {
                    tvSaveAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + ((OriginalAmount - DiscountedAmount) - pCodeDiscount)));
                }
            } else {
                if (pCodeDiscount == 0) {
                    lytSaveAmount.setVisibility(View.GONE);
                }
            }
            if (Constant.FLOAT_TOTAL_AMOUNT <= Constant.SETTING_MINIMUM_AMOUNT_FOR_FREE_DELIVERY) {
                tvDeliveryCharge.setText(session.getData(Constant.CURRENCY) + Constant.SETTING_DELIVERY_CHARGE);
                deliveryCharge = "" + Constant.SETTING_DELIVERY_CHARGE;
            } else {
                tvDeliveryCharge.setText(getResources().getString(R.string.free));
                deliveryCharge = "0";
            }
            dCharge = tvDeliveryCharge.getText().toString().equals(getString(R.string.free)) ? 0.0 : Constant.SETTING_DELIVERY_CHARGE;
            if (!pCode.isEmpty()) {
                subtotal = subtotal - pCodeDiscount;
            }
            tvTotalBeforeTax.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subtotal));
            tvSubTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (subtotal + dCharge)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.checkout);
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
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        activity.invalidateOptionsMenu();
    }

}
