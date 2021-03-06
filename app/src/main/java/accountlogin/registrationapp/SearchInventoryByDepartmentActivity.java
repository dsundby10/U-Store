package accountlogin.registrationapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class SearchInventoryByDepartmentActivity extends AppCompatActivity {
    LinearLayout checkbox_linearLayout;
    Spinner dept_spinner;
    ListView listView;
    Button main_menu_btn, modify_btn;
    CheckBox product_cbox, pid_cbox, stock_cbox, desc_cbox, image_cbox, department_cbox, location_cbox;
    TextView tvDisplayInfo;

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Database References
    private DatabaseReference myRef;
    private DatabaseReference AisleBayShelfRef;
    private DatabaseReference ABS;
    private String userID;

    String deptString = "";
    String[] deptArr;

    ArrayList<String> dept_spinnerValues = new ArrayList<>();
    ArrayList<String> currentDeptProductInfo = new ArrayList<>();
    ArrayList<String> productChecker = new ArrayList<>();

    String deptProductInfo = "";
    String currentDept = "";

    int pCbox=1; //default
    int pidCbox=1; //default
    int stockCbox=0;
    int descCbox=0;
    int imageCbox=0;
    int departmentCbox = 0;
    int locationCbox = 0;

    ArrayList<String> allImage = new ArrayList<>();
    ArrayList<String> allProduct = new ArrayList<>();

    String[] myDeptArr;
    int totalNumProductsForDept = 0;

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_inventory_by_department);

        setTitle("Search by Department");

        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {

                }
            }
        };

        //Initialize variables & modifyBtn checkbox listener
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
        initializeModifyVariables();
        modifyCheckBoxListeners();
        mainMenuBtnListener();

        /*== Modify search Filters Listener ==*/
        modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hide = "Hide Search Filters";
                String modify = "Modify Search Filters";
                /*== avoid an error ==*/
                if (dept_spinnerValues.size()==0){
                    toastMessage("You must have departments to modify your search.");
                } else {
                    if (modify_btn.getText().toString().equals(hide)) {
                        modify_btn.setTextColor(Color.BLACK);
                        updateListView();
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
            }
        });


          /*==== Product listener - pulling all the stores product info and forming it into an arrayList ====*/
        ABS = mFirebaseDatabase.getReference().child(userID).child("Products");
        ABS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!this.getClass().equals(SearchInventoryByDepartmentActivity.class)){
                    ABS.removeEventListener(this);
                }
                productChecker = new ArrayList<String>();
                deptProductInfo = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_dept = data.child("P_Dept").getValue().toString();
                    String p_name = data.child("P_Name").getValue().toString();
                    String p_desc = data.child("P_Desc").getValue().toString();
                    String p_stock = data.child("P_Stock").getValue().toString();
                    String p_id = data.child("P_ID").getValue().toString();
                    String p_image = data.child("P_ImagePath").getValue().toString();
                    String p_aisle = data.child("P_Aisle").getValue().toString();
                    String p_bay = data.child("P_Bay").getValue().toString();
                    String p_shelf = data.child("P_Shelf").getValue().toString();
                    deptProductInfo = p_dept + "¿" + p_name + "¿" + p_id +"¿"+ p_stock +"¿"+ p_desc + "¿" + p_image + "¿" + p_aisle + "¿" + p_bay + "¿" + p_shelf;
                    productChecker.add(deptProductInfo);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*======= Aisle DB Reference =======*/
        AisleBayShelfRef = mFirebaseDatabase.getReference().child(userID);
        AisleBayShelfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dept_spinnerValues = new ArrayList<String>();
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    if (data.getKey().equals("deptNames") && !data.getValue().toString().trim().equals("")) {
                        deptString = data.getValue().toString();
                        createDeptSpinner(deptString);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*=====Department Spinner Listner =====*/
        dept_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateListView();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    /*===Generate Dept Spinner Values ====*/
    public void createDeptSpinner(String deptString) {
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
        dept_spinnerValues = new ArrayList<>();
        deptArr = deptString.split("\\s*,\\s*");

        //Generate Spinner
        for (int i = 0; i < deptArr.length; i++) {
            String x = String.valueOf(i);
            dept_spinnerValues.add(deptArr[i]);
        }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
            (this, android.R.layout.simple_spinner_item,  dept_spinnerValues);
            dataAdapter.setDropDownViewResource
            (android.R.layout.simple_spinner_dropdown_item);
            dept_spinner.setAdapter(dataAdapter);
    }

    public void updateListView(){
        String pname="";
        String pid="";
        String stock="";
        String desc= "";
        String location = "";
        String dept = "";
        totalNumProductsForDept=0;

        currentDept = dept_spinner.getSelectedItem().toString();
        String findDept;
        currentDeptProductInfo = new ArrayList<>();
        allImage = new ArrayList<>();
        /*===Find all the products that are assigned to this department===*/
        for (int i = 0; i < productChecker.size(); i++) {
            findDept = productChecker.get(i);
            myDeptArr = findDept.split("¿");
            if (myDeptArr[0].trim().equals(currentDept.trim())){
                totalNumProductsForDept++;
                if (pCbox == 1) {
                    pname= "Product: " + myDeptArr[1];
                } else { pname = ""; }
                if (pidCbox == 1) {
                    pid = "\nPID: " + myDeptArr[2];
                } else { pid=""; }
                if (stockCbox==1){
                    stock="\nStock: " + myDeptArr[3];
                } else { stock=""; }
                if (descCbox==1) {
                    desc="\nDescription: " + myDeptArr[4];
                } else { desc=""; }
                if (imageCbox==1){
                    allImage.add(myDeptArr[5]);
                } else {
                    allImage.add(null); // set nothing into imageview
                }
                if (locationCbox==1){
                    location="\nLocation: A:"+myDeptArr[6] + " B:" +myDeptArr[7]+" S:"+myDeptArr[8];
                } else {
                    location="";
                }
                if (departmentCbox==1){
                    dept="\nDepartment: " + myDeptArr[0];
                } else {
                    dept="";
                }
                String currentDeptProd = pname + pid + stock + desc + dept + location;
                currentDeptProductInfo.add(currentDeptProd);
            }
        }

        /*===Regenerate the Listview & create it with Products that match current Dept Selected====*/
        if (currentDeptProductInfo.size() >= 1) {
            tvDisplayInfo.setText("Displaying " + totalNumProductsForDept + " Product(s)");
            CustomImageList adapter = new CustomImageList(SearchInventoryByDepartmentActivity.this, currentDeptProductInfo , allImage, userID);
            listView = (ListView)findViewById(R.id.listViewX);
            listView.setAdapter(adapter);

        } else {
            tvDisplayInfo.setText("Displaying " + totalNumProductsForDept + " Product(s)");
            currentDeptProductInfo.add("No Products To Display For This Department!");
            allImage.add(null); // set nothing into imageview
            CustomImageList adapter = new CustomImageList(SearchInventoryByDepartmentActivity.this, currentDeptProductInfo , allImage, userID);
            listView = (ListView)findViewById(R.id.listViewX);
            listView.setAdapter(adapter);


        }
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
    public void hideCheckBoxes(){
        checkbox_linearLayout.setVisibility(View.INVISIBLE);
        product_cbox.setVisibility(View.INVISIBLE);
        pid_cbox.setVisibility(View.INVISIBLE);
        stock_cbox.setVisibility(View.INVISIBLE);
        desc_cbox.setVisibility(View.INVISIBLE);
        image_cbox.setVisibility(View.INVISIBLE);
    }
    public void showCheckBoxes(){
        checkbox_linearLayout.setVisibility(View.VISIBLE);
        product_cbox.setVisibility(View.VISIBLE);
        pid_cbox.setVisibility(View.VISIBLE);
        stock_cbox.setVisibility(View.VISIBLE);
        desc_cbox.setVisibility(View.VISIBLE);
        image_cbox.setVisibility(View.VISIBLE);
    }

    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
    }
    public void mainMenuBtnListener(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchInventoryByDepartmentActivity.this, MainMenu.class);
                sendIntentData(intent);
            }
        });
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


