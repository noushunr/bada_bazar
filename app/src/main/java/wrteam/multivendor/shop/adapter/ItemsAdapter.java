package wrteam.multivendor.shop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.activity.MainActivity;
import wrteam.multivendor.shop.fragment.TrackerDetailFragment;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.OrderTracker;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.CartItemHolder> {

    final Activity activity;
    final ArrayList<OrderTracker> orderTrackerArrayList;
    final Session session;
    final String from;

    public ItemsAdapter(Activity activity, ArrayList<OrderTracker> orderTrackerArrayList, String from) {
        this.activity = activity;
        this.orderTrackerArrayList = orderTrackerArrayList;
        this.from = from;
        session = new Session(activity);
    }

    @NonNull
    @Override
    public CartItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_items, null);
        return new CartItemHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull final CartItemHolder holder, final int position) {

        final OrderTracker order = orderTrackerArrayList.get(position);

        ApiConfig.setOrderTrackerLayout(activity, order, holder);

        String payType;
        if (order.getPayment_method().equalsIgnoreCase("cod"))
            payType = activity.getResources().getString(R.string.cod);
        else
            payType = order.getPayment_method();
        holder.tvQuantity.setText(order.getQuantity());

        String taxPercentage = order.getTax_percent();
        double price;

        if (order.activeStatus.equals(Constant.CANCELLED) || order.activeStatus.equals(Constant.RETURNED)) {
            holder.lytTracker.setVisibility(View.GONE);
            if (order.activeStatus.equals(Constant.CANCELLED)) {
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText(activity.getString(R.string.cancelled));
            } else if (order.activeStatus.equals(Constant.RETURNED)) {
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText(activity.getString(R.string.returned));
            } else {
                holder.tvStatus.setVisibility(View.GONE);
            }
        } else {
            holder.lytTracker.setVisibility(View.VISIBLE);
        }

        if (order.cancelable_status.equals("1")) {
            if (order.till_status.equals(Constant.RECEIVED) && order.activeStatus.equals(Constant.RECEIVED)) {
                holder.btnCancel.setVisibility(View.VISIBLE);
            } else if (order.till_status.equals(Constant.PROCESSED) && (order.activeStatus.equals(Constant.RECEIVED) || order.activeStatus.equals(Constant.PROCESSED))) {
                holder.btnCancel.setVisibility(View.VISIBLE);
            } else if (order.till_status.equals(Constant.SHIPPED) && (order.activeStatus.equals(Constant.RECEIVED) || order.activeStatus.equals(Constant.PROCESSED) || order.activeStatus.equals(Constant.SHIPPED))) {
                holder.btnCancel.setVisibility(View.VISIBLE);
            } else {
                holder.btnCancel.setVisibility(View.GONE);
            }
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }

        if (order.return_status.equals("1")) {
            if (order.activeStatus.equals(Constant.DELIVERED)) {
                holder.btnReturn.setVisibility(View.VISIBLE);
            }
        } else {
            holder.btnReturn.setVisibility(View.GONE);
        }

        if (order.getDiscounted_price().equals("0") || order.getDiscounted_price().equals("")) {
            price = (((Float.parseFloat(order.getPrice()) + ((Float.parseFloat(order.getPrice()) * Float.parseFloat(taxPercentage)) / 100))) * Integer.parseInt(order.getQuantity()));
        } else {
            price = (((Float.parseFloat(order.getDiscounted_price()) + ((Float.parseFloat(order.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100))) * Integer.parseInt(order.getQuantity()));
        }
        holder.tvPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

        holder.tvPaymentType.setText(activity.getResources().getString(R.string.via) + payType);
        holder.tvName.setText(order.getName() + "(" + order.getMeasurement() + order.getUnit() + ")");

        Picasso.get().
                load(order.getImage())
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgOrder);

        holder.cardViewDetail.setOnClickListener(v -> {
            Fragment fragment = new TrackerDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("id", "");
            bundle.putSerializable("model", order);
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });


        holder.btnCancel.setOnClickListener(view -> updateOrderStatus(activity, order, Constant.CANCELLED, holder, from));

        holder.btnReturn.setOnClickListener(view -> {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            Date date = new Date();
            //System.out.println (myFormat.format (date));
            String inputString1 = order.getActiveStatusDate();
            String inputString2 = myFormat.format(date);
            try {
                Date date1 = myFormat.parse(inputString1);
                Date date2 = myFormat.parse(inputString2);
                long diff = Objects.requireNonNull(date2).getTime() - Objects.requireNonNull(date1).getTime();
                //  System.out.println("Days: "+TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

                if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= Integer.parseInt(order.getReturn_days())) {
                    updateOrderStatus(activity, order, Constant.RETURNED, holder, from);
                } else {
                    final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getResources().getString(R.string.product_return) + Integer.parseInt(new Session(activity).getData(Constant.max_product_return_days)) + activity.getString(R.string.day_max_limit), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(activity.getResources().getString(R.string.ok), view1 -> snackbar.dismiss());
                    snackbar.setActionTextColor(Color.RED);
                    View snackBarView = snackbar.getView();
                    TextView textView = snackBarView.findViewById(R.id.snackbar_text);
                    textView.setMaxLines(5);
                    snackbar.show();

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

    }

    private void updateOrderStatus(final Activity activity, final OrderTracker order, final String status, final CartItemHolder holder, final String from) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        if (status.equals(Constant.CANCELLED)) {
            alertDialog.setTitle(activity.getResources().getString(R.string.cancel_item));
            alertDialog.setMessage(activity.getResources().getString(R.string.cancel_msg));
        } else if (status.equals(Constant.RETURNED)) {
            alertDialog.setTitle(activity.getResources().getString(R.string.return_order));
            alertDialog.setMessage(activity.getResources().getString(R.string.return_msg));
        }
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(activity.getResources().getString(R.string.yes), (dialog, which) -> {

            Map<String, String> params = new HashMap<>();
            params.put(Constant.UPDATE_ORDER_STATUS, Constant.GetVal);
            params.put(Constant.ORDER_ID, order.getOrder_id());
            params.put(Constant.ORDER_ITEM_ID, order.getId());
            params.put(Constant.STATUS, status);
            ApiConfig.RequestToVolley((result, response) -> {
                // System.out.println("================= " + response);
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            if (status.equals(Constant.CANCELLED)) {
                                holder.btnCancel.setVisibility(View.GONE);
                                order.status = status;
                                if (from.equals("detail")) {
                                    orderTrackerArrayList.size();
                                }
                                ApiConfig.getWalletBalance(activity, new Session(activity));
                            } else {
                                holder.btnReturn.setVisibility(View.GONE);
                            }
                            Constant.isOrderCancelled = true;
                        }
                        Toast.makeText(activity, object.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, params, true);

        });
        alertDialog.setNegativeButton(activity.getResources().getString(R.string.no), (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return orderTrackerArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class CartItemHolder extends RecyclerView.ViewHolder {
        final TextView tvQuantity;
        final TextView tvPrice;
        final TextView tvPaymentType;
        final TextView tvStatus;
        final TextView tvName;
        final ImageView imgOrder;
        final CardView cardViewDetail;
        final Button btnCancel;
        final Button btnReturn;
        final LinearLayout lytTracker;

        public CartItemHolder(View itemView) {
            super(itemView);

            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPaymentType = itemView.findViewById(R.id.tvPaymentType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvName = itemView.findViewById(R.id.tvName);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            imgOrder = itemView.findViewById(R.id.imgOrder);
            cardViewDetail = itemView.findViewById(R.id.cardViewDetail);
            btnReturn = itemView.findViewById(R.id.btnReturn);
            lytTracker = itemView.findViewById(R.id.lytTracker);
        }
    }

}
