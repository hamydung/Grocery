package com.blogspot.atifsoftwares.grocery.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.grocery.adapters.AdapterOrderShop;
import com.blogspot.atifsoftwares.grocery.adapters.AdapterProductSeller;
import com.blogspot.atifsoftwares.grocery.Constants;
import com.blogspot.atifsoftwares.grocery.modles.ModelOrderShop;
import com.blogspot.atifsoftwares.grocery.modles.ModelProduct;
import com.blogspot.atifsoftwares.grocery.R;
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

public class MainSellerActivity extends AppCompatActivity {
    private TextView nameTv, shopNameTv, emailTv, tabsProductsTv, tabsOrdersTv, filterProductTv, filteredOrdersTv;
    private EditText searchProductEt;
    private ImageButton logoutBtn, editProfileBtn, addProductBtn, filterProductBtn, filterOrderBtn, reviewsBtn, settingsBtn;
    private ImageView profileIv;
    private RecyclerView productsRv, ordersRv;
    private RelativeLayout productsRl, ordersRl;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameTv = findViewById(R.id.nameTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        tabsProductsTv = findViewById(R.id.tabsProductsTv);
        filterProductTv = findViewById(R.id.filterProductTv);
        tabsOrdersTv = findViewById(R.id.tabsOrdersTv);
        searchProductEt = findViewById(R.id.searchProductEt);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        profileIv = findViewById(R.id.profileIv);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        productsRv = findViewById(R.id.productsRv);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        ordersRv = findViewById(R.id.ordersRv);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth =FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();

        showProductsUI();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                try {
                    adapterProductSeller.getFilter().filter(s);

                }
                catch (Exception e) {
                    e.printStackTrace();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make offline
                //sign out
                //go to login activity
                makeMeOffLine();

            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit profile activity
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit add product activity
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
            }
        });

        tabsProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load products
                showProductsUI();

            }
        });

        tabsOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load orders
                showOrdersUI();

            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //get selected item
                                String selected = Constants.productCategories1[which];
                                filterProductTv.setText(selected);
                                if (selected.equals("All")){
                                    //load all
                                    loadAllProducts();
                                }
                                else {
                                    //load filtered
                                    loadFilteredProducts(selected);
                                }

                            }
                        })
                        .show();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //options to display in dialog
                final String[] options = {"All", "In Progress", "Completed", "Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders: ")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //handle item clicked
                                if (which == 0) {
                                    //All clicked
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter(""); //show all orders
                                }
                                else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing" + optionClicked + "Orders");
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        })
                        .show();
            }
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open same reviews activity as used in user main page
                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", ""+firebaseAuth.getUid());
                startActivity(intent);

            }
        });

        //start settings screen
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class));
            }
        });

    }

    private void loadAllOrders() {
        //init array list
        orderShopArrayList = new ArrayList<>();

        //load orders of shop
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding new data in it
                        orderShopArrayList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            //add to list
                            orderShopArrayList.add(modelOrderShop);
                        }
                        //setup adapter
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, orderShopArrayList);
                        //set adapter to recyclerview
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();

                             //if selected category matches product category then add in list
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }

                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void showProductsUI() {
        //show products ui and hide orders ui
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabsProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabsProductsTv.setBackgroundResource(R.drawable.shape_rec04);

        tabsOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabsOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders ui and hide products ui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabsProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabsProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabsOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabsOrdersTv.setBackgroundColor(R.drawable.shape_rec04);
    }

    private void makeMeOffLine() {
        //after logging in, make user online
        progressDialog.setMessage("Logging Out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        }
        else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //get data from db
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();

                            //set data to ui
                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                            }
                            catch (Exception e) {
                                profileIv.setImageResource(R.drawable.ic_store_gray);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}