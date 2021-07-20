package com.imesconsult.originstore.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imesconsult.originstore.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    //uid views
    private ImageButton backBtn;
    private ImageView profileTv;
    private TextView shopNameTv;
    private RatingBar ratingBar;
    private EditText reviewEt;
    private FloatingActionButton reviewSubmitBtn;


    private String shopUid;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        //init views
        backBtn=findViewById(R.id.backBtn);
        profileTv=findViewById(R.id.profileTv);
        shopNameTv=findViewById(R.id.shopNameTv);
        ratingBar=findViewById(R.id.ratingBar);
        reviewEt=findViewById(R.id.reviewEt);
        reviewSubmitBtn=findViewById(R.id.reviewSubmitBtn);

        firebaseAuth=FirebaseAuth.getInstance();

        //get shop uid from intent
        shopUid=getIntent().getStringExtra("shopUid");

        //load shop info: shop name, shop image
        loadShopInfo();

        //if user has written review to this shop, load it
        loadMyReview();

        //go back to preview activity
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //input data
        reviewSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });



    }

    private void loadShopInfo() {
        DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("Users");
        ref1.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName=""+snapshot.child("shopName").getValue();
                String shopImage=""+snapshot.child("profileImage").getValue();

                //set shop info to ui
                shopNameTv.setText(shopName);

                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_baseline_store_24).into(profileTv);

                }catch (Exception e){
                    profileTv.setImageResource(R.drawable.ic_baseline_store_24);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

   private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            //my review is available in this shop

                            //get review details
                            String uid = ""+snapshot.child("uid").getValue();
                            String ratings = ""+snapshot.child("ratings").getValue();
                            String review = ""+snapshot.child("review").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();

                            //set review details to ui
                            float myRating=Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            reviewEt.setText(review);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings = ""+ratingBar.getRating();
        String review = reviewEt.getText().toString().trim();

        //time of review
        String timestamp =""+System.currentTimeMillis();

        //set up data in hashmap
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("ratings",""+ratings); //eg. 4.5
        hashMap.put("review",""+review); //eg. Good Service
        hashMap.put("timestamp",""+timestamp); //eg. 4.5

        //put to db > Users> Shop Uid > Ratings
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //review added to db
                        Toast.makeText(WriteReviewActivity.this, "Review posted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to add to db
                        Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}