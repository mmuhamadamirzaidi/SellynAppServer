package com.mmuhamadamirzaidi.sellynappserver;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mmuhamadamirzaidi.sellynappserver.Common.Common;
import com.mmuhamadamirzaidi.sellynappserver.Model.User;

import dmax.dialog.SpotsDialog;

public class SignInActivity extends AppCompatActivity {

    private EditText sign_in_phone, sign_in_password;
    private Button button_sign_in, button_account_sign_up;

    private CheckBox sign_in_remember_me;

    private AlertDialog dialog, dialog_loading;

    FirebaseDatabase database;
    DatabaseReference table_user;

    private TextView sign_in_forget_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        sign_in_phone = findViewById(R.id.sign_in_phone);
        sign_in_password = findViewById(R.id.sign_in_password);

        button_sign_in = findViewById(R.id.button_sign_in);
        button_account_sign_up = findViewById(R.id.button_account_sign_up);

        sign_in_remember_me = findViewById(R.id.sign_in_remember_me);

        sign_in_forget_password = findViewById(R.id.sign_in_forget_password);

        sign_in_remember_me.setTypeface(ResourcesCompat.getFont(getBaseContext(), R.font.mr));

        // Custom dialog
        dialog = new SpotsDialog.Builder().setContext(SignInActivity.this).setTheme(R.style.SignIn).build();
        dialog_loading = new SpotsDialog.Builder().setContext(SignInActivity.this).setTheme(R.style.Loading).build();

        // Init Firebase
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

        button_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInServer(sign_in_phone.getText().toString().trim(), sign_in_password.getText().toString().trim());
            }
        });
    }

    private void SignInServer(String phone, String password) {
        dialog.show();

        final String localPhone = phone;
        final String localPassword = password;

        table_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(localPhone).exists()){
                    dialog.dismiss();

                    User user = dataSnapshot.child(localPhone).getValue(User.class);
                    user.setUserPhone(localPhone);

                    if (Boolean.parseBoolean(user.getIsStaff())){
                        if (user.getUserPassword().equals(localPassword)){
                            dialog.dismiss();

                            Toast.makeText(SignInActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();

                            Common.currentUser = user;
                            SendUserToMainActivity();
                        }
                        else{
                            dialog.dismiss();
                            Toast.makeText(SignInActivity.this, "Wrong password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        dialog.dismiss();
                        Toast.makeText(SignInActivity.this, "Please use staff account!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    dialog.dismiss();
                    Toast.makeText(SignInActivity.this, "User don't exist in system!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
