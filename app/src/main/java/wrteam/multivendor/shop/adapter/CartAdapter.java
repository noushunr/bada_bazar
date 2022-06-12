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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.fragment.CartFragment;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Cart;


@SuppressLint("NotifyDataSetChanged")
public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final ArrayList<Cart> items;
    final Session session;


    public CartAdapter(Context context, Activity activity, ArrayList<Cart> items) {
        this.activity = activity;
        this.items = items;
        session = new Session(context);
    }

    public void add(int position, Cart item) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        Cart cart = items.get(position);

        if (CartFragment.values.containsKey(cart.getProduct_variant_id())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CartFragment.values.replace(cart.getProduct_variant_id(), "0");
            } else {
                CartFragment.values.remove(cart.getProduct_variant_id());
                CartFragment.values.put(cart.getProduct_variant_id(), "0");
            }
        } else {
            CartFragment.values.put(cart.getProduct_variant_id(), "0");
        }
        Constant.FLOAT_TOTAL_AMOUNT = 0.00;

        items.remove(cart);
        CartFragment.isSoldOut = false;
        CartFragment.isDeliverable = false;
        notifyDataSetChanged();
        CartFragment.SetData();
        Constant.TOTAL_CART_ITEM = getItemCount();
        CartFragment.SetData();
        activity.invalidateOptionsMenu();
        if (getItemCount() == 0) {
            CartFragment.lytEmpty.setVisibility(View.VISIBLE);
            CartFragment.lytTotal.setVisibility(View.GONE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CartFragment.values.replace(cart.getProduct_variant_id(), "0");
            } else {
                CartFragment.values.remove(cart.getProduct_variant_id());
                CartFragment.values.put(cart.getProduct_variant_id(), "0");
            }
        }

        ApiConfig.AddMultipleProductInCart(session, activity, CartFragment.values);
        showUndoSnackBar(cart, position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
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
        try {

            if (holderParent instanceof HolderItems) {
                final HolderItems holder = (HolderItems) holderParent;
                final Cart cart = items.get(position);

                double price;
                double oPrice;
                String taxPercentage = "0";

                try {
                    taxPercentage = (Double.parseDouble(cart.getItems().get(0).getTax_percentage()) > 0 ? cart.getItems().get(0).getTax_percentage() : "0");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (cart.getItems().get(0).getDiscounted_price().equals("0") || cart.getItems().get(0).getDiscounted_price().equals("")) {
                    price = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                } else {
                    price = ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    oPrice = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                    holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvOriginalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));
                }

                Constant.FLOAT_TOTAL_AMOUNT += (price * Integer.parseInt(cart.getQty()));


                Picasso.get()
                        .load(cart.getItems().get(0).getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.imgProduct);

                holder.tvProductName.setText(cart.getItems().get(0).getName());
                holder.imgRemove.setOnClickListener(v -> removeItem(position));

                holder.tvMeasurement.setText(cart.getItems().get(0).getMeasurement() + "\u0020" + cart.getItems().get(0).getUnit());

                if (cart.getItems().get(0).getIsAvailable().equals("false")) {
                    holder.tvStatus.setVisibility(View.VISIBLE);
                    holder.tvStatus.setText(activity.getString(R.string.sold_out));
                    holder.tvQuantity.setVisibility(View.GONE);
                    CartFragment.isSoldOut = true;
                } else {
                    holder.tvStatus.setVisibility(View.GONE);
                }

                if (Boolean.parseBoolean(cart.getItems().get(0).getIs_item_deliverable())) {
                    holder.txtDeliveryStatus.setVisibility(View.VISIBLE);
                    holder.txtDeliveryStatus.setText(activity.getString(R.string.msg_non_deliverable_to) + session.getData(Constant.GET_SELECTED_PINCODE_NAME));
                    CartFragment.isDeliverable = true;
                } else {
                    holder.txtDeliveryStatus.setVisibility(View.GONE);
                }

                holder.tvPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

                holder.txtQuantity.setText(cart.getQty());

                holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price * Integer.parseInt(cart.getQty())));

                final double finalPrice = price;
                holder.btnAddQuantity.setOnClickListener(view -> {

                    if (ApiConfig.isConnected(activity)) {
                        if (!(Integer.parseInt(holder.txtQuantity.getText().toString()) >= Float.parseFloat(cart.getItems().get(0).getStock()))) {
                            if (!(Integer.parseInt(holder.txtQuantity.getText().toString()) + 1 > Integer.parseInt(session.getData(Constant.max_cart_items_count)))) {
                                int count = Integer.parseInt(holder.txtQuantity.getText().toString());
                                count++;
                                cart.setQty("" + count);
                                holder.txtQuantity.setText("" + count);
                                holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + finalPrice * count));
                                Constant.FLOAT_TOTAL_AMOUNT += finalPrice;
                                if (CartFragment.values.containsKey(cart.getProduct_variant_id())) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        CartFragment.values.replace(cart.getProduct_variant_id(), "" + count);
                                    } else {
                                        CartFragment.values.remove(cart.getProduct_variant_id());
                                        CartFragment.values.put(cart.getProduct_variant_id(), "" + count);
                                    }
                                } else {
                                    CartFragment.values.put(cart.getProduct_variant_id(), "" + count);
                                }
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
                            cart.setQty("" + count);
                            holder.txtQuantity.setText("" + count);
                            holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + finalPrice * count));
                            Constant.FLOAT_TOTAL_AMOUNT -= finalPrice;
                            if (CartFragment.values.containsKey(cart.getProduct_variant_id())) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    CartFragment.values.replace(cart.getProduct_variant_id(), "" + count);
                                } else {
                                    CartFragment.values.remove(cart.getProduct_variant_id());
                                    CartFragment.values.put(cart.getProduct_variant_id(), "" + count);
                                }
                            } else {
                                CartFragment.values.put(cart.getProduct_variant_id(), "" + count);
                            }
                            CartFragment.SetData();
                        }
                    }
                });

                CartFragment.SetData();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        Cart cart = items.get(position);
        if (cart != null)
            return Integer.parseInt(cart.getId());
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
            txtDeliveryStatus = itemView.findViewById(R.id.txtDeliveryStatus);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }

    void showUndoSnackBar(Cart cart, int position) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.undo_message), Snackbar.LENGTH_LONG);
        snackbar.setAction(activity.getString(R.string.undo), view -> {
            snackbar.dismiss();

            Constant.FLOAT_TOTAL_AMOUNT = 0.00;

            CartFragment.values.put(cart.getProduct_variant_id(), cart.getQty());

            add(position, cart);
            notifyDataSetChanged();
            CartFragment.SetData();
            CartFragment.isSoldOut = false;
            Constant.TOTAL_CART_ITEM = getItemCount();
            CartFragment.SetData();
            ApiConfig.AddMultipleProductInCart(session, activity, CartFragment.values);
            activity.invalidateOptionsMenu();
        });
        snackbar.setActionTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }
}