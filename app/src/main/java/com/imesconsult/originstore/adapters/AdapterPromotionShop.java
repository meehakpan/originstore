package com.imesconsult.originstore.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imesconsult.originstore.R;
import com.imesconsult.originstore.models.ModelPromotion;
import com.imesconsult.originstore.sellers.AddPromotionCodesActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterPromotionShop extends RecyclerView.Adapter<AdapterPromotionShop.HolderPromotionShop> {

    private Context context;
    private ArrayList<ModelPromotion> promotionArrayList;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    public AdapterPromotionShop(Context context, ArrayList<ModelPromotion> promotionArrayList) {
        this.context = context;
        this.promotionArrayList = promotionArrayList;

        progressDialog=new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderPromotionShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_promotion_xml
        View view= LayoutInflater.from(context).inflate(R.layout.row_promotion_shop,parent,false);
        return new HolderPromotionShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderPromotionShop holder, int position) {
        //get Data
        final ModelPromotion modelPromotion = promotionArrayList.get(position);
        String id = modelPromotion.getId();
        String timestamp = modelPromotion.getTimestamp();
        String description = modelPromotion.getDescription();
        String promoCode = modelPromotion.getPromoCode();
        String promoPrice = modelPromotion.getPromoPrice();
        String expireDate = modelPromotion.getExpireDate();
        String minimumOrderPrice = modelPromotion.getMinimumOrderPrice();

        //set data

        holder.descriptionTv.setText(description);
        holder.promotionPriceTv.setText(promoPrice);
        holder.minimumOrderPriceTv.setText(minimumOrderPrice);
        holder.promoCodeTv.setText("Code: "+promoCode);
        holder.expireDateTv.setText("Expire Date: "+expireDate);

        //handle click : show Edit, Delete dialog
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDeleteDialog(modelPromotion,holder);

            }
        });



    }

    private void editDeleteDialog(final ModelPromotion modelPromotion, HolderPromotionShop holder) {
        //Options to display in dialog
        String[] options ={"Edit", "Delete"};
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if(which==0){
                            //edit clicked
                            editPromoCode(modelPromotion);
                        }else if(which==1){
                            //delete clicked
                            deletePromoDialog(modelPromotion);

                        }

                    }
                }).show();


    }

    private void deletePromoDialog(ModelPromotion modelPromotion) {
        //show progress Dialog
        progressDialog.setMessage("Deleting Promotion Code...");
        progressDialog.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(modelPromotion.getId())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //deleted
                        progressDialog.dismiss();
                        Toast.makeText(context, "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to delete
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void editPromoCode(ModelPromotion modelPromotion) {
        //start and add data to AddPromotionCodeActivity to edit
        Intent intent = new Intent(context, AddPromotionCodesActivity.class);
        intent.putExtra("promoId",modelPromotion.getId()); //will use id to edit promotion code
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return promotionArrayList.size();
    }

    //view holder class
    class HolderPromotionShop extends RecyclerView.ViewHolder{

        //views of row_promotion_shop.xml
        private ImageView iconTv;
        private TextView promoCodeTv,promotionPriceTv,minimumOrderPriceTv,expireDateTv,descriptionTv;


        public HolderPromotionShop(@NonNull View itemView) {
            super(itemView);

            //init view
            iconTv=itemView.findViewById(R.id.iconTv);
            promoCodeTv=itemView.findViewById(R.id.promoCodeTv);
            promotionPriceTv=itemView.findViewById(R.id.promotionPriceTv);
            minimumOrderPriceTv=itemView.findViewById(R.id.minimumOrderPriceTv);
            expireDateTv=itemView.findViewById(R.id.expireDateTv);
            descriptionTv=itemView.findViewById(R.id.descriptionTv);

        }
    }

}
