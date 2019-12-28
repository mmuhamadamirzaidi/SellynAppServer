package com.mmuhamadamirzaidi.sellynappserver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mmuhamadamirzaidi.sellynappserver.Common.Common;
import com.mmuhamadamirzaidi.sellynappserver.Interface.ItemClickListener;
import com.mmuhamadamirzaidi.sellynappserver.Model.Product;
import com.mmuhamadamirzaidi.sellynappserver.ViewHolder.ProductViewHolder;
import com.squareup.picasso.Picasso;

public class ProductListActivity extends AppCompatActivity {

    RecyclerView recycler_product;
    RecyclerView.LayoutManager layoutManager;

    ImageView add_product;

    FirebaseDatabase database;
    DatabaseReference product;

    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId="";

    SwipeRefreshLayout swipe_layout_product_list;

    FirebaseRecyclerAdapter<Product, ProductViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Init Firebase
        database = FirebaseDatabase.getInstance();
        product = database.getReference("Product");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Init recyclerview
        recycler_product = (RecyclerView) findViewById(R.id.recycler_product);
//        recycler_product.setHasFixedSize(true); //Need to remove if using Firebase Recycler Adapter/Disable for API 19 and below
        layoutManager = new LinearLayoutManager(this);
        recycler_product.setLayoutManager(layoutManager);



        swipe_layout_product_list = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_product_list);
        swipe_layout_product_list.setColorSchemeResources(R.color.colorPrimaryDark);

        swipe_layout_product_list.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Get CategoryId intent
                if (getIntent() != null){
                    categoryId = getIntent().getStringExtra("categoryId");
                }
                if (!categoryId.isEmpty() && categoryId != null){

                    if (Common.isConnectedToInternet(getBaseContext())){
                        loadProduct(categoryId);
                    }
                    else{
                        Toast.makeText(ProductListActivity.this, "Please check Internet connection!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        swipe_layout_product_list.post(new Runnable() {
            @Override
            public void run() {
                // Get CategoryId intent
                if (getIntent() != null){
                    categoryId = getIntent().getStringExtra("categoryId");
                }
                if (!categoryId.isEmpty() && categoryId != null){

                    if (Common.isConnectedToInternet(getBaseContext())){
                        loadProduct(categoryId);
                    }
                    else{
                        Toast.makeText(ProductListActivity.this, "Please check Internet connection!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        add_product = findViewById(R.id.add_product);

        add_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(ProductListActivity.this, AddProductActivity.class);
                mainIntent.putExtra("categoryId", categoryId);
                startActivity(mainIntent);
            }
        });

    }

    private void loadProduct(String categoryId) {

        Query searchByName = product.orderByChild("categoryId").equalTo(categoryId);

        FirebaseRecyclerOptions<Product> productOptions = new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(searchByName, Product.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(productOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ProductViewHolder viewHolder, final int position, @NonNull final Product model) {

                viewHolder.product_name.setText(model.getProductName());
//                viewHolder.product_notification.setText(model.getNotificationNo());

                Picasso.with(getBaseContext()).load(model.getProductImage()).into(viewHolder.product_image);

                final Product clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Toast.makeText(ProductListActivity.this, "Product Name: "+clickItem.getProductName()+". Notification No: "+clickItem.getNotificationNo(), Toast.LENGTH_SHORT).show();


//                        Intent product_detail = new Intent(ProductListActivity.this, AddProductActivity.class);
//                        product_detail.putExtra("productId", adapter.getRef(position).getKey());
////                        product_detail.putExtra("notificationNo", model.getNotificationNo());
//                        startActivity(product_detail);
                    }
                });
            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_products, viewGroup, false);
                return new ProductViewHolder(itemView);
            }
        };

        adapter.startListening();
        recycler_product.setAdapter(adapter);
        swipe_layout_product_list.setRefreshing(false);
    }
}
