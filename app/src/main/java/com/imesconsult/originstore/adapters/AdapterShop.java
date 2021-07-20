package com.imesconsult.originstore.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imesconsult.originstore.FilterShopUser;
import com.imesconsult.originstore.R;
import com.imesconsult.originstore.models.ModelShop;
import com.imesconsult.originstore.users.ShopDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> implements Filterable {

    private Context context;
    public ArrayList<ModelShop> shopsList,filterList ;
    private FilterShopUser filter;

    public AdapterShop(Context context, ArrayList<ModelShop> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
        this.filterList = shopsList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_shop.xml
        View view= LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //get data
        ModelShop modelShop=shopsList.get(position);
        String accountType = modelShop.getAccountType();
        String address = modelShop.getAddress();
        String city= modelShop.getCity();
        String country = modelShop.getCountry();
        String deliveryFee = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        String latitude=modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String online = modelShop.getOnline();
        String name = modelShop.getName();
        String phone = modelShop.getPhone();
        final String uid = modelShop.getUid();
        String timestamp=modelShop.getTimestamp();
        String shopOpen = modelShop.getShopOpen();
        String state = modelShop.getState();
        String profileImage=modelShop.getProfileImage();
        String shopName = modelShop.getShopName();

        loadReviews(modelShop,holder); //load average rating, set to rating bar

        //set data
        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);
            //check if online
        if(online.equals("true")){
            //shop owner is online
            holder.onlineTv.setVisibility(View.VISIBLE);
        }else{
            //shop owner is offline
            holder.onlineTv.setVisibility(View.GONE);
        }
        //check if open
        if(shopOpen.equals("true")){
            //shop open
            holder.shopClosedTv.setVisibility(View.GONE);
        }else{
            //shop closed
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }
        try{
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_store_24).into(holder.shopIv);

        }catch(Exception e){
            holder.shopIv.setImageResource(R.drawable.ic_baseline_store_24);
        }
        //handle click listener. show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUID",uid);
                context.startActivity(intent);

            }
        });


    }

    private float ratingSum=0;
    private void loadReviews(ModelShop modelShop, final HolderShop holder) {

        String shopUid = modelShop.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data into it
                        ratingSum=0;
                        for(DataSnapshot ds:snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue()); //eg. 4.3
                            ratingSum=ratingSum+rating; //for avg rating add (addition) of all ratings, later will divide by number of reviews
                        }
                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating=ratingSum/numberOfReviews;

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return shopsList.size();//return number of records
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter=new FilterShopUser(this,filterList);
        }
        return filter;
    }

    //view holder
    class HolderShop extends RecyclerView.ViewHolder{

        //ui views of row_shop.xml
        private ImageView shopIv,onlineTv;
        private TextView shopClosedTv,shopNameTv,phoneTv,addressTv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            //init uid views
            shopIv=itemView.findViewById(R.id.shopIv);
            onlineTv=itemView.findViewById(R.id.onlineTv);
            shopClosedTv=itemView.findViewById(R.id.shopClosedTv);
            shopNameTv=itemView.findViewById(R.id.shopNameTv);
            phoneTv=itemView.findViewById(R.id.phoneTv);
            addressTv=itemView.findViewById(R.id.addressTv);
            ratingBar=itemView.findViewById(R.id.ratingBar);

        }
    }
}
