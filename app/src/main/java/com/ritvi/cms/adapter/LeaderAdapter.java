package com.ritvi.cms.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ritvi.cms.R;
import com.ritvi.cms.Util.Pref;
import com.ritvi.cms.Util.TagUtils;
import com.ritvi.cms.Util.ToastClass;
import com.ritvi.cms.activity.AllLeaderActivity;
import com.ritvi.cms.pojo.leader.LeaderPOJO;
import com.ritvi.cms.webservice.AdapterWebService;
import com.ritvi.cms.webservice.MsgPassInterface;
import com.ritvi.cms.webservice.WebServicesCallBack;
import com.ritvi.cms.webservice.WebServicesUrls;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sunil on 03-11-2017.
 */

public class LeaderAdapter extends RecyclerView.Adapter<LeaderAdapter.ViewHolder> implements WebServicesCallBack {
    private List<LeaderPOJO> items;
    Activity activity;
    Fragment fragment;
    int device_height = 0;

    public LeaderAdapter(Activity activity, Fragment fragment, List<LeaderPOJO> items) {
        this.items = items;
        this.activity = activity;
        this.fragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_leaders, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.tv_leader_name.setText(items.get(position).getUpFirstName()+" "+items.get(position).getUpLastName());
        holder.tv_leader_email.setText("email");


        if(activity instanceof AllLeaderActivity) {
            if (items.get(position).getMyFavourite().equals("1")) {
                holder.iv_favorite.setImageResource(R.drawable.ic_favorite);
            } else {
                holder.iv_favorite.setImageResource(R.drawable.ic_unfavorite);
            }

            holder.iv_favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callFavoriteAPI(items.get(position), holder.iv_favorite);
                }
            });
        }else{
            holder.iv_favorite.setVisibility(View.GONE);
        }


        holder.ll_leader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLeaderProfile(items.get(position));
            }
        });

        holder.itemView.setTag(items.get(position));
    }

    public void callFavoriteAPI(LeaderPOJO leaderPOJO, final ImageView favorite_image) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("request_action","SET_AS_FAVOURITE"));
        nameValuePairs.add(new BasicNameValuePair("citizen_id", Pref.GetUserProfile(activity.getApplicationContext()).getCitizenId()));
        nameValuePairs.add(new BasicNameValuePair("leader_profile_id",leaderPOJO.getUpLeaderId()));

        new AdapterWebService(activity, nameValuePairs, false, new MsgPassInterface() {
            @Override
            public void onMsgPassed(String response) {
                Log.d(TagUtils.getTag(),"api called:-"+response);
                try {
                    JSONObject jsonObject=new JSONObject(response);
                    if(jsonObject.optString("status").equals("success")){
                        ToastClass.showShortToast(activity.getApplicationContext(),jsonObject.optString("message"));
                        if(jsonObject.optString("favourite").equals("0")){
                            favorite_image.setImageResource(R.drawable.ic_unfavorite);
                        }else{
                            favorite_image.setImageResource(R.drawable.ic_favorite);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).executeApi(WebServicesUrls.CITIZEN_PROCESS);
    }

    public void showLeaderProfile(LeaderPOJO leaderPOJO){
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_Dialog);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_leader);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setTitle("");
        dialog.show();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ImageView iv_close=dialog.findViewById(R.id.iv_close);

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        TextView tv_profile_name=dialog.findViewById(R.id.tv_profile_name);
        TextView tv_mobile_number=dialog.findViewById(R.id.tv_mobile_number);

        tv_mobile_number.setText(leaderPOJO.getUpMobile());
        tv_profile_name.setText(leaderPOJO.getUpFirstName()+" "+leaderPOJO.getUpLastName());

    }


    private final String TAG = getClass().getSimpleName();

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onGetMsg(String apicall, String response) {

    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView cv_image;
        public ImageView iv_favorite;
        public TextView tv_leader_email;
        public TextView tv_leader_name;
        public LinearLayout ll_leader;

        public ViewHolder(View itemView) {
            super(itemView);
            cv_image = (CircleImageView) itemView.findViewById(R.id.cv_image);
            iv_favorite = (ImageView) itemView.findViewById(R.id.iv_favorite);
            tv_leader_email = (TextView) itemView.findViewById(R.id.tv_leader_email);
            tv_leader_name = (TextView) itemView.findViewById(R.id.tv_leader_name);
            ll_leader = (LinearLayout) itemView.findViewById(R.id.ll_leader);
        }
    }
}
