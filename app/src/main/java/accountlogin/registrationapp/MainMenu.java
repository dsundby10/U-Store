package accountlogin.registrationapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainMenu extends AppCompatActivity {
    private Button add_inv_btn, edit_inv_btn, add_shelving_btn,
            add_dept_btn, edit_layout_btn, view_layout_btn, logout_btn, acct_info_btn, button2;
    private TextView dislayMessage;
    private Spinner search_inv_btn;

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
        add_shelving_btn = (Button)findViewById(R.id.add_shelving_btn);
        add_dept_btn = (Button)findViewById(R.id.add_dept_btn);
        edit_layout_btn = (Button)findViewById(R.id.edit_layout_btn);
        view_layout_btn = (Button)findViewById(R.id.view_layout_btn);
        logout_btn = (Button) findViewById(R.id.logout_btn);
        acct_info_btn = (Button) findViewById(R.id.acct_info_btn);

        button2 = (Button)findViewById(R.id.button2);

        search_inv_btn = (Spinner)findViewById(R.id.search_inv_btn);

        final String searchInventoryBy = "Search Inventory, Location, Department, Keyword";
        String[] fillSpinner = searchInventoryBy.split("\\s*,\\s*");
        ArrayList<String> inventoryValues = new ArrayList<>();
        for (int i = 0; i < fillSpinner.length; i++) {
            inventoryValues.add(fillSpinner[i]);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, inventoryValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        search_inv_btn.setAdapter(dataAdapter);

        search_inv_btn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currentPosition = search_inv_btn.getSelectedItem().toString();
                if (currentPosition.equals("Department")) {
                    Intent intent5 = new Intent(MainMenu.this, SearchInventoryByDepartmentActivity.class);
                    startActivity(intent5);
                }
                if (currentPosition.equals("Location")){
                    Intent intent = new Intent(MainMenu.this, SearchInventoryActivity.class);
                    startActivity(intent);
                }

               search_inv_btn.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        edit_inv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, EditInventoryActivity.class);
                startActivity(intent);
            }
        });

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
                Intent intent = new Intent(MainMenu.this, ShelvingAddEditActivity.class);
                startActivity(intent);
            }
        });

        view_layout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, StoreLayoutActivity.class);
                startActivity(intent);
            }
        });


        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(MainMenu.this, LoginActivity.class);
                startActivity(intent);
            }
        });
       acct_info_btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               AlertDialog.Builder builder;
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                   builder = new AlertDialog.Builder(acct_info_btn.getContext(), android.R.style.Theme_Material_Dialog_Alert);
               } else {
                   builder = new AlertDialog.Builder(acct_info_btn.getContext());
               }
               builder.setTitle("Delete entry")
                       .setMessage("Are you sure you want to delete this entry?")
                       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                               // continue with delete
                           }
                       })
                       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                               // do nothing
                           }
                       })
                       .setIcon(android.R.drawable.ic_dialog_alert)
                       .show();
               //Intent intent = new Intent(MainMenu.this, Main2Activity.class);
               //startActivity(intent);
           }
       });


    }
    @SuppressLint("SetTextI18n")
    private void setDataToView(FirebaseUser user) {
        dislayMessage.setText("Welcome " + user.getEmail());

    }
}


