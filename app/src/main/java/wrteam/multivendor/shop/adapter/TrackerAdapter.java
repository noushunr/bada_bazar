package wrteam.multivendor.shop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.activity.MainActivity;
import wrteam.multivendor.shop.fragment.TrackerDetailFragment;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.OrderTracker;


public class TrackerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final ArrayList<OrderTracker> orderTrackerArrayList;
    final Context context;
    public boolean isLoading;


    public TrackerAdapter(Context context, Activity activity, ArrayList<OrderTracker> orderTrackerArrayList) {
        this.context = context;
        this.activity = activity;
        this.orderTrackerArrayList = orderTrackerArrayList;
    }

    public void add(int position, OrderTracker item) {
        orderTrackerArrayList.add(position, item);
        notifyItemInserted(position);
    }

    public void setLoaded() {
        isLoading = false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view;
        switch (viewType){
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_trackorder, parent, false);
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderParent, final int position) {

        if (holderParent instanceof HolderItems) {
            final HolderItems holder = (HolderItems) holderParent;
            final OrderTracker order = orderTrackerArrayList.get(position);
            holder.tvOrderID.setText(activity.getString(R.string.order_number) + order.getId());
            String[] date = order.getDate_added().split("\\s+");
            holder.tvOrderDate.setText(activity.getString(R.string.ordered_on) + date[0]);
            holder.tvOrderAmount.setText(activity.getString(R.string.for_amount_on) + new Session(context).getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getFinal_total()));

            holder.cardViewDetail.setOnClickListener(v -> {
                Fragment fragment = new TrackerDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", "");
                bundle.putSerializable("model", order);
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });

            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < order.getItemsList().size(); i++) {
                items.add(order.getItemsList().get(i).getName());
            }
            holder.tvItems.setText(Arrays.toString(items.toArray()).replace("]", "").replace("[", ""));
            holder.tvTotalItems.setText(items.size() + activity.getString(R.string.item));

        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return orderTrackerArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return orderTrackerArrayList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
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
        final TextView tvOrderID;
        final TextView tvOrderDate;
        final TextView cardViewDetail;
        final TextView tvOrderAmount;
        final TextView tvTotalItems;
        final TextView tvItems;
        final CardView cardView;

        public HolderItems(View itemView) {
            super(itemView);
            tvOrderID = itemView.findViewById(R.id.tvOrderID);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            cardViewDetail = itemView.findViewById(R.id.cardViewDetail);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvTotalItems = itemView.findViewById(R.id.tvTotalItems);
            tvItems = itemView.findViewById(R.id.tvItems);
            cardView = itemView.findViewById(R.id.cardView);

        }
    }
}