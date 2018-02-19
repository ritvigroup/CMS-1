package com.ritvi.cms.activity;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.google.gson.Gson;
import com.ritvi.cms.R;
import com.ritvi.cms.Util.Pref;
import com.ritvi.cms.Util.TagUtils;
import com.ritvi.cms.Util.ToastClass;
import com.ritvi.cms.adapter.LeaderAdapter;
import com.ritvi.cms.pojo.leader.LeaderAPIResultPOJO;
import com.ritvi.cms.pojo.leader.LeaderPOJO;
import com.ritvi.cms.webservice.WebServiceBase;
import com.ritvi.cms.webservice.WebServicesCallBack;
import com.ritvi.cms.webservice.WebServicesUrls;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AllLeaderActivity extends LocalizationActivity implements WebServicesCallBack {

    private static final String CALL_ALL_LEADER = "call_all_leader";
    @BindView(R.id.rv_leader)
    RecyclerView rv_leader;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    List<LeaderPOJO> leaderPOJOS = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_leader);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Leader");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        attachAdapter();
        callLeaderAPI();
    }

    LeaderAdapter leaderAdapter;

    public void attachAdapter() {
        leaderAdapter = new LeaderAdapter(this, null, leaderPOJOS);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_leader.setHasFixedSize(true);
        rv_leader.setAdapter(leaderAdapter);
        rv_leader.setLayoutManager(linearLayoutManager);
        rv_leader.setItemAnimator(new DefaultItemAnimator());

    }

    public void callLeaderAPI() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("request_action", "ALL_LEADERS"));
        nameValuePairs.add(new BasicNameValuePair("c_profile_id", Pref.GetUserProfile(getApplicationContext()).getUserId()));
        nameValuePairs.add(new BasicNameValuePair("search_text", ""));
        new WebServiceBase(nameValuePairs, this, this, CALL_ALL_LEADER, true).execute(WebServicesUrls.USER_ADMIN_PROCESS);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onGetMsg(String apicall, String response) {
        Log.d(TagUtils.getTag(), apicall + ":-" + response);
        switch (apicall) {
            case CALL_ALL_LEADER:
                parseALLLeaderResponse(response);
                break;
        }
    }


    public void parseALLLeaderResponse(String response) {
        leaderPOJOS.clear();
        try {
            Gson gson = new Gson();
            LeaderAPIResultPOJO leaderAPIResultPOJO = gson.fromJson(response, LeaderAPIResultPOJO.class);
            if (leaderAPIResultPOJO.getStatus().equals("success")) {
                leaderPOJOS.addAll(leaderAPIResultPOJO.getLeaderPOJOS());
            } else {
                ToastClass.showShortToast(getApplicationContext(), "No Leader Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        leaderAdapter.notifyDataSetChanged();
    }
}
