package accountlogin.registrationapp;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class SearchInventoryByKeyword extends AppCompatActivity {
    //Layout Variables
    LinearLayout checkbox_linearLayout;
    Button search_btn, main_menu_btn;
    EditText keywordSearch;
    CheckBox product_cbox, pid_cbox, stock_cbox, desc_cbox, image_cbox, department_cbox, location_cbox;
    TextView tvDisplayInfo;
    Button modify_btn;
    ListView listView;

    //add firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference ProductRef;
    private String userID;

    ArrayList<String> productChecker = new ArrayList<>();
    ArrayList<String> displaySearchedProducts = new ArrayList<>();
    String productInfo = "";

    //Checkbox Values
    int pCbox = 1; //default
    int pidCbox = 1; //default
    int stockCbox = 0;
    int descCbox = 0;
    int imageCbox = 0;
    int departmentCbox = 0;
    int locationCbox = 0;
    int totalNumProducts = 0;

    //Search btn control variable
    int searchBtnPressed = 0;

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";

    ArrayList<String> allImage = new ArrayList<>();
    ArrayList<String> allProduct = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_inventory_by_keyword);
        setTitle("Search Keyword");

        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        search_btn = (Button)findViewById(R.id.search_btn);
        keywordSearch = (EditText)findViewById(R.id.keywordText);
        listView = (ListView)findViewById(R.id.listViewX);
        //Setup the modify search functionality
        initializeModifyVariables();
        modifyCheckBoxListeners();
        modifyButtonListener();
        mainMenuBtnListener();

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                }
            }
        };

        ProductRef = mFirebaseDatabase.getReference().child(userID).child("Products");
        ProductRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productChecker = new ArrayList<String>();
                productInfo = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_name = data.child("P_Name").getValue().toString();
                    String p_desc = data.child("P_Desc").getValue().toString();
                    String p_stock = data.child("P_Stock").getValue().toString();
                    String p_id = data.child("P_ID").getValue().toString();
                    String p_image = data.child("P_ImagePath").getValue().toString();
                    String p_aisle = data.child("P_Aisle").getValue().toString();
                    String p_bay = data.child("P_Bay").getValue().toString();
                    String p_shelf = data.child("P_Shelf").getValue().toString();
                    String p_dept = data.child("P_Dept").getValue().toString();
                    productInfo = p_name + "¿" + p_id + "¿" + p_stock + "¿" + p_desc + "¿" + p_image + "¿" + p_aisle + "¿" + p_bay + "¿" + p_shelf + "¿" + p_dept;
                    productChecker.add(productInfo);
                }
                updateListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*== Search button listener ==*/
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pname = "";
                String pid = "";
                String stock = "";
                String desc = "";
                String location ="";
                String dept = "";
                searchBtnPressed=1;
                totalNumProducts = 0;
                String keyword = keywordSearch.getText().toString();
                displaySearchedProducts = new ArrayList<String>();
                allImage = new ArrayList<>();
                for (int i = 0; i < productChecker.size(); i++) {
                    String strHold = productChecker.get(i);
                    String[] myDeptArr = strHold.split("¿");
                    if (myDeptArr[0].toString().contains(keyword.toLowerCase())){
                        totalNumProducts++;
                        if (pCbox == 1) {
                            pname= "Product: " + myDeptArr[0];
                        } else { pname = ""; }
                        if (pidCbox == 1) {
                            pid = "\nPID: " + myDeptArr[1];
                        } else { pid=""; }
                        if (stockCbox==1){
                            stock="\nStock: " + myDeptArr[2];
                        } else { stock=""; }
                        if (descCbox==1) {
                            desc="\nDescription: " + myDeptArr[3];
                        } else { desc=""; }
                        if (imageCbox==1){
                            allImage.add(myDeptArr[4]);
                        } else {
                            allImage.add(null); // set nothing into imageview
                        }
                        if (locationCbox==1){
                            location="\nLocation: A:"+myDeptArr[5] + " B:" +myDeptArr[6]+" S:"+myDeptArr[7];
                        } else {
                            location="";
                        }
                        if (departmentCbox==1){
                            dept="\nDepartment: " + myDeptArr[8];
                        } else {
                            dept="";
                        }
                        displaySearchedProducts.add(pname + pid + stock + desc + location + dept);
                    }

                }

                /*===Regenerate the Listview & create it with Products that match current Dept Selected====*/
                if (displaySearchedProducts.size() >= 1) {
                    tvDisplayInfo.setText("Displaying " + totalNumProducts+ " Product(s)");
                    CustomImageList adapter = new CustomImageList(SearchInventoryByKeyword.this, displaySearchedProducts, allImage, userID);
                    listView = (ListView)findViewById(R.id.listViewX);
                    listView.setAdapter(adapter);

                } else {
                    tvDisplayInfo.setText("Displaying " + totalNumProducts + " Product(s)");
                    allImage.add(null); // set nothing into imageview
                    displaySearchedProducts.add("No Products To Display For This Department!");
                    CustomImageList adapter = new CustomImageList(SearchInventoryByKeyword.this, displaySearchedProducts, allImage, userID);
                    listView = (ListView)findViewById(R.id.listViewX);
                    listView.setAdapter(adapter);
                }
            }
        });



    }

    /*== Update List View when Search button is pressed and user makes modifications within search filters ===*/
    public void searchBtnUpdateListener(){
        String pname = "";
        String pid = "";
        String stock = "";
        String desc = "";
        String location="";
        String dept="";
        totalNumProducts = 0;
        searchBtnPressed = 1;
        String keyword = keywordSearch.getText().toString();
        displaySearchedProducts = new ArrayList<String>();
        allImage = new ArrayList<>();
        for (int i = 0; i < productChecker.size(); i++) {
            String strHold = productChecker.get(i);
            String[] myDeptArr = strHold.split("¿");
            if (myDeptArr[0].toString().contains(keyword.toLowerCase())){
                totalNumProducts++;
                if (pCbox == 1) {
                    pname= "Product: " + myDeptArr[0];
                } else { pname = ""; }
                if (pidCbox == 1) {
                    pid = "\nPID: " + myDeptArr[1];
                } else { pid=""; }
                if (stockCbox==1){
                    stock="\nStock: " + myDeptArr[2];
                } else { stock=""; }
                if (descCbox==1) {
                    desc="\nDescription: " + myDeptArr[3];
                } else { desc=""; }
                if (imageCbox==1){
                    allImage.add(myDeptArr[4]);
                } else {
                    allImage.add(null); // set nothing into imageview
                }
                if (locationCbox==1){
                    location="\nLocation: A:"+myDeptArr[5] + " B:" +myDeptArr[6]+" S:"+myDeptArr[7];
                } else {
                    location="";
                }
                if (departmentCbox==1){
                    dept="\nDepartment: " + myDeptArr[8];
                } else {
                    dept="";
                }
                displaySearchedProducts.add(pname + pid + stock + desc + location + dept);
            }
        }
        /*===Regenerate the Listview & create it with Products that match current Dept Selected====*/
        if (displaySearchedProducts.size() >= 1) {
            tvDisplayInfo.setText("Displaying " + totalNumProducts+ " Product(s)");
            CustomImageList adapter = new CustomImageList(SearchInventoryByKeyword.this, displaySearchedProducts, allImage, userID);
            listView = (ListView)findViewById(R.id.listViewX);
            listView.setAdapter(adapter);

        } else {
            tvDisplayInfo.setText("Displaying " + totalNumProducts + " Product(s)");
            allImage.add(null); // set nothing into imageview
            displaySearchedProducts.add("No Products To Display For This Keyword!");
            CustomImageList adapter = new CustomImageList(SearchInventoryByKeyword.this, displaySearchedProducts, allImage, userID);
            listView = (ListView)findViewById(R.id.listViewX);
            listView.setAdapter(adapter);
        }
    }



    public void modifyButtonListener() {
         /*== Modify search Filters Listener ==*/
        modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hide = "Hide Search Filters";
                String modify = "Modify Search Filters";
                if (modify_btn.getText().toString().equals(hide)) {
                    if (searchBtnPressed==1){
                        searchBtnUpdateListener();
                    }else {
                        updateListView();
                    }
                    modify_btn.setTextColor(Color.BLACK);
                    hideCheckBoxes();
                    listView.setVisibility(View.VISIBLE);
                    tvDisplayInfo.setVisibility(View.VISIBLE);
                    modify_btn.setText(modify);
                } else {
                    modify_btn.setTextColor(Color.RED);
                    showCheckBoxes();
                    modify_btn.setText(hide);
                    listView.setVisibility(View.INVISIBLE);
                    tvDisplayInfo.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    /*== Create / Update the custom list view based on search modifiers ==*/
    public void updateListView() {
        String pname = "";
        String pid = "";
        String stock = "";
        String desc = "";
        String location="";
        String dept = "";
        totalNumProducts = 0;
        allImage = new ArrayList<>();
        allProduct = new ArrayList<>();
        for (int i = 0; i < productChecker.size(); i++) {
            String strHold = productChecker.get(i);
            String[] myDeptArr = strHold.split("¿");
            totalNumProducts++;

            if (pCbox == 1) {
                pname= "Product: " + myDeptArr[0];
            } else { pname = ""; }
            if (pidCbox == 1) {
                pid = "\nPID: " + myDeptArr[1];
            } else { pid=""; }
            if (stockCbox==1){
                stock="\nStock: " + myDeptArr[2];
            } else { stock=""; }
            if (descCbox==1) {
                desc="\nDescription: " + myDeptArr[3];
            } else { desc=""; }
            if (imageCbox==1){
                allImage.add(myDeptArr[4]);
            } else {
                allImage.add(null); // set nothing into imageview
            }
            if (locationCbox==1){
                location="\nLocation: A:"+myDeptArr[5] + " B:" +myDeptArr[6]+" S:"+myDeptArr[7];
            } else {
                location="";
            }
            if (departmentCbox==1){
                dept="\nDepartment: " + myDeptArr[8];
            } else {
                dept="";
            }
            displaySearchedProducts.add(pname + pid + stock + desc + location + dept);
        }

         /*===Regenerate the Listview & create it with Products that match current Dept Selected====*/
        if (allProduct.size() >= 1) {
            tvDisplayInfo.setText("Displaying " + totalNumProducts+ " Product(s)");
            CustomImageList adapter = new CustomImageList(SearchInventoryByKeyword.this, allProduct, allImage, userID);
            listView = (ListView)findViewById(R.id.listViewX);
            listView.setAdapter(adapter);

        } else {
            tvDisplayInfo.setText("Displaying " + totalNumProducts + " Product(s)");
            allProduct.add("No Products to display...");
            CustomImageList adapter = new CustomImageList(SearchInventoryByKeyword.this, allProduct, allImage, userID);
            listView = (ListView)findViewById(R.id.listViewX);
            listView.setAdapter(adapter);
        }
    }
    public void initializeModifyVariables() {
        checkbox_linearLayout = (LinearLayout)findViewById(R.id.checkbox_linearLayout);
        tvDisplayInfo = (TextView) findViewById(R.id.tvDisplayInfo);
        modify_btn =    (Button)   findViewById(R.id.modify_btn);
        product_cbox =  (CheckBox) findViewById(R.id.product_cbox);
        pid_cbox =      (CheckBox) findViewById(R.id.pid_cbox);
        desc_cbox =     (CheckBox) findViewById(R.id.desc_cbox);
        stock_cbox =    (CheckBox) findViewById(R.id.stock_cbox);
        image_cbox =    (CheckBox) findViewById(R.id.image_cbox);
        department_cbox = (CheckBox)findViewById(R.id.department_cbox);
        location_cbox = (CheckBox)findViewById(R.id.location_cbox);
        hideCheckBoxes();
    }

    public void hideCheckBoxes() {
        checkbox_linearLayout.setVisibility(View.INVISIBLE);
        product_cbox.setVisibility(View.INVISIBLE);
        pid_cbox.setVisibility(View.INVISIBLE);
        stock_cbox.setVisibility(View.INVISIBLE);
        desc_cbox.setVisibility(View.INVISIBLE);
        image_cbox.setVisibility(View.INVISIBLE);
    }

    public void showCheckBoxes() {
        checkbox_linearLayout.setVisibility(View.VISIBLE);
        product_cbox.setVisibility(View.VISIBLE);
        pid_cbox.setVisibility(View.VISIBLE);
        stock_cbox.setVisibility(View.VISIBLE);
        desc_cbox.setVisibility(View.VISIBLE);
        image_cbox.setVisibility(View.VISIBLE);
    }

    /*== Listeners that determine what is displayed within the listView ==*/
    public void modifyCheckBoxListeners() {
        product_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (product_cbox.isChecked()) {
                    pCbox = 1;
                } else {
                    pCbox = 0;
                }
            }
        });
        pid_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (pid_cbox.isChecked()) {
                    pidCbox = 1;
                } else {
                    pidCbox = 0;
                }
            }
        });
        stock_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (stock_cbox.isChecked()) {
                    stockCbox = 1;
                } else {
                    stockCbox = 0;
                }
            }
        });
        desc_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (desc_cbox.isChecked()) {
                    descCbox = 1;
                } else {
                    descCbox = 0;
                }
            }
        });
        image_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (image_cbox.isChecked()){
                    imageCbox = 1;
                } else {
                    imageCbox = 0;
                }
            }
        });
        location_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (location_cbox.isChecked()){
                    locationCbox = 1;
                }else {
                    locationCbox = 0;
                }
            }
        });
        department_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(department_cbox.isChecked()){
                    departmentCbox = 1;
                } else {
                    departmentCbox = 0;
                }
            }
        });
    }
    public void mainMenuBtnListener(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchInventoryByKeyword.this, MainMenu.class);
                sendIntentData(intent);
            }
        });
    }

    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    private void toastMessage(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}
