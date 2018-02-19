package com.ritvi.cms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.google.gson.Gson;
import com.ritvi.cms.R;
import com.ritvi.cms.Util.Pref;
import com.ritvi.cms.Util.TagUtils;
import com.ritvi.cms.Util.ToastClass;
import com.ritvi.cms.adapter.CustomAutoCompleteAdapter;
import com.ritvi.cms.pojo.communication.CommunicationSubmittionPOJO;
import com.ritvi.cms.pojo.leader.LeaderAPIResultPOJO;
import com.ritvi.cms.pojo.leader.LeaderPOJO;
import com.ritvi.cms.pojo.user.UserProfilePOJO;
import com.ritvi.cms.webservice.WebServiceBase;
import com.ritvi.cms.webservice.WebServicesCallBack;
import com.ritvi.cms.webservice.WebServicesUrls;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddCommunication extends LocalizationActivity implements WebServicesCallBack {
    private static final String CALL_ALL_LEADER = "call_all_leader";
    @BindView(R.id.btn_next)
    Button btn_next;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_favorite_leader_add)
    ImageView iv_favorite_leader_add;
    @BindView(R.id.auto_fav_list)
    AutoCompleteTextView auto_fav_list;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.et_father_name)
    EditText et_father_name;
    @BindView(R.id.et_mobile_number)
    EditText et_mobile_number;
    @BindView(R.id.et_email)
    EditText et_email;
    @BindView(R.id.et_aadhar)
    EditText et_aadhar;

    @BindView(R.id.rg_gender)
    RadioGroup rg_gender;
    @BindView(R.id.rb_male)
    RadioButton rb_male;
    @BindView(R.id.rb_female)
    RadioButton rb_female;
    @BindView(R.id.rb_other)
    RadioButton rb_other;

    String leader_id="";

    List<LeaderPOJO> leaderPOJOS = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_communication);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle("Add Communication");

        iv_favorite_leader_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddCommunication.this, FavoriteLeaderActivity.class));
            }
        });

        autoFillForm();

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });

        auto_fav_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(leaderPOJOS.size()>0){
                    leader_id=leaderPOJOS.get(i).getUp_user_profile_id();
                }
            }
        });
    }

    public void checkValidation(){
        if(et_name.getText().toString().length()>0&&et_father_name.getText().toString().length()>0&&
                et_mobile_number.getText().toString().length()>0&&auto_fav_list.getText().toString().length()>0&&
                leader_id.length()>0){
            CommunicationSubmittionPOJO communicationSubmittionPOJO=new CommunicationSubmittionPOJO();
            communicationSubmittionPOJO.setUser_id(Pref.GetUserProfile(getApplicationContext()).getUserId());
            communicationSubmittionPOJO.setC_name(et_name.getText().toString());
            communicationSubmittionPOJO.setC_gender(((RadioButton)findViewById(rg_gender.getCheckedRadioButtonId())).getText().toString());
            communicationSubmittionPOJO.setSelf_other_group("1");
            communicationSubmittionPOJO.setC_father_name(et_father_name.getText().toString());
            communicationSubmittionPOJO.setC_mobile(et_mobile_number.getText().toString());
            communicationSubmittionPOJO.setC_email(et_email.getText().toString());
            communicationSubmittionPOJO.setC_aadhaar_number(et_aadhar.getText().toString());
            communicationSubmittionPOJO.setLeader_id(leader_id);
            Intent intent=new Intent(AddCommunication.this, AddCommunicationAddressActivity.class);
            intent.putExtra("complaintPOJO",communicationSubmittionPOJO);
            startActivity(intent);
        }else{
            ToastClass.showShortToast(getApplicationContext(),"Please Enter Mandatory Fields");
        }
    }

    public void autoFillForm() {
        UserProfilePOJO userProfilePOJO = Pref.GetUserProfile(getApplicationContext());
        setField(userProfilePOJO.getUserFullName(), et_name);
        setField(userProfilePOJO.getUserMobile(), et_mobile_number);
        setField(userProfilePOJO.getUserEmail(), et_email);

        String gender = userProfilePOJO.getUserGender();
        if (gender != null && gender.length() > 0) {
            switch (gender.toLowerCase().toString()) {
                case "male":
                    rb_male.setChecked(true);
                    break;
                case "female":
                    rb_female.setChecked(true);
                    break;
                case "other":
                    rb_other.setChecked(true);
                    break;
            }
        }
    }

    public void setField(String text, EditText editText) {
        if (text != null && text.length() > 0) {
            editText.setText(text);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        callLeaderAPI();
    }

    public void callLeaderAPI() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("request_action", "MY_FAVOURITE_LEADER"));
        nameValuePairs.add(new BasicNameValuePair("c_profile_id", Pref.GetUserProfile(getApplicationContext()).getUserId()));
        new WebServiceBase(nameValuePairs, this, this, CALL_ALL_LEADER, false).execute(WebServicesUrls.USER_ADMIN_PROCESS);
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

    CustomAutoCompleteAdapter adapter = null;

    public void parseALLLeaderResponse(String response) {
        leaderPOJOS.clear();
        try {
            Gson gson = new Gson();
            LeaderAPIResultPOJO leaderAPIResultPOJO = gson.fromJson(response, LeaderAPIResultPOJO.class);
            if (leaderAPIResultPOJO.getStatus().equals("success")) {
                leaderPOJOS.addAll(leaderAPIResultPOJO.getLeaderPOJOS());
                adapter = new CustomAutoCompleteAdapter(this, (ArrayList<LeaderPOJO>) leaderPOJOS);
                auto_fav_list.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
