package com.mmuhamadamirzaidi.sellynappserver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mmuhamadamirzaidi.sellynappserver.Common.Common;
import com.mmuhamadamirzaidi.sellynappserver.Interface.ItemClickListener;
import com.mmuhamadamirzaidi.sellynappserver.Model.Category;
import com.mmuhamadamirzaidi.sellynappserver.Model.Product;
import com.mmuhamadamirzaidi.sellynappserver.ViewHolder.ProductViewHolder;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import dmax.dialog.SpotsDialog;

public class ProductListActivity extends AppCompatActivity {

    RecyclerView recycler_product;
    RecyclerView.LayoutManager layoutManager;

    ImageView add_product;

    FirebaseDatabase database;
    DatabaseReference product;

    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId="";

    Uri saveUri;

    SwipeRefreshLayout swipe_layout_product_list;

    FirebaseRecyclerAdapter<Product, ProductViewHolder> adapter;

    AlertDialog dialog;

    Dialog updateDialog, updateLoadingDialog;

    EditText update_product_name, update_product_price, update_product_discount, update_product_notification, update_product_description;
    ImageView select_image, update_product_image;

    Button button_update, button_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Init Firebase
        database = FirebaseDatabase.getInstance();
        product = database.getReference("Product");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Custom dialog
        updateLoadingDialog = new SpotsDialog.Builder().setContext(ProductListActivity.this).setTheme(R.style.Update).build();
        dialog = new SpotsDialog.Builder().setContext(ProductListActivity.this).setTheme(R.style.Upload).build();

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
                viewHolder.product_notification.setText(model.getNotificationNo());

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

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {

            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {

            deleteProduct(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(final String key, final Product item) {

        updateDialog = new Dialog(ProductListActivity.this);
        updateDialog.setCanceledOnTouchOutside(false);
        updateDialog.setContentView(R.layout.dialog_activity_update_product);

        select_image = updateDialog.findViewById(R.id.select_image);
        update_product_image = updateDialog.findViewById(R.id.update_product_image);

        update_product_name = updateDialog.findViewById(R.id.update_product_name);
        update_product_price = updateDialog.findViewById(R.id.update_product_price);
        update_product_discount = updateDialog.findViewById(R.id.update_product_discount);
        update_product_notification = updateDialog.findViewById(R.id.update_product_notification);
        update_product_description = updateDialog.findViewById(R.id.update_product_description);

        button_update = updateDialog.findViewById(R.id.button_update);
        button_cancel = updateDialog.findViewById(R.id.button_cancel);

        // Set original value
        update_product_name.setText(item.getProductName());
        update_product_price.setText(item.getProductPrice());
        update_product_discount.setText(item.getProductDiscount());
        update_product_notification.setText(item.getNotificationNo());
        update_product_description.setText(item.getProductDescription());

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
            }
        });

        update_product_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadNewImage(item);
            }
        });

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateLoadingDialog.show();
                item.setProductName(update_product_name.getText().toString().trim());
                item.setProductPrice(update_product_price.getText().toString().trim());
                item.setProductDiscount(update_product_discount.getText().toString().trim());
                item.setNotificationNo(update_product_notification.getText().toString().trim());
                item.setProductDescription(update_product_description.getText().toString().trim());

                product.child(key).setValue(item);

                updateLoadingDialog.dismiss();
                updateDialog.cancel();
                Toast.makeText(ProductListActivity.this, item.getProductName() + " product was updated!", Toast.LENGTH_SHORT).show();
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog.cancel();
            }
        });

        updateDialog.show();

    }

    private void uploadNewImage(final Product item) {
        dialog.show();

        if (saveUri != null) {

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("products/" + imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();

                    Toast.makeText(ProductListActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            dialog.dismiss();

                            // Update new image info and upload link
                            item.setProductImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();

                    updateDialog.cancel();
                    Toast.makeText(ProductListActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            dialog.dismiss();
            Toast.makeText(ProductListActivity.this, "Please select product image!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), Common.PICK_IMAGE_REQUEST);

    }

    private void deleteProduct(String key) {
        product.child(key).removeValue();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            saveUri = data.getData();
        }
    }
}
