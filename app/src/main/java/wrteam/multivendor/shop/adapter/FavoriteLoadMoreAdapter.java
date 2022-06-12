package wrteam.multivendor.shop.adapter;

import static wrteam.multivendor.shop.fragment.FavoriteFragment.recyclerView;
import static wrteam.multivendor.shop.fragment.FavoriteFragment.tvAlert;
import static wrteam.multivendor.shop.helper.ApiConfig.AddOrRemoveFavorite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.fragment.FavoriteFragment;
import wrteam.multivendor.shop.fragment.ProductDetailFragment;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.DatabaseHelper;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Favorite;
import wrteam.multivendor.shop.model.PriceVariation;
import wrteam.multivendor.shop.model.Product;

public class FavoriteLoadMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    public final int resource;
    public final ArrayList<Favorite> mDataset;
    final Context context;
    final Activity activity;
    final Session session;
    final boolean isLogin;
    final DatabaseHelper databaseHelper;


    public FavoriteLoadMoreAdapter(Context context, ArrayList<Favorite> myDataset, int resource) {
        this.context = context;
        this.activity = (Activity) context;
        this.mDataset = myDataset;
        this.resource = resource;
        this.session = new Session(activity);
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN);
        Constant.CartValues = new HashMap<>();
        databaseHelper = new DatabaseHelper(activity);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view;
        switch (viewType){
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(resource, parent, false);
                return new HolderItems(view);
            case (VIEW_TYPE_LOADING):
                view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                return new ViewHolderLoading(view);
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderParent, final int position) {

        if (holderParent instanceof HolderItems) {
            try {
                final HolderItems holder = (HolderItems) holderParent;
                holder.setIsRecyclable(false);
                final Favorite product = mDataset.get(position);

                final ArrayList<PriceVariation> priceVariations = product.getPriceVariations();
                if (priceVariations.size() == 1) {
                    holder.spinner.setVisibility(View.INVISIBLE);
                    holder.lytSpinner.setVisibility(View.INVISIBLE);
                }
                if (!product.getIndicator().equals("0")) {
                    holder.imgIndicator.setVisibility(View.VISIBLE);
                    if (product.getIndicator().equals("1"))
                        holder.imgIndicator.setImageResource(R.drawable.ic_veg_icon);
                    else if (product.getIndicator().equals("2"))
                        holder.imgIndicator.setImageResource(R.drawable.ic_non_veg_icon);
                }

                holder.productName.setText(Html.fromHtml(product.getName()));

                Picasso.get()
                        .load(product.getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.imgThumb);

                CustomAdapter customAdapter = new CustomAdapter(context, priceVariations, holder, product);
                holder.spinner.setAdapter(customAdapter);

                holder.lytMain.setOnClickListener(v -> {

                    if (Constant.CartValues.size() > 0) {
                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                    }

                    AppCompatActivity activity1 = (AppCompatActivity) context;
                    Fragment fragment = new ProductDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("variantPosition", priceVariations.size() == 1 ? 0 : holder.spinner.getSelectedItemPosition());
                    bundle.putString("id", product.getProduct_id());
                    bundle.putInt("position", position);
                    bundle.putString(Constant.FROM, "favorite");
                    fragment.setArguments(bundle);
                    activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                });


                if (isLogin) {

                    holder.tvQuantity.setText(priceVariations.get(0).getCart_count());

                    if (product.isIs_favorite()) {
                        holder.imgFav.setImageResource(R.drawable.ic_is_favorite);
                    } else {
                        holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                    }
                    final Session session = new Session(activity);

                    holder.imgFav.setImageResource(R.drawable.ic_is_favorite);

                    holder.imgFav.setOnClickListener(v -> {
                        if (ApiConfig.isConnected(activity)) {
                            mDataset.remove(product);
                            FavoriteFragment.favoriteLoadMoreAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(FavoriteFragment.favoriteLoadMoreAdapter);
                            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                                AddOrRemoveFavorite(activity, session, product.getProduct_id(), false);
                            } else {
                                databaseHelper.AddOrRemoveFavorite(product.getId(), false);
                            }
                            if (mDataset.size() == 0) {
                                tvAlert.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        }
                    });

                } else {

                    holder.tvQuantity.setText(databaseHelper.CheckOrderExists(product.getPriceVariations().get(0).getId(), product.getId()));

                    holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite);

                    holder.imgFav.setOnClickListener(v -> databaseHelper.AddOrRemoveFavorite(product.getId(), false));
                }
                SetSelectedData(holder, priceVariations.get(0), product);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        Favorite product = mDataset.get(position);
        if (product != null)
            return Integer.parseInt(product.getId());
        else
            return position;
    }

    public void setLoaded() {
    }

    @SuppressLint("SetTextI18n")
    public void SetSelectedData(final HolderItems holder, final PriceVariation extra, Product product) {

        holder.Measurement.setText(extra.getMeasurement() + extra.getMeasurement_unit_name());
        holder.productPrice.setText(session.getData(Constant.CURRENCY) + extra.getPrice());

        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (Constant.CartValues.containsKey(extra.getId())) {
                holder.tvQuantity.setText("" + Constant.CartValues.get(extra.getId()));
            } else {
                holder.tvQuantity.setText(extra.getCart_count());
            }
        } else {
            holder.tvQuantity.setText(databaseHelper.CheckOrderExists(extra.getId(), extra.getProduct_id()));
        }

        holder.tvStatus.setText(activity.getString(R.string.sold_out));

        double price, oPrice;
        String taxPercentage = "0";
        try {
            taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (extra.getDiscounted_price().equals("0") || extra.getDiscounted_price().equals("")) {
            holder.lytDiscount.setVisibility(View.INVISIBLE);
            price = ((Float.parseFloat(extra.getPrice()) + ((Float.parseFloat(extra.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
        } else {
            price = ((Float.parseFloat(extra.getDiscounted_price()) + ((Float.parseFloat(extra.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
            oPrice = (Float.parseFloat(extra.getPrice()) + ((Float.parseFloat(extra.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

            holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.originalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));

            holder.lytDiscount.setVisibility(View.VISIBLE);
            holder.showDiscount.setText(extra.getDiscountpercent().replace("(", "").replace(")", ""));
        }
        holder.productPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

        if (extra.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setTextColor(Color.RED);
            holder.qtyLyt.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setVisibility(View.GONE);
            holder.qtyLyt.setVisibility(View.VISIBLE);
        }

        if (isLogin) {

            holder.tvQuantity.setText(extra.getCart_count());
            holder.imgAdd.setOnClickListener(view -> {
                int count = Integer.parseInt(holder.tvQuantity.getText().toString());
                if (count < Float.parseFloat(extra.getStock())) {
                    if (count < Integer.parseInt(session.getData(Constant.max_cart_items_count))) {
                        count++;
                        holder.tvQuantity.setText("" + count);
                        if (Constant.CartValues.containsKey(extra.getId())) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Constant.CartValues.replace(extra.getId(), "" + count);
                            }
                        } else {
                            Constant.CartValues.put(extra.getId(), "" + count);
                        }
                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, activity.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                }
            });

            holder.imgMinus.setOnClickListener(view -> {
                int count = Integer.parseInt(holder.tvQuantity.getText().toString());
                if (!(count <= 0)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        count--;
                        holder.tvQuantity.setText("" + count);
                        if (Constant.CartValues.containsKey(extra.getId())) {
                            Constant.CartValues.replace(extra.getId(), "" + count);
                        } else {
                            Constant.CartValues.put(extra.getId(), "" + count);
                        }
                    }
                    ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                }
            });
        } else {


            holder.tvQuantity.setText(databaseHelper.CheckOrderExists(extra.getId(), extra.getProduct_id()));

            holder.imgAdd.setOnClickListener(view -> {
                int count = Integer.parseInt(holder.tvQuantity.getText().toString());
                if (count < Float.parseFloat(extra.getStock())) {
                    if (count < Integer.parseInt(session.getData(Constant.max_cart_items_count))) {
                        count++;
                        holder.tvQuantity.setText("" + count);
                        databaseHelper.AddOrderData(extra.getId(), extra.getProduct_id(), "" + count);
                        databaseHelper.getTotalItemOfCart(activity);
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, activity.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                }
            });

            holder.imgMinus.setOnClickListener(view -> {
                int count = Integer.parseInt(holder.tvQuantity.getText().toString());
                if (!(count <= 0)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        count--;
                        holder.tvQuantity.setText("" + count);
                        databaseHelper.AddOrderData(extra.getId(), extra.getProduct_id(), "" + count);
                        databaseHelper.getTotalItemOfCart(activity);
                    }
                }
            });
        }

    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = view.findViewById(R.id.itemProgressbar);
        }
    }

    public static class HolderItems extends RecyclerView.ViewHolder {
        public final ImageButton imgAdd;
        public final ImageButton imgMinus;
        final TextView productName;
        final TextView productPrice;
        final TextView tvQuantity;
        final TextView Measurement;
        final TextView showDiscount;
        final TextView originalPrice;
        final TextView tvStatus;
        final ImageView imgThumb;
        final ImageView imgFav;
        final ImageView imgIndicator;
        final CardView lytMain;
        final RelativeLayout lytDiscount, lytSpinner;
        final AppCompatSpinner spinner;
        final RelativeLayout qtyLyt;

        public HolderItems(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.tvPrice);
            showDiscount = itemView.findViewById(R.id.showDiscount);
            originalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            Measurement = itemView.findViewById(R.id.tvMeasurement);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            imgIndicator = itemView.findViewById(R.id.imgIndicator);
            imgAdd = itemView.findViewById(R.id.btnAddQuantity);
            imgMinus = itemView.findViewById(R.id.btnMinusQuantity);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            qtyLyt = itemView.findViewById(R.id.qtyLyt);
            imgFav = itemView.findViewById(R.id.imgFav);
            lytMain = itemView.findViewById(R.id.lytMain);
            spinner = itemView.findViewById(R.id.spinner);
            lytDiscount = itemView.findViewById(R.id.lytDiscount);
            lytSpinner = itemView.findViewById(R.id.lytSpinner);

        }

    }

    public class CustomAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<PriceVariation> extraList;
        final LayoutInflater inflter;
        final HolderItems holder;
        final Favorite product;

        public CustomAdapter(Context applicationContext, ArrayList<PriceVariation> extraList, HolderItems holder, Favorite product) {
            this.context = applicationContext;
            this.extraList = extraList;
            this.holder = holder;
            this.product = product;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return extraList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.tvMeasurement);
//            TextView price = view.findViewById(R.id.tvPrice);


            PriceVariation extra = extraList.get(i);
            measurement.setText(extra.getMeasurement() + " " + extra.getMeasurement_unit_name());
//            price.setText(session.getData(Constant.CURRENCY) + extra.getPrice());

            if (extra.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
                measurement.setTextColor(context.getResources().getColor(R.color.red));
//                price.setTextColor(context.getResources().getColor(R.color.red));
            } else {
                measurement.setTextColor(context.getResources().getColor(R.color.black));
//                price.setTextColor(context.getResources().getColor(R.color.black));
            }

            holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    PriceVariation priceVariation = extraList.get(i);
                    SetSelectedData(holder, priceVariation, product);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            return view;
        }
    }

}

