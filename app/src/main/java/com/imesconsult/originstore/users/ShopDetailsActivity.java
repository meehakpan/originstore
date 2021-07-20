package com.imesconsult.originstore.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.imesconsult.originstore.Constants;
import com.imesconsult.originstore.R;
import com.imesconsult.originstore.adapters.AdapterCartItem;
import com.imesconsult.originstore.adapters.AdapterProductUser;
import com.imesconsult.originstore.models.ModelCartItem;
import com.imesconsult.originstore.models.ModelProduct;
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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    //declare ui views
    private ImageView shopIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, filteredProductTv, ExShopNameTv, cartCountTv;
    private ImageButton callBtn, mapBtn, cartBtn, backBtn, filterProductBtn, reviewsBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;



    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    public String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    public String deliveryFee;
    public String profileImage;



    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;


    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //init ui views
        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductTv = findViewById(R.id.filteredProductTv);
        productsRv = findViewById(R.id.productsRv);
        ExShopNameTv = findViewById(R.id.ExShopNameTv);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        cartCountTv = findViewById(R.id.cartCountTv);
        ratingBar = findViewById(R.id.ratingBar);

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        //get uid of the shop from intent
        shopUid = getIntent().getStringExtra("shopUID");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews(); //avg rating, set on rating bar

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //each shop has its own products and orders , so if user add items to cart and go back and open cart in different shop , then cart  should be different
        //so delete cart data whenever user open this activity
        deleteCartData();
        cartCount();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);

                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //before going back, check any products remaining in cart. if yes open dialog
                checkDb();


            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialog
                showCartDialog();

            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get seller items
                                String selected = Constants.productCategories1[which];
                                filteredProductTv.setText(selected);
                                if (selected.equals("All")) {
                                    //load all
                                    loadShopProducts();

                                } else {
                                    //load filtered
                                    adapterProductUser.getFilter().filter(selected);
                                }

                            }
                        }).show();

            }
        });
        //handle reviews button click, open reviews activity

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass shop uid to show its review
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });


    }

    private float ratingSum = 0;

    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data into it
                        ratingSum = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue()); //eg. 4.3
                            ratingSum = ratingSum + rating; //for avg rating add (addition) of all ratings, later will divide by number of reviews
                        }
                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum / numberOfReviews;

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkDb() {
        //init list
        cartItemList = new ArrayList<>();
        //inflate cart layout
        View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_existing_cart, null);
        //init views
        TextView subTotalTv = view1.findViewById(R.id.SubtotalTv);
        TextView ExShopNameTv = view1.findViewById(R.id.ExShopNameTv);
        ImageView exShopIv = view1.findViewById(R.id.exShopIv);
        TextView commentTxt = view1.findViewById(R.id.commentTxt);
        TextView delFeeTv = view1.findViewById(R.id.delFeeTv);
        TextView tAmountTv = view1.findViewById(R.id.tAmountTv);
        Button goToCartBtn = view1.findViewById(R.id.goToCartBtn);
        Button removeCartBtn = view1.findViewById(R.id.removeCartBtn);


        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view1);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCartItem modelCartItem = new ModelCartItem("" + id,
                    "" + pId,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity
            );
            cartItemList.add(modelCartItem);
        }
        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        ExShopNameTv.setText(shopName);
        subTotalTv.setText("$" + String.format("%.2f", allTotalPrice));
        delFeeTv.setText("$" + deliveryFee);
        tAmountTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", ""))));
        commentTxt.setText("Your cart exists with some product\n from the shop " + (shopName) + "\n Please confirm the order\n or\n remove products from Cart");

        try {
            Picasso.get().load(profileImage).into(exShopIv);

        } catch (Exception e) {

        }
        if (allTotalPrice > 0) {
            //show dialog
            final AlertDialog dialog = builder.create();
            dialog.show();

            //reset total price on dialog dismiss
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    allTotalPrice = 0.0;
                }
            });

            goToCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCartDialog();
                    dialog.dismiss();

                }
            });
            removeCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteCartData();
                    dialog.dismiss();
                    allTotalPrice = 0.0;

                }
            });
        } else {
            //cart is empty so , go back to shop list
            onBackPressed();
        }
    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();//delete all records from cart table
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            //no item in cart, hide cart count text view
            cartCountTv.setVisibility(View.GONE);
        } else {
            //have items in cart, hide cart count textView and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText("" + count);//concatenate with string , because we cant set integer to textView
        }


    }

    public void cartCount() {
        //keep it public , so we can access in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            //no item in cart, hide cart count text view
            cartCountTv.setVisibility(View.GONE);

        } else {
            //have item in cart, show cart count text view and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText("" + count);//concatenate with string , because we cant set integer value with TextView


        }
    }

    public double allTotalPrice = 0.00;    //need to access these views, so making public
    public  TextView sTotalTv, dFeeTv, totalTv, allTotalPriceTv, emptyCart,promoDescriptionTv,discountTv;//need to access these views, so making public
    public EditText promoCodeEt;
    public Button applyBtn;

    private void showCartDialog() {

        //Reset cart total
        allTotalPrice = 0.0;




        //init list
        cartItemList = new ArrayList<>();

        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //init views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        final Button checkoutBtn = view.findViewById(R.id.checkoutBtn);
        emptyCart = view.findViewById(R.id.emptyCart);
        totalTv = view.findViewById(R.id.totalTv);
        promoCodeEt = view.findViewById(R.id.promoCodeEt);
        promoDescriptionTv = view.findViewById(R.id.promoDescriptionTv);
        discountTv = view.findViewById(R.id.discountTv);
        applyBtn = view.findViewById(R.id.applyBtn);
        FloatingActionButton validateBtn=view.findViewById(R.id.validateBtn);

        if(cartItemList.size()==0){
            emptyCart.setVisibility(View.VISIBLE);

        }else{
            emptyCart.setVisibility(View.GONE);
        }




        //whenever cart dialog shows , check if promo code is applied or not
        if(isPromoCodeApplied){
            //applied
            promoDescriptionTv.setVisibility(View.VISIBLE);
            applyBtn.setVisibility(View.VISIBLE);
            applyBtn.setText("Applied");
            promoCodeEt.setText(promoCode);
            promoDescriptionTv.setText(promoDescription);
        }else{
            //not applied
            promoDescriptionTv.setVisibility(View.GONE);
            applyBtn.setVisibility(View.GONE);
            applyBtn.setText("Apply");
        }



        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);



        shopNameTv.setText(shopName);
        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);


            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCartItem modelCartItem = new ModelCartItem("" + id,
                    "" + pId,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity
            );




            cartItemList.add(modelCartItem);

            if (cartItemList.size() > 0) {
                //cart is not empty
                emptyCart.setVisibility(View.INVISIBLE);
            }
        }

        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        //set to recycler view
        cartItemsRv.setAdapter(adapterCartItem);

        if (isPromoCodeApplied) {
            priceWithDiscount();
        }else{
            priceWithoutDiscount();
        }




        //show dialog
        final AlertDialog dialog = builder.create();
        dialog.show();






        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.0;
            }
        });

        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if (myLatitude.equals("") || myLatitude.equals(null) || myLongitude.equals("") || myLongitude.equals(null)) {
                    //user didn't enter address in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if (myPhone.equals("") || myPhone.equals(null)) {
                    //user didn't enter phone number in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone number in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if (cartItemList.size() == 0) {
                    //cart list is empty
                    //refresh cart to show all the price zero
                    Toast.makeText(ShopDetailsActivity.this, "Your cart is empty...", Toast.LENGTH_SHORT).show();
                    emptyCart.setVisibility(View.VISIBLE);
                    return;//don't proceed further
                }
                /*dialog.dismiss();*/
                checkoutBtn.setEnabled(false);
                submitOrder();
            }
        });

        //start validating promo code when validate button is pressed
        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Flow
                * 1) get code from EditText
                        If not empty: promotion may be applied, other wise no promotion
                * 2) Check is code is valid ie. Available id seller's promotion db
                *       if available: promotion may be applied, otherwise no promotion
                * 3) Check if expired or not
                *       if not expired: promotion may be applied, otherwise no promotion
                * 4) Check if minimum order price
                *       if minimum order price is >= subtotal price : promotion available , otherwise no promotion*/

                final String promotionCode =promoCodeEt.getText().toString().trim();
                if(TextUtils.isEmpty(promotionCode)){
                    Toast.makeText(ShopDetailsActivity.this, "Please enter promo code...", Toast.LENGTH_SHORT).show();
                }else {
                    checkCodeAvailability(promotionCode);
                }
            }
        });

        //apply if code valid, no need to check if valid or not, because this button will be visible only if code is valid
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPromoCodeApplied=true;
                applyBtn.setText("Applied");
                priceWithDiscount();
            }
        });
    }




    private void priceWithDiscount(){
        discountTv.setText("$"+promoPrice);
        dFeeTv.setText("$"+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$"+(allTotalPrice+Double.parseDouble(deliveryFee.replace("$",""))-Double.parseDouble(promoPrice)));
    }
    private void priceWithoutDiscount() {
        discountTv.setText("$0");
        dFeeTv.setText("$"+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$"+(allTotalPrice+Double.parseDouble(deliveryFee.replace("$",""))));

    }


    public boolean isPromoCodeApplied=false;
    public String promoId,promoTimestamp,promoCode,promoDescription,promoExpDate,promoMinimumOrderPrice,promoPrice;
    private void checkCodeAvailability(String promotionCode){
        //progress bar
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Checking Promo Code...");
        progressDialog.setCanceledOnTouchOutside(false);

        //promo is not applied yet
        isPromoCodeApplied=false;
        applyBtn.setText("Apply");
        priceWithoutDiscount();

        //check promo code availability
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Promotions").orderByChild("promoCode").equalTo(promotionCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //check if promo code exists
                        if(snapshot.exists()){

                            //promo code exists
                            progressDialog.dismiss();
                            for(DataSnapshot ds:snapshot.getChildren()){
                                promoId=""+ds.child("id").getValue();
                                promoTimestamp=""+ds.child("timestamp").getValue();
                                promoCode=""+ds.child("promoCode").getValue();
                                promoDescription=""+ds.child("description").getValue();
                                promoMinimumOrderPrice=""+ds.child("minimumOrderPrice").getValue();
                                promoPrice=""+ds.child("promoPrice").getValue();
                                promoExpDate=""+ds.child("expireDate").getValue();



                                //now check if code is expired on not
                                checkCodeExpireDate();
                            }

                        }else{

                            //entered promo code doesn't exists
                            progressDialog.dismiss();
                            Toast.makeText(ShopDetailsActivity.this, "Invalid Promo Code", Toast.LENGTH_SHORT).show();
                            applyBtn.setVisibility(View.GONE);
                            promoDescriptionTv.setVisibility(View.GONE);
                            promoDescriptionTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkCodeExpireDate() {
        //get current date
        Calendar calendar=Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final String todayDate = day+"/"+month+"/"+year; //eg. 25/07/2020

        //Check for expiry date...
        try{
            SimpleDateFormat sdFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = sdFormat.parse(todayDate); //date 2
            Date expireDate = sdFormat.parse(promoExpDate); //date 1

            //compare date

            if(expireDate.compareTo(currentDate)>0){
                //date 1 occurs after date 2 (ie. not expire date)
                checkMinimumOrderPrice();

                } else if(expireDate.compareTo(currentDate)<0){
                //date 1 occurs before date 2 (ie. expired)
                Toast.makeText(this, "The Promotion Code is expired on "+promoExpDate, Toast.LENGTH_SHORT).show();
                applyBtn.setVisibility(View.GONE);
                promoDescriptionTv.setVisibility(View.GONE);
                promoDescriptionTv.setText("");
            }else if(expireDate.compareTo(currentDate)==0){
                //both dates are equal , ie. not expired
                checkMinimumOrderPrice();
            }

        }catch (Exception e){
            //if anything goes wrong exception while comparing current date and expired date
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        }
    }

    private void checkMinimumOrderPrice() {
        //each promo code have minimum price requirements. if order price is less than required then don't allow to apply code

        if(Double.parseDouble(String.format("%.2f",allTotalPrice))<Double.parseDouble(promoMinimumOrderPrice)){
            //current order price is less than minimum order price required by promo code, so don't allow to apply
            Toast.makeText(this, "This code is for order with minimum amount: $"+promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        }else {
            //current order price is equal to or greater than minimum order price required for Promo code. so allow to apply
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);

        }



    }


    private void submitOrder() {

        //show progress dialog

        progressDialog.setMessage("Placing order...");
        progressDialog.show();

        //order id and order time
        final String timestamp = "" + System.currentTimeMillis();
        String cost = allTotalPriceTv.getText().toString().trim().replace("$", "");//remove $ if contains

        //add latitude , longitude of user to each order | delete previous orders from firebase or add manually to them

        //set up order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderTime", "" + timestamp);
        hashMap.put("orderStatus", "In Progress");//In progress / completed / cancelled
        hashMap.put("orderCost", "" + cost);
        hashMap.put("deliveryFee", "" + deliveryFee); //include delivery fee in each order
        hashMap.put("orderBy", "" + firebaseAuth.getUid());
        hashMap.put("orderTo", "" + shopUid);
        hashMap.put("latitude", "" + myLatitude);
        hashMap.put("longitude", "" + myLongitude);
        if(isPromoCodeApplied=true) {
            //promo applied
            hashMap.put("discount", "" + promoPrice); // include promo price
        }else{
            //promo not applied. include price 0
            hashMap.put("discount", "0"); // without promo price
        }

        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //order info added, now add order items
                        for (int i = 0; i < cartItemList.size(); i++) {
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        /*deleteCartData();
                        showCartDialog();*/
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order placed successfully", Toast.LENGTH_LONG).show();

                        prepareNotificationMessage(timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
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
                            myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                String name = "" + snapshot.child("name").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopPhone = "" + snapshot.child("phone").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longitude").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();

                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: $" + deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true")) {
                    openCloseTv.setText("Open");
                } else {
                    openCloseTv.setText("Closed");
                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);

                } catch (Exception e) {

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {
        //init list
        productsList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding items
                        productsList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productsList);
                        //set adapter
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void prepareNotificationMessage(String orderId) {
        //when user places order , send notification to seller

        //prepare data for notification

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order " + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new order.";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid()); //since we are logged in as a buyer to place order, so current user id is buyer Uid
            notificationBodyJo.put("sellerUid", shopUid);
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo);

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start order details activity

                //open order details, we need key there, orderId, orderTo
                //open order details. we need to keys there, order Id, order to
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);//get these values through intent on OrderDetailUsesActivity
                finish();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm, still start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);//get these values through intent on OrderDetailUsesActivity
                finish();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String>headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key="+Constants.FCM_KEY);
                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);


    }


}