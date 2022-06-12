package wrteam.multivendor.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.activity.MainActivity;
import wrteam.multivendor.shop.adapter.AdapterStyle1;
import wrteam.multivendor.shop.adapter.SliderAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.DatabaseHelper;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Favorite;
import wrteam.multivendor.shop.model.PriceVariation;
import wrteam.multivendor.shop.model.Product;
import wrteam.multivendor.shop.model.Slider;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static wrteam.multivendor.shop.helper.ApiConfig.AddOrRemoveFavorite;
import static wrteam.multivendor.shop.helper.ApiConfig.GetSettings;


public class ProductDetailFragment extends Fragment {
    static ArrayList<Slider> sliderArrayList;
    TextView tvPinCode, tvSeller, showDiscount, tvMfg, tvMadeIn, txtProductName, tvQuantity, txtPrice, tvOriginalPrice, txtMeasurement, tvStatus, tvTitleMadeIn, tvTitleMfg;
    WebView webDescription;
    ViewPager viewPager;
    Spinner spinner;
    LinearLayout lytSpinner;
    ImageView imgIndicator;
    LinearLayout mMarkersLayout, lytMfg, lytMadeIn;
    RelativeLayout lytMainPrice, lytQuantity, lytDiscount;
    ScrollView scrollView;
    Session session;
    boolean favorite;
    ImageView imgFav;
    ImageButton imgAdd, imgMinus;
    LinearLayout lytShare, lytSave, lytSimilar;
    int size, count;
    View root;
    int variantPosition;
    String from, id;
    boolean isLogin;
    Product product;
    PriceVariation priceVariation;
    ArrayList<PriceVariation> priceVariationsList;
    DatabaseHelper databaseHelper;
    int position = 0;
    Button btnCart;
    Activity activity;
    RecyclerView recyclerView;
    RelativeLayout relativeLayout;
    TextView tvMore;
    ImageView imgReturnable, imgCancellable;
    TextView tvReturnable, tvCancellable;
    String taxPercentage;
    LottieAnimationView lottieAnimationView;
    ShimmerFrameLayout mShimmerViewContainer;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_product_detail, container, false);

        setHasOptionsMenu(true);
        activity = getActivity();

        Constant.CartValues = new HashMap<>();

        session = new Session(activity);
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN);
        databaseHelper = new DatabaseHelper(activity);

        from = requireArguments().getString(Constant.FROM);


        taxPercentage = "0";

        assert getArguments() != null;
        variantPosition = getArguments().getInt("variantPosition", 0);
        id = getArguments().getString("id");

        if (from.equals("fragment") || from.equals("sub_cate") || from.equals("favorite") || from.equals("search") || from.equals("seller")) {
            position = getArguments().getInt("position");
        }

        tvQuantity = root.findViewById(R.id.tvQuantity);
        scrollView = root.findViewById(R.id.scrollView);
        mMarkersLayout = root.findViewById(R.id.layout_markers);
        sliderArrayList = new ArrayList<>();
        viewPager = root.findViewById(R.id.viewPager);
        txtProductName = root.findViewById(R.id.tvProductName);
        tvOriginalPrice = root.findViewById(R.id.tvOriginalPrice);
        webDescription = root.findViewById(R.id.txtDescription);
        txtPrice = root.findViewById(R.id.tvPrice);
        lytDiscount = root.findViewById(R.id.lytDiscount);
        txtMeasurement = root.findViewById(R.id.tvMeasurement);
        imgFav = root.findViewById(R.id.imgFav);
        lytMainPrice = root.findViewById(R.id.lytMainPrice);
        lytQuantity = root.findViewById(R.id.lytQuantity);
        tvQuantity = root.findViewById(R.id.tvQuantity);
        tvStatus = root.findViewById(R.id.tvStatus);
        imgAdd = root.findViewById(R.id.btnAddQuantity);
        imgMinus = root.findViewById(R.id.btnMinusQuantity);
        spinner = root.findViewById(R.id.spinner);
        lytSpinner = root.findViewById(R.id.lytSpinner);
        imgIndicator = root.findViewById(R.id.imgIndicator);
        showDiscount = root.findViewById(R.id.showDiscount);
        tvSeller = root.findViewById(R.id.tvSeller);
        tvSeller.setPaintFlags(tvSeller.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        lytShare = root.findViewById(R.id.lytShare);
        lytSave = root.findViewById(R.id.lytSave);
        lytSimilar = root.findViewById(R.id.lytSimilar);
        tvMadeIn = root.findViewById(R.id.tvMadeIn);
        tvTitleMadeIn = root.findViewById(R.id.tvTitleMadeIn);
        tvMfg = root.findViewById(R.id.tvMfg);
        tvTitleMfg = root.findViewById(R.id.tvTitleMfg);
        lytMfg = root.findViewById(R.id.lytMfg);
        lytMadeIn = root.findViewById(R.id.lytMadeIn);
        btnCart = root.findViewById(R.id.btnCart);
        recyclerView = root.findViewById(R.id.recyclerView);
        relativeLayout = root.findViewById(R.id.relativeLayout);
        tvMore = root.findViewById(R.id.tvMore);
        tvPinCode = root.findViewById(R.id.tvPinCode);

        tvReturnable = root.findViewById(R.id.tvReturnable);
        tvCancellable = root.findViewById(R.id.tvCancellable);
        imgReturnable = root.findViewById(R.id.imgReturnable);
        imgCancellable = root.findViewById(R.id.imgCancellable);

        lottieAnimationView = root.findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setAnimation("add_to_wish_list.json");

        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        GetProductDetail(id);
        GetSettings(activity);

        tvMore.setOnClickListener(v -> ShowSimilar());

        lytSimilar.setOnClickListener(view -> ShowSimilar());

        btnCart.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new CartFragment()).addToBackStack(null).commit());

        lytShare.setOnClickListener(view -> lytShare.setOnClickListener(view1 -> {
            String message = Constant.MainBaseUrl + "itemdetail/" + product.getSlug();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_via));
            startActivity(shareIntent);
        }));

        tvPinCode.setOnClickListener(v -> OpenBottomDialog(activity));

        lytSave.setOnClickListener(view -> {
            if (isLogin) {
                favorite = product.isIs_favorite();
                if (ApiConfig.isConnected(activity)) {
                    if (favorite) {
                        favorite = false;
                        lottieAnimationView.setVisibility(View.GONE);
                        product.setIs_favorite(false);
                        imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                    } else {
                        favorite = true;
                        product.setIs_favorite(true);
                        lottieAnimationView.setVisibility(View.VISIBLE);
                        lottieAnimationView.playAnimation();
                    }
                    AddOrRemoveFavorite(activity, session, product.getId(), favorite);
                }
            } else {
                favorite = databaseHelper.getFavouriteById(product.getId());
                if (favorite) {
                    favorite = false;
                    lottieAnimationView.setVisibility(View.GONE);
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                } else {
                    favorite = true;
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                }
                databaseHelper.AddOrRemoveFavorite(product.getId(), favorite);
            }
            switch (from) {
                case "fragment":
                case "sub_cate":
                    ProductListFragment.productArrayList.get(position).setIs_favorite(favorite);
                    ProductListFragment.mAdapter.notifyDataSetChanged();
                    break;
                case "favorite":
                    if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                        Favorite favProduct = new Favorite();
                        favProduct.setId(product.getId());
                        favProduct.setProduct_id(product.getId());
                        favProduct.setName(product.getName());
                        favProduct.setSlug(product.getSlug());
                        favProduct.setSubcategory_id(product.getSubcategory_id());
                        favProduct.setImage(product.getImage());
                        favProduct.setStatus(product.getStatus());
                        favProduct.setDate_added(product.getDate_added());
                        favProduct.setCategory_id(product.getCategory_id());
                        favProduct.setIndicator(product.getIndicator());
                        favProduct.setManufacturer(product.getManufacturer());
                        favProduct.setMade_in(product.getMade_in());
                        favProduct.setReturn_status(product.getReturn_status());
                        favProduct.setCancelable_status(product.getCancelable_status());
                        favProduct.setTill_status(product.getTill_status());
                        favProduct.setPriceVariations(product.getPriceVariations());
                        favProduct.setOther_images(product.getOther_images());
                        favProduct.setIs_favorite(true);
                        if (favorite) {
                            FavoriteFragment.favoriteArrayList.add(favProduct);
                        } else {
                            FavoriteFragment.favoriteArrayList.remove(position);
                        }
                        FavoriteFragment.favoriteLoadMoreAdapter.notifyDataSetChanged();
                    } else {
                        if (favorite) {
                            FavoriteFragment.productArrayList.add(product);
                        } else {
                            FavoriteFragment.productArrayList.remove(position);
                        }
                        FavoriteFragment.offlineFavoriteAdapter.notifyDataSetChanged();
                    }
                    break;
                case "search":
                    SearchFragment.productArrayList.get(position).setIs_favorite(favorite);
                    SearchFragment.productAdapter.notifyDataSetChanged();
                    break;
                case "seller":
                    SellerProductsFragment.productArrayList.get(position).setIs_favorite(favorite);
                    SellerProductsFragment.mAdapter.notifyDataSetChanged();
                    break;
            }
        });

        imgMinus.setOnClickListener(view -> {
            if (ApiConfig.isConnected(activity)) {
                Constant.CLICK = true;
                count = Integer.parseInt(tvQuantity.getText().toString());
                if (!(count <= 0)) {
                    count--;
                    tvQuantity.setText("" + count);
                    if (isLogin) {
                        if (Constant.CartValues.containsKey(priceVariationsList.get(variantPosition).getId())) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Constant.CartValues.replace(priceVariationsList.get(variantPosition).getId(), "" + count);
                            } else {
                                Constant.CartValues.remove(priceVariationsList.get(variantPosition).getId());
                                Constant.CartValues.put(priceVariationsList.get(variantPosition).getId(), "" + count);
                            }
                        } else {
                            Constant.CartValues.put(priceVariationsList.get(variantPosition).getId(), "" + count);
                        }

                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                    } else {
                        databaseHelper.AddOrderData(priceVariationsList.get(variantPosition).getId(), priceVariation.getProduct_id(), "" + count);
                    }
                    NotifyData(count);

                }
            }

        });

        imgAdd.setOnClickListener(view -> {
            if (ApiConfig.isConnected(activity)) {
                count = Integer.parseInt(tvQuantity.getText().toString());
                if (!(count >= Float.parseFloat(priceVariationsList.get(variantPosition).getStock()))) {
                    if (count < Integer.parseInt(session.getData(Constant.max_cart_items_count))) {
                        Constant.CLICK = true;
                        count++;
                        tvQuantity.setText("" + count);
                        if (isLogin) {
                            if (Constant.CartValues.containsKey(priceVariationsList.get(variantPosition).getId())) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Constant.CartValues.replace(priceVariationsList.get(variantPosition).getId(), "" + count);
                                } else {
                                    Constant.CartValues.remove(priceVariationsList.get(variantPosition).getId());
                                    Constant.CartValues.put(priceVariationsList.get(variantPosition).getId(), "" + count);
                                }
                            } else {
                                Constant.CartValues.put(priceVariationsList.get(variantPosition).getId(), "" + count);
                            }
                            ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                        } else {
                            databaseHelper.AddOrderData(priceVariationsList.get(variantPosition).getId(), priceVariation.getProduct_id(), "" + count);
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                }
                NotifyData(count);
            }
        });

        return root;
    }


    @SuppressLint("SetTextI18n")
    public void OpenBottomDialog(final Activity activity) {
        try {
            @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_check_pincode, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetTheme);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            EditText edtPinCode = sheetView.findViewById(R.id.edtPinCode);
            Button btnApply = sheetView.findViewById(R.id.btnApply);
            ImageView imgPincodeClose = sheetView.findViewById(R.id.imgPincodeClose);
            mBottomSheetDialog.setCancelable(true);


            imgPincodeClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

            btnApply.setOnClickListener(view -> {
                String pinCode = edtPinCode.getText().toString();

                if (ApiConfig.CheckValidation(pinCode, false, false)) {
                    edtPinCode.requestFocus();
                    edtPinCode.setError(activity.getString(R.string.enter_an_pincode));
                } else if (ApiConfig.isConnected(activity)) {
                    Map<String, String> params = new HashMap<>();
                    params.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
                    params.put(Constant.PRODUCT_ID, product.getId());
                    params.put(Constant.PINCODE, pinCode);

                    ApiConfig.RequestToVolley((result, response) -> {
                        if (result) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean(Constant.ERROR)) {
                                    tvPinCode.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
                                    tvPinCode.setText(activity.getString(R.string.deliverable_to) + pinCode);
                                } else {
                                    tvPinCode.setTextColor(ContextCompat.getColor(activity, R.color.red));
                                    tvPinCode.setText(activity.getString(R.string.can_not_deliverable_to) + pinCode);
                                }
                                mBottomSheetDialog.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                e.printStackTrace();
                            }
                        }
                    }, activity, Constant.GET_PRODUCTS_URL, params, false);
                }
            });

            mBottomSheetDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ShowSimilar() {
        Fragment fragment = new ProductListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", product.getId());
        bundle.putString("cat_id", product.getCategory_id());
        bundle.putString(Constant.FROM, "similar");
        bundle.putString("name", "Similar Products");
        fragment.setArguments(bundle);
        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
    }


    void GetSimilarData(Product product) {
        ArrayList<Product> productArrayList = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
        params.put(Constant.PRODUCT_ID, product.getId());
        params.put(Constant.CATEGORY_ID, product.getCategory_id());
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                        try {
                            productArrayList.addAll(ApiConfig.GetProductList(jsonArray));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AdapterStyle1 adapter = new AdapterStyle1(getContext(), activity, productArrayList, R.layout.offer_layout);
                        recyclerView.setAdapter(adapter);
                        relativeLayout.setVisibility(View.VISIBLE);
                    } else {
                        relativeLayout.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                }
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }

    public void NotifyData(int count) {
        switch (from) {
            case "fragment":
                ProductListFragment.productArrayList.get(position).getPriceVariations().get(variantPosition).setQty(count);
                ProductListFragment.mAdapter.notifyItemChanged(position, ProductListFragment.productArrayList.get(position));
                if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                    ApiConfig.getCartItemCount(activity, session);
                } else {
                    databaseHelper.getTotalItemOfCart(activity);
                }
                activity.invalidateOptionsMenu();
                break;
            case "seller":
                SellerProductsFragment.productArrayList.get(position).getPriceVariations().get(variantPosition).setQty(count);
                SellerProductsFragment.mAdapter.notifyItemChanged(position, ProductListFragment.productArrayList.get(position));
                if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                    ApiConfig.getCartItemCount(activity, session);
                } else {
                    databaseHelper.getTotalItemOfCart(activity);
                }
                activity.invalidateOptionsMenu();
                break;
            case "favorite":
                if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                    FavoriteFragment.favoriteArrayList.get(position).getPriceVariations().get(variantPosition).setQty(count);
                    FavoriteFragment.favoriteLoadMoreAdapter.notifyItemChanged(position, FavoriteFragment.favoriteArrayList.get(position));
                } else {
                    FavoriteFragment.productArrayList.get(position).getPriceVariations().get(variantPosition).setQty(count);
                    FavoriteFragment.offlineFavoriteAdapter.notifyItemChanged(position, FavoriteFragment.productArrayList.get(position));
                    databaseHelper.getTotalItemOfCart(activity);
                }
                activity.invalidateOptionsMenu();
                break;
            case "search":
                SearchFragment.productArrayList.get(position).getPriceVariations().get(variantPosition).setQty(count);
                SearchFragment.productAdapter.notifyItemChanged(position, SearchFragment.productArrayList.get(position));
                if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                    databaseHelper.getTotalItemOfCart(activity);
                }
                activity.invalidateOptionsMenu();
                break;
            case "section":
            case "share":
                if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                    databaseHelper.getTotalItemOfCart(activity);
                } else {
                    ApiConfig.getCartItemCount(activity, session);
                }
                activity.invalidateOptionsMenu();
                break;
        }
    }

    void GetProductDetail(final String productId) {
        scrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }
        if (from.equals("share")) {
            params.put(Constant.SLUG, productId);
        } else {
            params.put(Constant.PRODUCT_ID, productId);
        }
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params.put(Constant.USER_ID, session.getData(Constant.ID));
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
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
                                    product = new Product(jsonObject.getString(Constant.SELLER_NAME), jsonObject.getString(Constant.RETURN_DAYS), jsonObject.getString(Constant.IS_APPROVED), jsonObject.getString(Constant.SELLER_ID), jsonObject.getString(Constant.TAX_ID), jsonObject.getString(Constant.TAX_PERCENT), jsonObject.getString(Constant.ROW_ORDER), jsonObject.getString(Constant.TILL_STATUS), jsonObject.getString(Constant.CANCELLABLE_STATUS), jsonObject.getString(Constant.MANUFACTURER), jsonObject.getString(Constant.MADE_IN), jsonObject.getString(Constant.RETURN_STATUS), jsonObject.getString(Constant.ID), jsonObject.getString(Constant.NAME), jsonObject.getString(Constant.SLUG), jsonObject.getString(Constant.SUC_CATE_ID), jsonObject.getString(Constant.IMAGE), jsonObject.getJSONArray(Constant.OTHER_IMAGES), jsonObject.getString(Constant.DESCRIPTION), jsonObject.getString(Constant.STATUS), jsonObject.getString(Constant.DATE_ADDED), jsonObject.getBoolean(Constant.IS_FAVORITE), jsonObject.getString(Constant.CATEGORY_ID), priceVariations, jsonObject.getString(Constant.INDICATOR));
                                } catch (JSONException e) {
                        e.printStackTrace();
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        priceVariationsList = product.getPriceVariations();

                        SetProductDetails(product);
                        GetSimilarData(product);

                    }
                    scrollView.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmer();
                } catch (JSONException e) {
                        e.printStackTrace();
                    scrollView.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmer();
                }
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }


    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    void SetProductDetails(final Product product) {
        try {

            txtProductName.setText(product.getName());
            try {
                taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }

            sliderArrayList = new ArrayList<>();

            JSONArray jsonArray = product.getOther_images();
            size = jsonArray.length();

            sliderArrayList.add(new Slider(product.getImage()));

            if (product.getMade_in().length() > 0) {
                lytMadeIn.setVisibility(View.VISIBLE);
                tvMadeIn.setText(product.getMade_in());
            }

            if (product.getManufacturer().length() > 0) {
                lytMfg.setVisibility(View.VISIBLE);
                tvMfg.setText(product.getManufacturer());
            }

            tvSeller.setText(product.getSeller_name());

            if (session.getBoolean(Constant.GET_SELECTED_PINCODE)) {
                if (!session.getData(Constant.GET_SELECTED_PINCODE_NAME).equals(activity.getString(R.string.all))) {
                    tvPinCode.setText(activity.getString(R.string.deliverable_to) + session.getData(Constant.GET_SELECTED_PINCODE_NAME));
                }
            }

            tvSeller.setOnClickListener(v -> {
                Fragment fragment = new SellerProductsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.ID, product.getSeller_id());
                bundle.putString(Constant.TITLE, product.getSeller_name());
                bundle.putString(Constant.FROM, from);
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });

            if (isLogin) {
                if (product.isIs_favorite()) {
                    favorite = true;
                    imgFav.setImageResource(R.drawable.ic_is_favorite);
                } else {
                    favorite = false;
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                }
            } else {
                if (databaseHelper.getFavouriteById(product.getId())) {
                    imgFav.setImageResource(R.drawable.ic_is_favorite);
                } else {
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                }
            }

            if (isLogin) {
                if (Constant.CartValues.containsKey(product.getPriceVariations().get(0).getId())) {
                    tvQuantity.setText("" + Constant.CartValues.get(product.getPriceVariations().get(0).getId()));
                } else {
                    tvQuantity.setText(product.getPriceVariations().get(0).getCart_count());
                }
            } else {
                tvQuantity.setText(databaseHelper.CheckOrderExists(product.getPriceVariations().get(0).getId(), product.getPriceVariations().get(0).getProduct_id()));
            }

            if (product.getReturn_status().equalsIgnoreCase("1")) {
                imgReturnable.setImageDrawable(getResources().getDrawable(R.drawable.ic_returnable));
                tvReturnable.setText(product.getReturn_days() + getString(R.string.days) + getString(R.string.returnable));
            } else {
                imgReturnable.setImageDrawable(getResources().getDrawable(R.drawable.ic_not_returnable));
                tvReturnable.setText(getString(R.string.not_returnable));
            }

            if (product.getCancelable_status().equalsIgnoreCase("1")) {
                imgCancellable.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancellable));
                tvCancellable.setText(getString(R.string.cancellable_till) + ApiConfig.toTitleCase(product.getTill_status()));
            } else {
                imgCancellable.setImageDrawable(getResources().getDrawable(R.drawable.ic_not_cancellable));
                tvCancellable.setText(getString(R.string.not_cancellable));
            }


            for (int i = 0; i < jsonArray.length(); i++) {
                sliderArrayList.add(new Slider(jsonArray.getString(i)));
            }

            viewPager.setAdapter(new SliderAdapter(sliderArrayList, activity, R.layout.lyt_detail_slider, "detail"));
            ApiConfig.addMarkers(0, sliderArrayList, mMarkersLayout, getContext());


            if (priceVariationsList.size() == 1) {
                spinner.setVisibility(View.INVISIBLE);
                lytSpinner.setVisibility(View.INVISIBLE);
                lytMainPrice.setEnabled(false);
                priceVariation = priceVariationsList.get(0);
                session.setData(Constant.PRODUCT_VARIANT_ID, "" + 0);
                SetSelectedData();
            }

            if (!product.getIndicator().equals("0")) {
                imgIndicator.setVisibility(View.VISIBLE);
                if (product.getIndicator().equals("1"))
                    imgIndicator.setImageResource(R.drawable.ic_veg_icon);
                else if (product.getIndicator().equals("2"))
                    imgIndicator.setImageResource(R.drawable.ic_non_veg_icon);
            }
            CustomAdapter customAdapter = new CustomAdapter();
            spinner.setAdapter(customAdapter);

            webDescription.setVerticalScrollBarEnabled(true);
            webDescription.loadDataWithBaseURL("", product.getDescription(), "text/html", "UTF-8", "");
            webDescription.setBackgroundColor(getResources().getColor(R.color.white));
            txtProductName.setText(product.getName());

            spinner.setSelection(variantPosition);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {
                }

                @Override
                public void onPageSelected(int position) {
                    ApiConfig.addMarkers(position, sliderArrayList, mMarkersLayout, getContext());
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                }
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    priceVariation = product.getPriceVariations().get(i);
                    variantPosition = i;
                    session.setData(Constant.PRODUCT_VARIANT_ID, "" + i);
                    SetSelectedData();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            scrollView.setVisibility(View.VISIBLE);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.app_name);
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


    @SuppressLint("SetTextI18n")
    public void SetSelectedData() {
        txtMeasurement.setText(" ( " + priceVariation.getMeasurement() + priceVariation.getMeasurement_unit_name() + " ) ");
        tvStatus.setText(activity.getString(R.string.sold_out));

        double price, oPrice;
        String taxPercentage = "0";
        try {
            taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (priceVariation.getDiscounted_price().equals("0") || priceVariation.getDiscounted_price().equals("")) {
            lytDiscount.setVisibility(View.INVISIBLE);
            tvOriginalPrice.setVisibility(View.GONE);
            price = ((Float.parseFloat(priceVariation.getPrice()) + ((Float.parseFloat(priceVariation.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
        } else {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            price = ((Float.parseFloat(priceVariation.getDiscounted_price()) + ((Float.parseFloat(priceVariation.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
            oPrice = (Float.parseFloat(priceVariation.getPrice()) + ((Float.parseFloat(priceVariation.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));

            lytDiscount.setVisibility(View.VISIBLE);
            showDiscount.setText(priceVariation.getDiscountpercent().replace("(", "").replace(")", ""));
        }
        txtPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));


        if (isLogin) {
//            System.out.println("priceVariation.getId()) : " + Constant.CartValues);
            if (Constant.CartValues.containsKey(priceVariation.getId())) {
                tvQuantity.setText(Constant.CartValues.get(priceVariation.getId()));
            } else {
                tvQuantity.setText(priceVariation.getCart_count());
            }
        } else {
            tvQuantity.setText(databaseHelper.CheckOrderExists(priceVariation.getId(), priceVariation.getProduct_id()));
        }

        if (priceVariation.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
            tvStatus.setVisibility(View.VISIBLE);
            lytQuantity.setVisibility(View.GONE);
        } else {
            tvStatus.setVisibility(View.GONE);
            lytQuantity.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
        menu.findItem(R.id.toolbar_cart).setIcon(ApiConfig.buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity));
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
    }

    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return product.getPriceVariations().size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint({"ViewHolder", "SetTextI18n", "InflateParams"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.tvMeasurement);
//            TextView price = view.findViewById(R.id.tvPrice);

            PriceVariation priceVariation = product.getPriceVariations().get(i);
            measurement.setText(priceVariation.getMeasurement() + " " + priceVariation.getMeasurement_unit_name());
//            price.setText(session.getData(Constant.CURRENCY) + priceVariation.getPrice());

            if (priceVariation.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
                measurement.setTextColor(getResources().getColor(R.color.red));
//                price.setTextColor(getResources().getColor(R.color.red));
            } else {
                measurement.setTextColor(getResources().getColor(R.color.black));
//                price.setTextColor(getResources().getColor(R.color.black));
            }

            return view;
        }
    }
}
