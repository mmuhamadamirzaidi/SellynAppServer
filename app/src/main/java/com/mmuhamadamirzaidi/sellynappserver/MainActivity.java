package com.mmuhamadamirzaidi.sellynappserver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mmuhamadamirzaidi.sellynappserver.Common.Common;
import com.mmuhamadamirzaidi.sellynappserver.Interface.ItemClickListener;
import com.mmuhamadamirzaidi.sellynappserver.Model.Category;
import com.mmuhamadamirzaidi.sellynappserver.ViewHolder.CategoryViewHolder;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    FirebaseStorage storage;
    StorageReference storageReference;

    Category newCategory;

    Uri saveUri;
    private final int PICK_IMAGE_REQUEST = 71;

    ImageView header_profile_image, select_image;

    TextView header_fullname, header_identity_card;

    RecyclerView recycler_category;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category, CategoryViewHolder> adapter;

    SwipeRefreshLayout swipe_layout_category;

    EditText add_category_name;

    ImageView add_image_category;

    FloatingActionButton add_category;

    Button button_update, button_cancel;

    AlertDialog dialog;

    Dialog updateDialog, updateLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.category_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // Custom dialog
        updateLoadingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).setTheme(R.style.Update).build();
        dialog = new SpotsDialog.Builder().setContext(MainActivity.this).setTheme(R.style.Upload).build();

        // Swipe Layout
        swipe_layout_category = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_category);
        swipe_layout_category.setColorSchemeResources(R.color.colorPrimaryDark);

        // Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Swipe Layout
        swipe_layout_category = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_category);
        swipe_layout_category.setColorSchemeResources(R.color.colorPrimaryDark);

        swipe_layout_category.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadCategory();
                } else {
                    Toast.makeText(getBaseContext(), "Please check Internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Default, load for first time
        swipe_layout_category.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadCategory();
                } else {
                    Toast.makeText(getBaseContext(), "Please check Internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        add_category = findViewById(R.id.add_category);
        add_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(MainActivity.this, AddCategoryActivity.class);
                startActivity(mainIntent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        // Set user informations
        View headerView = navigationView.getHeaderView(0);
        header_fullname = (TextView) headerView.findViewById(R.id.header_fullname);
        header_identity_card = (TextView) headerView.findViewById(R.id.header_identity_card);
        header_profile_image = (ImageView) headerView.findViewById(R.id.header_profile_image);

        // Set Information
        header_fullname.setText(Common.currentUser.getUserName());
        header_identity_card.setText(Common.currentUser.getUserIdentityCard());

        // Set image
        Picasso.with(getBaseContext()).load(Common.currentUser.getUserImage()).into(header_profile_image);

        // Load category
        recycler_category = (RecyclerView) findViewById(R.id.recycler_category);
//        recycler_category.setHasFixedSize(true); //Need to remove if using Firebase Recycler Adapter/Disable for API 19 and below
        layoutManager = new LinearLayoutManager(this);
        recycler_category.setLayoutManager(layoutManager);
    }

    private void loadCategory() {

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, CategoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CategoryViewHolder viewHolder, int position, @NonNull Category model) {

                viewHolder.category_name.setText(model.getName());

                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.category_image);

                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        // Get CategoryId and send to new activity
                        Intent product_id = new Intent(MainActivity.this, ProductListActivity.class);
                        product_id.putExtra("categoryId", adapter.getRef(position).getKey());
                        startActivity(product_id);

                        Toast.makeText(MainActivity.this, "Category ID" + adapter.getRef(position).getKey(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_category, viewGroup, false);
                return new CategoryViewHolder(itemView);
            }
        };
        adapter.startListening();
        recycler_category.setAdapter(adapter);
        swipe_layout_category.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        if (item.getItemId() == R.id.refresh){
//            loadCategory();
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            Intent menuIntent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(menuIntent);

        } else if (id == R.id.nav_cart) {

            Toast.makeText(MainActivity.this, "News", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_wishlist) {

            Toast.makeText(MainActivity.this, "Wishlist", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.news) {

            Toast.makeText(MainActivity.this, "News", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_account) {

            Toast.makeText(MainActivity.this, "News", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_settings) {

            Toast.makeText(MainActivity.this, "News", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_sign_out) {

            //Forget user information
            Paper.book().destroy();

            Intent menuIntent = new Intent(MainActivity.this, SignInActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(menuIntent);
            Toast.makeText(MainActivity.this, "Sign out successfully", Toast.LENGTH_SHORT).show();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {

            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {

            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {
        category.child(key).removeValue();
    }

    private void showUpdateDialog(final String key, final Category item) {

        updateDialog = new Dialog(MainActivity.this);
        updateDialog.setCanceledOnTouchOutside(false);
        updateDialog.setContentView(R.layout.dialog_activity_update_category);

        add_image_category = updateDialog.findViewById(R.id.add_image_category);

        add_category_name = updateDialog.findViewById(R.id.add_category_name);


        select_image = updateDialog.findViewById(R.id.select_image);

        button_update = updateDialog.findViewById(R.id.button_update);
        button_cancel = updateDialog.findViewById(R.id.button_cancel);

        // Set original value
        add_category_name.setText(item.getName());

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
            }
        });

        add_image_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadNewImage(item);
            }
        });

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateLoadingDialog.show();
                item.setName(add_category_name.getText().toString().trim());
                category.child(key).setValue(item);

                updateLoadingDialog.dismiss();
                updateDialog.cancel();
                Toast.makeText(MainActivity.this, item.getName() + " category was updated!", Toast.LENGTH_SHORT).show();
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

    private void uploadNewImage(final Category item) {
        dialog.show();

        if (saveUri != null) {

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();

                    Toast.makeText(MainActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            dialog.dismiss();

                            // Update new image info and upload link
                            item.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();

                    updateDialog.cancel();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            dialog.dismiss();
            Toast.makeText(MainActivity.this, "Please select category image!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            saveUri = data.getData();
        }
    }
}
