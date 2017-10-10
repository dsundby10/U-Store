package accountlogin.registrationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainMenu extends AppCompatActivity {
    private Button add_inv_btn, edit_inv_btn, search_inv_btn, add_shelving_btn,
            add_dept_btn, edit_layout_btn, view_layout_btn, logout_btn, acct_info_btn;
    private TextView dislayMessage;

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Intent intent = getIntent();

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        dislayMessage = (TextView)findViewById(R.id.displayMessage);
        setDataToView(user);

        add_inv_btn = (Button)findViewById(R.id.add_inv_btn);
        edit_inv_btn = (Button)findViewById(R.id.edit_inv_btn);
        search_inv_btn = (Button)findViewById(R.id.search_inv_btn);
        add_shelving_btn = (Button)findViewById(R.id.add_shelving_btn);
        add_dept_btn = (Button)findViewById(R.id.add_dept_btn);
        edit_layout_btn = (Button)findViewById(R.id.edit_layout_btn);
        view_layout_btn = (Button)findViewById(R.id.view_layout_btn);
        logout_btn = (Button) findViewById(R.id.logout_btn);
        acct_info_btn = (Button) findViewById(R.id.acct_info_btn);

        add_inv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, AddInventory.class);
                startActivity(intent);
            }
        });

        add_dept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, EditStoreAndDepartmentActivity.class);
                startActivity(intent);
            }
        });

        add_shelving_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Will more than likely be a different class for this intent
                Intent intent = new Intent(MainMenu.this, ShelvingSetup.class);
                startActivity(intent);
            }
        });



        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, LoginActivity.class);
                startActivity(intent);
            }
        });


    }
    @SuppressLint("SetTextI18n")
    private void setDataToView(FirebaseUser user) {
        dislayMessage.setText("Welcome " + user.getEmail());

    }
}


