package wrteam.multivendor.shop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.fragment.CartFragment;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.DatabaseHelper;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.OfflineCart;

@SuppressLint("NotifyDataSetChanged")
public class OfflineCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final ArrayList<OfflineCart> items;
    final DatabaseHelper databaseHelper;
    final Session session;
    final Context context;
    public boolean isLoading;


    public OfflineCartAdapter(Context context, Activity activity, ArrayList<OfflineCart> items) {
        this.activity = activity;
        this.context = context;
        this.items = items;
        databaseHelper = new DatabaseHelper(activity);
        session = new Session(context);
    }

    public void add(int position, OfflineCart item) {
        if (position != (getItemCount() + 1)) {
            items.add(position, item);
        } else {
            items.add(item);
        }
        notifyItemInserted(position);
    }

    public void removeItem(int position) {

        OfflineCart cart = items.get(position);

        databaseHelper.AddOrderData(items.get(position).getId(), cart.getProduct_id(), "0");
        items.remove(cart);
        showUndoSnackBar(cart, position);
        notifyDataSetChanged();
        CartFragment.SetData();
        Constant.FLOAT_TOTAL_AMOUNT = 0.00;
        databaseHelper.getTotalItemOfCart(activity);
        activity.invalidateOptionsMenu();
        if (getItemCount() == 0) {
            CartFragment.lytEmpty.setVisibility(View.VISIBLE);
            CartFragment.lytTotal.setVisibility(View.GONE);
        }
    }

    public void setLoaded() {
        isLoading = false;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, final int viewType) {
        View view;
        switch (viewType){
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_cartlist, parent, false);
                return new HolderItems(view);
            case (VIEW_TYPE_LOADING):
                view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                return new ViewHolderLoading(view);
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holderParent, final int position) {

        if (holderParent instanceof HolderItems) {
            final HolderItems holder = (HolderItems) holderParent;
            final OfflineCart cart = items.get(position);

            Picasso.get()
                    .load(cart.getItem().get(0).getImage())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgProduct);

            holder.tvProductName.setText(cart.getItem().get(0).getName());
            holder.tvMeasurement.setText(cart.getItem().get(0).getMeasurement() + "\u0020" + cart.getItem().get(0).getUnit());
            double price, oPrice;
            String taxPercentage = "0";
            try {
                taxPercentage = (Double.parseDouble(cart.getTax_percentage()) > 0 ? cart.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!cart.getItem().get(0).getServe_for().equalsIgnoreCase("available")) {
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText(activity.getString(R.string.sold_out));
                holder.tvQuantity.setVisibility(View.GONE);
                CartFragment.isSoldOut = true;
            } else {
                holder.tvStatus.setVisibility(View.GONE);
            }

            if (Boolean.parseBoolean(cart.getItem().get(0).getIs_item_deliverable())) {
                holder.txtDeliveryStatus.setVisibility(View.VISIBLE);
                holder.txtDeliveryStatus.setText(activity.getString(R.string.msg_non_deliverable_to) + session.getData(Constant.GET_SELECTED_PINCODE_NAME));
                CartFragment.isDeliverable = true;
            } else {
                holder.txtDeliveryStatus.setVisibility(View.GONE);
            }

            holder.tvPrice.setText(new Session(activity).getData(Constant.CURRENCY) + (cart.getDiscounted_price().equals("0") ? cart.getPrice() : cart.getDiscounted_price()));
            holder.imgRemove.setOnClickListener(v -> removeItem(position));
            if (cart.getDiscounted_price().equals("0") || cart.getDiscounted_price().equals("")) {
                price = ((Float.parseFloat(cart.getPrice()) + ((Float.parseFloat(cart.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
            } else {
                price = ((Float.parseFloat(cart.getDiscounted_price()) + ((Float.parseFloat(cart.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                oPrice = ((Float.parseFloat(cart.getPrice()) + ((Float.parseFloat(cart.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvOriginalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));
            }
            holder.tvPrice.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

            holder.tvProductName.setText(cart.getItem().get(0).getName());
            holder.tvMeasurement.setText(cart.getItem().get(0).getMeasurement() + "\u0020" + cart.getItem().get(0).getUnit());

            holder.txtQuantity.setText(databaseHelper.CheckOrderExists(items.get(position).getId(), cart.getProduct_id()));
            cart.getItem().get(0).setCart_count(databaseHelper.CheckOrderExists(items.get(position).getId(), cart.getProduct_id()));

            holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price * Integer.parseInt(databaseHelper.CheckOrderExists(items.get(position).getId(), cart.getProduct_id()))));

            Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT + (price * Integer.parseInt(databaseHelper.CheckOrderExists(items.get(position).getId(), cart.getProduct_id())));
            CartFragment.SetData();

            final double finalPrice = price;
            holder.btnAddQuantity.setOnClickListener(view -> {
                if (ApiConfig.isConnected(activity)) {
                    if (!(Integer.parseInt(holder.txtQuantity.getText().toString()) >= Float.parseFloat(cart.getItem().get(0).getStock()))) {
                        if (!(Integer.parseInt(holder.txtQuantity.getText().toString()) + 1 > Integer.parseInt(session.getData(Constant.max_cart_items_count)))) {
                            int count = Integer.parseInt(holder.txtQuantity.getText().toString());
                            count++;
                            cart.getItem().get(0).setCart_count("" + count);
                            holder.txtQuantity.setText("" + count);
                            holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + finalPrice * count));
                            Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT + finalPrice;
                            databaseHelper.AddOrderData(items.get(position).getId(), cart.getProduct_id(), "" + count);
                            CartFragment.SetData();
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                    }
                }

            });

            holder.btnMinusQuantity.setOnClickListener(view -> {
                if (ApiConfig.isConnected(activity)) {
                    if (Integer.parseInt(holder.txtQuantity.getText().toString()) > 1) {
                        int count = Integer.parseInt(holder.txtQuantity.getText().toString());
                        count--;
                        cart.getItem().get(0).setCart_count("" + count);
                        holder.txtQuantity.setText("" + count);
                        holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + finalPrice * count));
                        Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT - finalPrice;
                        databaseHelper.AddOrderData(items.get(position).getId(), cart.getProduct_id(), "" + count);
                        CartFragment.SetData();
                    }
                }
            });

            if (getItemCount() == 0) {
                CartFragment.lytEmpty.setVisibility(View.VISIBLE);
                CartFragment.lytTotal.setVisibility(View.GONE);
            } else {
                CartFragment.lytEmpty.setVisibility(View.GONE);
                CartFragment.lytTotal.setVisibility(View.VISIBLE);
            }

        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        OfflineCart cart = items.get(position);
        if (cart != null)
            return Integer.parseInt(items.get(position).getId());
        else
            return position;
    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = view.findViewById(R.id.itemProgressbar);
        }
    }

    public static class HolderItems extends RecyclerView.ViewHolder {
        final ImageView imgProduct;
        final ImageView btnMinusQuantity;
        final ImageView btnAddQuantity;
        final ImageView imgRemove;
        final TextView tvProductName;
        final TextView tvMeasurement;
        final TextView tvPrice;
        final TextView tvOriginalPrice;
        final TextView txtQuantity;
        final TextView tvTotalPrice;
        final TextView tvStatus;
        final TextView txtDeliveryStatus;
        final LinearLayout tvQuantity;

        public HolderItems(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgRemove = itemView.findViewById(R.id.imgRemove);

            btnMinusQuantity = itemView.findViewById(R.id.btnMinusQuantity);
            btnAddQuantity = itemView.findViewById(R.id.btnAddQuantity);

            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvMeasurement = itemView.findViewById(R.id.tvMeasurement);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            txtDeliveryStatus = itemView.findViewById(R.id.txtDeliveryStatus);
        }
    }

    void showUndoSnackBar(OfflineCart cart, int position) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.undo_message), Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, R.color.gray));
        snackbar.setAction(activity.getString(R.string.undo), view -> {
            snackbar.dismiss();

            Constant.FLOAT_TOTAL_AMOUNT = 0.00;

            databaseHelper.AddOrderData(cart.getId(), cart.getItem().get(0).getProduct_id(), cart.getItem().get(0).getCart_count());

            add(position, cart);

            notifyDataSetChanged();
            CartFragment.SetData();
            CartFragment.isSoldOut = false;
            Constant.TOTAL_CART_ITEM = getItemCount();
            CartFragment.values.put(cart.getId(), databaseHelper.CheckOrderExists(cart.getId(), cart.getItem().get(0).getProduct_id()));
            CartFragment.SetData();
            activity.invalidateOptionsMenu();

        });
        snackbar.setActionTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }
}