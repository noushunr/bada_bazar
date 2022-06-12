package wrteam.multivendor.shop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.activity.MainActivity;
import wrteam.multivendor.shop.fragment.CartFragment;
import wrteam.multivendor.shop.fragment.HomeFragment;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.PinCode;

public class PinCodeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final ArrayList<PinCode> pinCodes;
    public boolean isLoading;
    final Session session;
    final String from;


    public PinCodeAdapter(Activity activity, ArrayList<PinCode> pinCodes, String from) {
        this.activity = activity;
        this.session = new Session(activity);
        this.pinCodes = pinCodes;
        this.from = from;
    }

    public void add(int position, PinCode pinCode) {
        pinCodes.add(position, pinCode);
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
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_pin_code_list, parent, false);
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
            try {
                final PinCode pinCode = pinCodes.get(position);

                holder.tvPinCode.setText(pinCode.getPincode());

                holder.tvPinCode.setOnClickListener(v -> {
                    session.setBoolean(Constant.GET_SELECTED_PINCODE, true);
                    session.setData(Constant.GET_SELECTED_PINCODE_ID, pinCode.getId());
                    session.setData(Constant.GET_SELECTED_PINCODE_NAME, pinCode.getPincode());
                    if (HomeFragment.tvLocation != null) {
                        HomeFragment.tvLocation.setText(pinCode.getPincode());
                    }
                    if (CartFragment.tvLocation != null) {
                        CartFragment.tvLocation.setText(pinCode.getPincode());
                    }
                    if (from.equals("home")) {
                        HomeFragment.refreshListener.onRefresh();
                    } else {
                        CartFragment.refreshListener.onRefresh();
                    }
                    MainActivity.pinCodeFragment.dismiss();
                });
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
        return pinCodes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return pinCodes.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = view.findViewById(R.id.itemProgressbar);
        }
    }

    static class HolderItems extends RecyclerView.ViewHolder {

        final TextView tvPinCode;
        public HolderItems(@NonNull View itemView) {
            super(itemView);

            tvPinCode = itemView.findViewById(R.id.tvPinCode);
        }
    }
}
