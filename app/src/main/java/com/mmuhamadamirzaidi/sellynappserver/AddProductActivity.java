package com.mmuhamadamirzaidi.sellynappserver;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mmuhamadamirzaidi.sellynappserver.Common.Common;
import com.mmuhamadamirzaidi.sellynappserver.Model.Category;
import com.mmuhamadamirzaidi.sellynappserver.Model.Product;

import java.util.UUID;

import dmax.dialog.SpotsDialog;

public class AddProductActivity extends AppCompatActivity {

    ImageView add_product_image, add_product_save, back;

    EditText add_product_name, add_product_price, add_product_discount, add_product_notification, add_product_description;

    Product newProduct;

    Uri saveUri;

    private AlertDialog dialog;

    FirebaseDatabase database;
    DatabaseReference product;

    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Get category Id
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");

        // Custom dialog
        dialog = new SpotsDialog.Builder().setContext(AddProductActivity.this).setTheme(R.style.Upload).build();

        // Init Firebase
        database = FirebaseDatabase.getInstance();
        product = database.getReference("Product");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        add_product_save = findViewById(R.id.add_product_save);
        back = findViewById(R.id.back);

        // Button to choose image
        add_product_image = findViewById(R.id.add_product_image);

        add_product_name = findViewById(R.id.add_product_name);
        add_product_price = findViewById(R.id.add_product_price);
        add_product_discount = findViewById(R.id.add_product_discount);
        add_product_notification = findViewById(R.id.add_product_notification);
        add_product_description = findViewById(R.id.add_product_description);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(AddProductActivity.this, ProductListActivity.class);
                startActivity(back);
            }
        });

        // Select image
        add_product_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
            }
        });

        // Save all product information
        add_product_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void addImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), Common.PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        dialog.show();

        final String add_product_name_ongoing = add_product_name.getText().toString().trim();
//        final String add_product_price_ongoing = add_product_price.getText().toString().trim();
//        final String add_product_discount_ongoing = add_product_discount.getText().toString().trim();
//        final String add_product_notification_ongoing = add_product_notification.getText().toString().trim();
//        final String add_product_description_ongoing = add_product_description.getText().toString().trim();

        if (saveUri != null){

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("products/"+imageName);

            if (add_product_name_ongoing.length() > 0) {
                imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        dialog.dismiss();

                        Toast.makeText(AddProductActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                        imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                dialog.dismiss();

                                //Set value for newCategory if image uploaded
//                                newProduct = new Product(add_product_name_ongoing, uri.toString(), add_product_description_ongoing,
//                                        add_product_price_ongoing, add_product_discount_ongoing, add_product_notification_ongoing, categoryId);

                                newProduct = new Product();

                                newProduct.setProductName(add_product_name.getText().toString().trim());
                                newProduct.setProductImage(uri.toString());
                                newProduct.setProductDescription(add_product_description.getText().toString().trim());
                                newProduct.setProductPrice(add_product_price.getText().toString().trim());
                                newProduct.setProductDiscount(add_product_discount.getText().toString().trim());
                                newProduct.setNotificationNo(add_product_notification.getText().toString().trim());
                                newProduct.setCategoryId(categoryId);

                                product.push().setValue(newProduct);
                                Toast.makeText(AddProductActivity.this, newProduct.getProductName() + " product was added!", Toast.LENGTH_SHORT).show();

                                SendUserToMainActivity();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();

                        Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                dialog.dismiss();
                Toast.makeText(AddProductActivity.this, "Please input product name!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            dialog.dismiss();
            Toast.makeText(AddProductActivity.this, "Please select product image!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            saveUri = data.getData();
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(AddProductActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
