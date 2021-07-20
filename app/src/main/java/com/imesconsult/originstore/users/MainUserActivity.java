package com.imesconsult.originstore.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.imesconsult.originstore.R;
import com.imesconsult.originstore.SettingsActivity;
import com.imesconsult.originstore.adapters.AdapterOrderUser;
import com.imesconsult.originstore.adapters.AdapterShop;
import com.imesconsult.originstore.models.ModelOrderUser;
import com.imesconsult.originstore.models.ModelShop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {
    private TextView nameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv;
    private ImageButton logOutBtn, profileBtn,filterOrderBtn,optionsBtn;
    private ImageView profileIv;
    private RelativeLayout shopsR1, ordersR1;
    private RecyclerView shopsRv,ordersRv;
    private TextView filteredOrdersTv;
    private EditText searchShopsEt;



    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelShop>shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser>ordersList;
    private AdapterOrderUser adapterOrderUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTv = findViewById(R.id.nameTv);
        logOutBtn = findViewById(R.id.logoutBtn);
        profileBtn = findViewById(R.id.profileBtn);
        profileIv = findViewById(R.id.profileIv);
        profileIv = findViewById(R.id.profileIv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        tabShopsTv = findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        shopsR1 = findViewById(R.id.shopsR1);
        ordersR1 = findViewById(R.id.ordersR1);
        shopsRv = findViewById(R.id.shopsRv);
        ordersRv=findViewById(R.id.ordersRv);
        filterOrderBtn=findViewById(R.id.filterOrderBtn);
        filteredOrdersTv=findViewById(R.id.filteredOrdersTv);
        optionsBtn=findViewById(R.id.optionsBtn);
        searchShopsEt=findViewById(R.id.searchShopsEt);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth = FirebaseAuth.getInstance();
        CheckUser();

        ShowShopsUI(); //show shops UI on start

        //search Shops
        searchShopsEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try{
                    adapterShop.getFilter().filter(charSequence);


                }catch (Exception e){
                    e.printStackTrace();

                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();
                //make offline
                //sign out
                //go to login activity
            }
        });

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit profile activity
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
            }
        });
        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show shops
                ShowShopsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show orders
                ShowOrdersUI();
            }
        });
        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //option to display in dialog
                final String[] options={"All","In Progress","Completed","Cancelled"};
                //dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(MainUserActivity.this);
                builder.setTitle("Filter Orders: ")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item click
                                if(which==0){
                                    //all clicked
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterOrderUser.getFilter().filter("");//show all orders
                                }else{
                                    String optionClicked=options[which];
                                    filteredOrdersTv.setText("Showing "+optionClicked+" Orders");// e.g Showing Completed Orders
                                    adapterOrderUser.getFilter().filter(optionClicked);

                                }
                            }
                        }).show();
            }
        });


        optionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu options = new PopupMenu(getApplicationContext(),optionsBtn);
                //inflate the popup menu
                options.getMenuInflater().inflate(R.menu.menu_user,options.getMenu());
                options.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.notificationSetting:
                                startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
                                return true;
                        }
                        return false;
                    }
                });
                options.show();

            }
        });


    }




    private void ShowShopsUI() {
        //show shop UI , hide orders UI
        shopsR1.setVisibility(View.VISIBLE);
        ordersR1.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void ShowOrdersUI() {
        //show orders UI , hide Shops UI
        ordersR1.setVisibility(View.VISIBLE);
        shopsR1.setVisibility(View.GONE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void makeMeOffline() {
        //after logging Out make user offline
        progressDialog.setMessage("Logging Out...");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //updated successfully
                        firebaseAuth.signOut();
                        CheckUser();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void CheckUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get user data
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();

                            //set user data
                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try{
                                Picasso.get().load(profileImage).placeholder(R.drawable.person_grey).into(profileIv);
                            }catch (Exception e){
                                profileIv.setImageResource(R.drawable.person_grey);
                            }
                            //load only those shops that are in the same city of user
                            loadShops(city);
                            loadOrders();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {
        //init order list
        ordersList=new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    String uid=""+ds.getRef().getKey();

                    DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        for(DataSnapshot ds:snapshot.getChildren()){
                                            ModelOrderUser modelOrderUser=ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            ordersList.add(modelOrderUser);
                                        }
                                        //set up adapter
                                        adapterOrderUser=new AdapterOrderUser(MainUserActivity.this,ordersList);
                                        //set to recyclerview
                                        ordersRv.setAdapter(adapterOrderUser);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(final String myCity) {
        //init list
        shopsList=new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        shopsList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelShop modelShop=ds.getValue(ModelShop.class);

                            String shopCity=""+ds.child("city").getValue();

                            //show only shops from user city
                            if(shopCity.equals(myCity)){
                                shopsList.add(modelShop);
                            }
                            //if you want to display all shops , skip the if statement and add this
                            //shopsList.add(modelShop);
                        }
                        //setup adapter
                        adapterShop=new AdapterShop(MainUserActivity.this,shopsList);
                        //set adapter to recycler view
                        shopsRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}