package com.imesconsult.originstore.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imesconsult.originstore.R;
import com.imesconsult.originstore.models.ModelCartItem;
import com.imesconsult.originstore.users.ShopDetailsActivity;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {
    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout of row_cartitem.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem,parent,false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderCartItem holder, final int position) {
        //get data
        ModelCartItem modelCartItem=cartItems.get(position);
        final String id = modelCartItem.getId();
        String getPid = modelCartItem.getpId();
        String title = modelCartItem.getName();
        final String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantityTv.setText("["+quantity+"]");//e.g [3]
        holder.itemPriceEachTv.setText(""+price);

        //handle remove click listener , delete item from cart

        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //will create table if not exists, but in this case table is exists
                EasyDB easyDB=EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                        .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                        .doneTableColumn();
                easyDB.deleteRow(1,id);
                Toast.makeText(context, "Removed from Cart...", Toast.LENGTH_SHORT).show();


                //refresh list...
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                //adjust the subtotal after product remove
                double subTotalWithoutDiscount=((ShopDetailsActivity)context).allTotalPrice;
                double subTotalAfterProductRemove = subTotalWithoutDiscount-Double.parseDouble(cost.replace("$",""));
                ((ShopDetailsActivity)context).allTotalPrice=subTotalAfterProductRemove;
                ((ShopDetailsActivity)context).sTotalTv.setText("$"+String.format("%.2f",((ShopDetailsActivity)context).allTotalPrice));



                //once subtotal is updated....check minimum order price of promo code
               /* Double promoPrice = Double.parseDouble(((ShopDetailsActivity)context).promoPrice);*/
                double deliveryFee =Double.parseDouble(((ShopDetailsActivity)context).deliveryFee.replace("$",""));
                if(cartItems.size()==0){
                    deliveryFee=0;
                    ((ShopDetailsActivity)context).dFeeTv.setText("$0");
                    ((ShopDetailsActivity)context).emptyCart.setVisibility(View.VISIBLE);

                }

                //check if promo code applied
                if(((ShopDetailsActivity)context).isPromoCodeApplied){
                    //applied
                    if (subTotalAfterProductRemove<Double.parseDouble(((ShopDetailsActivity)context).promoMinimumOrderPrice)){
                        //current order price is less than minimum required price
                        Toast.makeText(context, "This code is valid for order with minimum amount: $"+((ShopDetailsActivity)context).promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText("");
                        ((ShopDetailsActivity)context).discountTv.setText("");
                        ((ShopDetailsActivity)context).isPromoCodeApplied=false;

                        //show now net total after delivery
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterProductRemove+deliveryFee))));

                    }else{
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText(((ShopDetailsActivity)context).promoDescription);
                        //show new total after adding delivery fee and subtracting promo fee
                        ((ShopDetailsActivity)context).isPromoCodeApplied=true;
                        Double promoPrice = Double.parseDouble(((ShopDetailsActivity)context).promoPrice);
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterProductRemove+deliveryFee-promoPrice))));
                    }
                }else{
                    //not applied
                 ((ShopDetailsActivity)context).allTotalPriceTv.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterProductRemove+deliveryFee))));



                }

                //after removing items from cart , update cart count

                ((ShopDetailsActivity)context).cartCount();

            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size(); //return number of records
    }

    //View holder class
    class HolderCartItem extends RecyclerView.ViewHolder{

        //ui views of row_cartitem.xml
        private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv;
        private ImageButton itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);
            //init views
            itemTitleTv=itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv=itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv=itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv=itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv=itemView.findViewById(R.id.itemRemoveTv);
        }
    }
}
