package accountlogin.registrationapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainMenu extends AppCompatActivity {
    private Button add_inv_btn, edit_inv_btn, add_shelving_btn, edit_ab_btn,
            add_dept_btn, edit_layout_btn, view_layout_btn, logout_btn, acct_info_btn,
            create_store_btn, logout_create_btn, manage_create_btn;
    private TextView dislayMessage;
    private Spinner search_inv_btn;
    LinearLayout createStoreLinearLayout, showStoreButtonsLinearLayout;
    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference shelfRef;
    private DatabaseReference populateDate;
    private DatabaseReference AisleRef;
    private String userID;
    String getStoreName = "";
    String employeeID;
    String getUserPermissions="";
    static String[] permissionArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Intent intent = getIntent();

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        //LayoutVariables Initialization
        initializeLayoutVariables();
        showStoreButtonsLinearLayout = (LinearLayout)findViewById(R.id.showStoreButtonsLinearLayout);
        createStoreLinearLayout = (LinearLayout)findViewById(R.id.createStoreLinearLayout);
        activateDatabaseListeners();

        //If employee logs into the store owners store...
        if (!getIntent().getStringExtra("STORE_USER").equals("null")){
            getStoreName = getIntent().getStringExtra("STORE_NAME");
            employeeID = getIntent().getStringExtra("STORE_USER");
            getUserPermissions = getIntent().getStringExtra("USER_PERMISSIONS").trim();
            permissionArray = getUserPermissions.trim().split("");
            storeDoesExist();
            dislayMessage.setText("Store: " + getStoreName + "\n Store User: " + employeeID);
            System.out.println("USER Intent Received! \n(STORE_NAME) : " + getStoreName
                    + "\n(STORE_USER) : " + employeeID + "\n(USER_PERMISSIONS) : " + getUserPermissions);
        } else {
            employeeID="null";
            employeeID = getIntent().getStringExtra("STORE_USER");
            getStoreName = getIntent().getStringExtra("STORE_NAME");
            getUserPermissions = getIntent().getStringExtra("USER_PERMISSIONS").trim();
            //Check if store owner has an active store created
            if (getStoreName.equals("?¿NA¿?")){
                dislayMessage.setText(user.getEmail());
                noStoreExists();
            } else {
                dislayMessage.setText("Store: " + getStoreName + "\nStore Owner: " + user.getEmail());
                storeDoesExist();
            }
            permissionArray = getUserPermissions.split("");
            System.out.println("OWNER Intent Received! \n(STORE_NAME) : " + getStoreName
                    + "\n(STORE_USER) : " + employeeID + "\n(USER_PERMISSIONS) : " + getUserPermissions);
        }

        /*== Create store button listener ==*/
        create_store_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, StoreAndDepartmentSetupActivity.class);
                sendIntentData(intent);
            }
        });

        /*=== Modify Aisle / Bays Button Listener ===*/
        edit_ab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionArray[5].equals("0")) {
                    toastMessage("You don't have permission to access this content!");
                } else {
                    Intent intent = new Intent(MainMenu.this, AisleBaySetup.class);
                    sendIntentData(intent);
                }
            }
        });

        /*== Add / Edit Shelving Button Listener ==*/
        add_shelving_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissionArray[4].equals("0")){
                    toastMessage("You don't have permission to access this content!");
                }else {
                    Intent intent = new Intent(MainMenu.this, ShelvingAddEditActivity.class);
                    sendIntentData(intent);
                }
            }
        });

        /*== Add / Edit Departments Button Listner ==*/
        add_dept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissionArray[7].equals("0")){
                    toastMessage("You don't have permission to access this content!");
                } else {
                    Intent intent = new Intent(MainMenu.this, EditStoreAndDepartmentActivity.class);
                    sendIntentData(intent);
                }
            }
        });

         /*== Add Inventory Button Listener ==*/
        add_inv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissionArray[1].equals("0")){
                    toastMessage("You don't have permission to access this content!");
                } else {
                    Intent intent = new Intent(MainMenu.this, AddInventory.class);
                    sendIntentData(intent);
                }
            }
        });

        /*=== Search inventory button / spinner ===*/
        searchInventoryListener();

        edit_inv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionArray[2].equals("0")){
                    toastMessage("You don't have permission to access this content!");
                } else {
                    Intent intent = new Intent(MainMenu.this, EditInventoryActivity.class);
                    sendIntentData(intent);
                }
            }
        });

        /*== Edit Store Layout Button Listener ==*/
        edit_layout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionArray[6].equals("0")){
                    toastMessage("You don't have permission to access this content!");
                } else {
                    toastMessage("Under Development...");

                }
            }
        });

        /*== View Store Layout Button Listener ==*/
        view_layout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionArray[8].equals("0")){
                    toastMessage("You don't have permission to access this content!");
                } else {
                    Intent intent = new Intent(MainMenu.this, StoreLayoutActivity.class);
                    sendIntentData(intent);
                }
            }
        });

         /*== Manage Account Button Listener ==*/
        acct_info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionArray[9].equals("0")){
                    toastMessage("You don't have permission to access this content!");
                } else {
                    Intent intent = new Intent(MainMenu.this, ManageAccountActivity.class);
                    sendIntentData(intent);
                }
            }
        });


        /*== Logout Button Listener ==*/
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(MainMenu.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        /*== Logout button for create store layout ==*/
        logout_create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(MainMenu.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        /*== Manage Account Button Listener for create store layout ==*/
        manage_create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionArray[9].equals("0")){
                    toastMessage("You don't have permission to access this content!");
                } else {
                    Intent intent = new Intent(MainMenu.this, ManageAccountActivity.class);
                    sendIntentData(intent);
                }
            }
        });
    }


    private void toastMessage(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    public void initializeLayoutVariables(){
        dislayMessage = (TextView)findViewById(R.id.displayMessage);
        add_inv_btn = (Button)findViewById(R.id.add_inv_btn);
        edit_inv_btn = (Button)findViewById(R.id.edit_inv_btn);
        add_shelving_btn = (Button)findViewById(R.id.add_shelving_btn);
        add_dept_btn = (Button)findViewById(R.id.add_dept_btn);
        edit_layout_btn = (Button)findViewById(R.id.edit_layout_btn);
        view_layout_btn = (Button)findViewById(R.id.main_menu_btn);
        logout_btn = (Button) findViewById(R.id.logout_btn);
        acct_info_btn = (Button) findViewById(R.id.acct_info_btn);
        edit_ab_btn = (Button)findViewById(R.id.edit_ab_btn);
        create_store_btn = (Button)findViewById(R.id.create_store_btn);
        logout_create_btn = (Button)findViewById(R.id.logout_create_btn);
        manage_create_btn = (Button)findViewById(R.id.manage_create_btn);
    }
    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
    }


    public void storeDoesExist(){
        showStoreButtonsLinearLayout.setVisibility(View.VISIBLE);
        createStoreLinearLayout.setVisibility(View.INVISIBLE);
    }

    public void noStoreExists(){
        showStoreButtonsLinearLayout.setVisibility(View.INVISIBLE);
        createStoreLinearLayout.setVisibility(View.VISIBLE);
    }

    /*=== Search inventory button / spinner ===*/
    public void searchInventoryListener(){
        search_inv_btn = (Spinner) findViewById(R.id.search_inv_btn);
        String searchInventoryBy = "Search Inventory, Location, Department, Keyword";
        String[] fillSpinner = searchInventoryBy.split("\\s*,\\s*");
        final ArrayList<String> inventoryValues = new ArrayList<>();
        //create spinner values
        for (int i = 0; i < fillSpinner.length; i++) {
            inventoryValues.add(fillSpinner[i]);
        }

         ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, inventoryValues);
        dataAdapter.setDropDownViewResource(R.layout.custom_spinner_layout);
        search_inv_btn.setAdapter(dataAdapter);

        /*===Search Inventory Button/Spinner Selected Listener ===*/
        search_inv_btn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currentPosition = search_inv_btn.getSelectedItem().toString();
                if (currentPosition.equals("Search Inventory")){
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                    ((TextView) parent.getChildAt(0)).setTextSize(14);
                    ((TextView) parent.getChildAt(0)).setAllCaps(true);
                    ((TextView) parent.getChildAt(0)).setTypeface(null,Typeface.BOLD);
                    ((TextView) parent.getChildAt(0)).setTypeface(Typeface.SANS_SERIF);
                }
                //Search by Department
                if (currentPosition.equals("Department")) {
                    if (permissionArray[3].equals("0")) {
                        toastMessage("You don't have permission to access this content!");
                    } else {
                        Intent intent = new Intent(MainMenu.this, SearchInventoryByDepartmentActivity.class);
                        sendIntentData(intent);
                    }
                }
                //Search by Store Location (Aisle / Bay / Shelf)
                if (currentPosition.equals("Location")) {
                    if (permissionArray[3].equals("0")) {
                        toastMessage("You don't have permission to access this content!");
                    } else {
                        Intent intent = new Intent(MainMenu.this, SearchInventoryActivity.class);
                        sendIntentData(intent);
                    }
                }
                //Search by Product Keyword
                if (currentPosition.equals("Keyword")) {
                    if (permissionArray[3].equals("0")) {
                        toastMessage("You don't have permission to access this content!");
                    } else {
                        Intent intent = new Intent(MainMenu.this, SearchInventoryByKeyword.class);
                        sendIntentData(intent);
                    }
                }
                search_inv_btn.setSelection(0);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }




    public void activateDatabaseListeners(){
        AisleRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        shelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        shelfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        populateDate = mFirebaseDatabase.getReference().child(userID);
        populateDate.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}


