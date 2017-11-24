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

public class SearchInventoryActivity extends AppCompatActivity {
    //Layout Variables
    Spinner aisle_spinner, bay_spinner, shelf_spinner;
    ListView listView;
    CheckBox checkBox0, checkBox1, checkBox2;
    Button main_menu_btn;

    //added
    LinearLayout checkbox_linearLayout;
    CheckBox product_cbox, pid_cbox, stock_cbox, desc_cbox, image_cbox, department_cbox, location_cbox;
    TextView tvDisplayInfo;
    Button modify_btn;

    int pCbox = 1; //default
    int pidCbox = 1; //default
    int stockCbox = 0;
    int descCbox = 0;
    int imageCbox = 0;
    int departmentCbox = 0;
    int locationCbox = 0;

    ArrayList<String> allImage = new ArrayList<>();
    ArrayList<String> allProduct = new ArrayList<>();

    int totalNumProducts = 0;
    ArrayList<String> currentSelectedLocation = new ArrayList<>();
    int aisleCbox = 0;
    int bayCbox = 0;
    int shelfCbox = 0;

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Database References
    private DatabaseReference myRef;
    private DatabaseReference AisleBayShelfRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference ShelfRef;
    private DatabaseReference ABS;
    private String userID;

    ArrayList<String> aisle_spinnerValues = new ArrayList<>();
    String aisleChecker = "";
    int aisleIntChecker = 0;
    ArrayList<String> bay_spinnerValues = new ArrayList<>();
    ArrayList<String> bayCheckerList = new ArrayList<>();
    String bayChecker = "";

    ArrayList<String> shelf_spinnerValues = new ArrayList<>();
    ArrayList<String> shelfCheckerList = new ArrayList<>();

    ArrayList<String> productChecker = new ArrayList<>();
    String allABSProductInfo = "";


    ArrayList<String> currentShelfProductInfo = new ArrayList<>();
    ArrayList<String> aisleCheckerList = new ArrayList<String>();
    ArrayList<String> bayCheckerListz = new ArrayList<String>();
    ArrayList<String> allABS = new ArrayList<>();

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Search Inventory: Location");
        setContentView(R.layout.activity_search_inventory);

        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        //Setup the modify search functionality
        initializeModifyVariables();
        modifyCheckBoxListeners();
        modifyButtonListener();
        mainMenuBtnListener();

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        //variable initialization
        aisle_spinner = (Spinner) findViewById(R.id.spinner0);
        bay_spinner = (Spinner) findViewById(R.id.spinner1);
        shelf_spinner = (Spinner) findViewById(R.id.spinner2);
        checkBox0 = (CheckBox) findViewById(R.id.checkBox0);
        checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
        checkBox2 = (CheckBox) findViewById(R.id.checkBox2);
        listView = (ListView) findViewById(R.id.listViewX);
        checkBox1.setVisibility(View.GONE);
        checkBox2.setVisibility(View.GONE);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {

                }
            }
        };
        /*==== Product listener - pulling all the stores product info and forming it into an arrayList ====*/
        ABS = mFirebaseDatabase.getReference().child(userID).child("Products");
        ABS.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              /*  if (!this.getClass().equals(SearchInventoryActivity.class)) {
                    ABS.removeEventListener(this);
                }*/

                productChecker = new ArrayList<String>();
                allABSProductInfo = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_aisle = data.child("P_Aisle").getValue().toString();
                    String p_bay = data.child("P_Bay").getValue().toString();
                    String p_shelf = data.child("P_Shelf").getValue().toString();
                    String p_name = data.child("P_Name").getValue().toString();
                    String p_desc = data.child("P_Desc").getValue().toString();
                    String p_stock = data.child("P_Stock").getValue().toString();
                    String p_id = data.child("P_ID").getValue().toString();
                    String p_image = data.child("P_ImagePath").getValue().toString();
                    String p_dept = data.child("P_Dept").getValue().toString();
                    // the ¿ servers as the element at which I split the string at to form a string (probably isnt the right way, but easiest?)
                    allABSProductInfo =  p_aisle + "¿" + p_bay + "¿" + p_shelf + "¿" + p_name + "¿" + p_id +"¿"+ p_stock +"¿"+ p_desc + "¿" + p_image + "¿" + p_dept;
                    productChecker.add(allABSProductInfo);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*=====Aisle Spinnner listener to update listview ====*/
        aisle_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (checkBox1.isChecked()) {
                    createBaySpinner();
                } else {
                    updateListView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        /*======= Bay Spinner Listener to update listview =====*/
        bay_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Update Shelf Spinner
                if (checkBox2.isChecked()) {
                    createShelfSpinner();
                } else {
                    updateListView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*====Shelf Spinner Listener to update listview====*/
        shelf_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*=====Aisle Check Box Listener ======= */
        checkBox0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    createSpinner();
                    aisle_spinner.setEnabled(true);
                    checkBox1.setVisibility(View.VISIBLE);
                    aisleCbox = 1;

                } else {
                    if (!checkBox0.isChecked() && checkBox1.isChecked() && checkBox2.isChecked()) {
                        // aisleCbox=0;
                        bay_spinner.setEnabled(false);
                        shelf_spinner.setEnabled(false);
                        aisle_spinner.setEnabled(false);
                        currentShelfProductInfo = new ArrayList<String>();
                        currentShelfProductInfo.add("Please Specify A Location.");
                        listView = (ListView) findViewById(R.id.listViewX);
                        ArrayAdapter arrayAdapter = new ArrayAdapter(SearchInventoryActivity.this, android.R.layout.simple_list_item_1, currentShelfProductInfo);
                        listView.setAdapter(arrayAdapter);
                    }
                    if (!checkBox0.isChecked() && !checkBox1.isChecked() && !checkBox2.isChecked()) {
                        //  aisleCbox=0;
                        bay_spinner.setEnabled(false);
                        shelf_spinner.setEnabled(false);
                        aisle_spinner.setEnabled(false);
                        currentShelfProductInfo = new ArrayList<String>();
                        currentShelfProductInfo.add("Please Specify A Location.");
                        listView = (ListView) findViewById(R.id.listViewX);
                        ArrayAdapter arrayAdapter = new ArrayAdapter(SearchInventoryActivity.this, android.R.layout.simple_list_item_1, currentShelfProductInfo);
                        listView.setAdapter(arrayAdapter);
                    }
                    aisleCbox = 0;
                    bay_spinner.setEnabled(false);
                    shelf_spinner.setEnabled(false);
                    aisle_spinner.setEnabled(false);

                    checkBox1.setVisibility(View.GONE);
                    checkBox2.setVisibility(View.GONE);
                    checkBox2.setChecked(false);
                    checkBox1.setChecked(false);

                }
            }
        });
        /*=====Bay Check Box Listener ======= */
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    createBaySpinner();
                    checkBox2.setVisibility(View.VISIBLE);
                    bay_spinner.setEnabled(true);
                    bayCbox = 1;
                } else {
                    if (checkBox2.isChecked() && !checkBox1.isChecked() && checkBox0.isChecked()) {
                        //Keep Aisles at current state and update the listview
                        bayCbox = 0;
                        String crntAisle = aisle_spinner.getSelectedItem().toString();
                        createSpinner();
                        aisle_spinner.setSelection(Integer.parseInt(crntAisle) - 1);
                    }
                    if (!checkBox2.isChecked() && !checkBox1.isChecked() && checkBox0.isChecked()) {
                        //Keep Aisles at current state and update the listview
                        bayCbox = 0;
                        String crntAisle = aisle_spinner.getSelectedItem().toString();
                        createSpinner();
                        aisle_spinner.setSelection(Integer.parseInt(crntAisle) - 1);
                    }
                    if (!checkBox1.isChecked() && !checkBox2.isChecked() && !checkBox0.isChecked()) {
                        bayCbox = 0;
                        currentShelfProductInfo = new ArrayList<String>();
                        currentShelfProductInfo.add("Please Specify A Location.");
                        listView = (ListView) findViewById(R.id.listViewX);
                        ArrayAdapter arrayAdapter = new ArrayAdapter(SearchInventoryActivity.this, android.R.layout.simple_list_item_1, currentShelfProductInfo);
                        listView.setAdapter(arrayAdapter);
                    }
                    bayCbox = 0;
                    shelf_spinner.setEnabled(false);
                    checkBox2.setChecked(false);
                    checkBox2.setVisibility(View.GONE);
                    bay_spinner.setEnabled(false);
                    checkBox1.setChecked(false);
                }
            }
        });
        /*=====Shelf Check Box Listener ======= */
        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    createShelfSpinner();
                    shelf_spinner.setEnabled(true);
                    shelfCbox = 1;
                } else {
                    if (!checkBox2.isChecked() && checkBox1.isChecked() && checkBox0.isChecked()) {
                        //Keep current bay spinner at current state
                        shelfCbox = 0;
                        String crntBay = bay_spinner.getSelectedItem().toString();
                        createBaySpinner();
                        bay_spinner.setSelection(Integer.parseInt(crntBay) - 1);
                    }
                    if (checkBox2.isChecked() && !checkBox1.isChecked() && checkBox0.isChecked()) {
                        //Keep Aisles at current state and update the listview
                        shelfCbox = 0;
                        String crntAisle = aisle_spinner.getSelectedItem().toString();
                        createSpinner();
                        aisle_spinner.setSelection(Integer.parseInt(crntAisle) - 1);
                    }
                    if (checkBox2.isChecked() && checkBox1.isChecked() && !checkBox0.isChecked()) {
                        shelfCbox = 0;
                        currentShelfProductInfo = new ArrayList<String>();
                        currentShelfProductInfo.add("Please Specify A Location.");
                        listView = (ListView) findViewById(R.id.listViewX);
                        ArrayAdapter arrayAdapter = new ArrayAdapter(SearchInventoryActivity.this, android.R.layout.simple_list_item_1, currentShelfProductInfo);
                        listView.setAdapter(arrayAdapter);
                    }
                    shelfCbox = 0;
                    shelf_spinner.setEnabled(false);
                    checkBox2.setChecked(false);
                }
            }
        });

         /*======= Aisle DB Reference =======*/
        AisleBayShelfRef = mFirebaseDatabase.getReference().child(userID);
        AisleBayShelfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                aisleChecker = "";
                aisleIntChecker = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    /*=== If User has an Existing Aisle Entry in the Database ===*/
                    if (data.getKey().equals("aisles") && !data.getValue().toString().trim().equals("")) {
                        aisleChecker = data.getValue().toString();
                        aisleIntChecker = Integer.parseInt(aisleChecker);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        /*===== ShelfSetup DB reference to generate spinner ===*/
        ShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        ShelfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!this.getClass().equals(SearchInventoryActivity.class)) {
                    ShelfRef.removeEventListener(this);
                }
                int count = 0;
                aisleCheckerList = new ArrayList<String>();
                bayCheckerListz = new ArrayList<String>();
                shelfCheckerList = new ArrayList<String>();
                allABS = new ArrayList<String>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String a = data.child("aisle_num").getValue().toString();
                    String b = data.child("bay_num").getValue().toString();
                    String s = data.child("num_of_shelves").getValue().toString();

                    aisleCheckerList.add(data.child("aisle_num").getValue().toString());
                    bayCheckerListz.add(data.child("bay_num").getValue().toString());
                    shelfCheckerList.add(data.child("num_of_shelves").getValue().toString());

                    allABS.add(a + "¿" + b + "¿" + s);
                    count++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*==== Pulling Bay Number out of "BaySetup" in the Database ===*/
        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bayCheckerList = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    bayCheckerList.add(data.child("bays").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    /*==== Create Aisle Spinner ====*/
    public void createSpinner() {
        aisle_spinner = (Spinner) findViewById(R.id.spinner0);
        aisle_spinnerValues = new ArrayList<>(); //ensures spinner values wont duplicate
        int aisle = Integer.parseInt(aisleChecker);
        if (aisle < 0) {

        } else { //Generate Spinner
            for (int i = 1; i <= Integer.parseInt(aisleChecker); i++) {
                aisle_spinnerValues.add(String.valueOf(i));
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, aisle_spinnerValues);
            dataAdapter.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            aisle_spinner.setAdapter(dataAdapter);
        }
    }

    /*=== Create Bay Spinner ====*/
    public void createBaySpinner() {
        bayChecker = "";
        bay_spinner = (Spinner) findViewById(R.id.spinner1);
        String crntAisle = "";
        crntAisle = aisle_spinner.getSelectedItem().toString();

        bay_spinnerValues = new ArrayList<>();
        for (int i = 1; i <= aisleIntChecker; i++) {
            if (i == Integer.parseInt(crntAisle)) {
                bayChecker = bayCheckerList.get(i - 1);
                for (int j = 1; j <= Integer.parseInt(bayChecker); j++) {
                    bay_spinnerValues.add(String.valueOf(j));
                }
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, bay_spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bay_spinner.setAdapter(dataAdapter);
    }

    /*==== Create Shelf Spinner ====*/
    public void createShelfSpinner() {
        String currentShelf = "";
        shelf_spinner = (Spinner) findViewById(R.id.spinner2);
        int crntA = aisle_spinner.getSelectedItemPosition() + 1;
        int crntB = bay_spinner.getSelectedItemPosition() + 1;

        String abshold = "";
        String[] absArr;
        //Find aisle & bay in database that match with spinners and set currentShelf == to num_of_shelves
        for (int i = 0; i < allABS.size(); i++) {
            abshold = allABS.get(i);
            absArr = abshold.split("¿");
            if (Integer.parseInt(absArr[0]) == (crntA) && Integer.parseInt(absArr[1]) == (crntB)) {
                currentShelf = absArr[2];
            }
        }
        //Create an ArrayList num_of_shelves from currentShelf down to 1
        shelf_spinnerValues = new ArrayList<>();
        if (currentShelf.equals("0")) {
            shelf_spinnerValues.add(currentShelf);
        } else {
            for (int i = 1; i <= Integer.parseInt(currentShelf); i++) {
                shelf_spinnerValues.add(String.valueOf(i));
            }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, shelf_spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shelf_spinner.setAdapter(dataAdapter);
    }

    public void sendIntentData(Intent intent) {
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
    }

    public void initializeModifyVariables() {
        checkbox_linearLayout = (LinearLayout)findViewById(R.id.checkbox_linearLayout);
        tvDisplayInfo = (TextView) findViewById(R.id.tvDisplayInfo);
        modify_btn = (Button) findViewById(R.id.modify_btn);
        product_cbox = (CheckBox) findViewById(R.id.product_cbox);
        pid_cbox = (CheckBox) findViewById(R.id.pid_cbox);
        desc_cbox = (CheckBox) findViewById(R.id.desc_cbox);
        stock_cbox = (CheckBox) findViewById(R.id.stock_cbox);
        image_cbox = (CheckBox) findViewById(R.id.image_cbox);
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

    public void modifyButtonListener() {
         /*== Modify search Filters Listener ==*/
        modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hide = "Hide Search Filters";
                String modify = "Modify Search Filters";
                /*== avoid an error ==*/
                if (aisle_spinnerValues.size() == 0) {
                    toastMessage("You must have aisles to modify");
                } else {
                    if (modify_btn.getText().toString().equals(hide)) {
                        modify_btn.setTextColor(Color.BLACK);
                        updateListView();
                        hideCheckBoxes();
                        listView.setVisibility(View.VISIBLE);
                        tvDisplayInfo.setVisibility(View.VISIBLE);
                        modify_btn.setText(modify);
                    } else {
                        showCheckBoxes();
                        modify_btn.setTextColor(Color.RED);
                        modify_btn.setText(hide);
                        listView.setVisibility(View.INVISIBLE);
                        tvDisplayInfo.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    public void updateListView() {
        String pname = "";
        String pid = "";
        String stock = "";
        String desc = "";
        String location = "";
        String dept = "";
        totalNumProducts = 0;
        String findData;
        currentSelectedLocation = new ArrayList<>();
        int curAisle = aisle_spinner.getSelectedItemPosition() + 1;
        int curBay = bay_spinner.getSelectedItemPosition() + 1;
        allImage = new ArrayList<>();
        /*===Find all the products that are assigned to this department===*/
        for (int i = 0; i < productChecker.size(); i++) {
            findData = productChecker.get(i);
            String[] myDeptArr = findData.split("¿");
            /*-- Getting Aisle Product Info Only --*/
            if (bayCbox==0){
                if (myDeptArr[0].trim().equals(String.valueOf(curAisle))){
                    //Aisle Product Info Only
                    totalNumProducts++;
                    if (pCbox == 1) {
                        pname = "Product: " + myDeptArr[3];
                    } else {
                        pname = "";
                    }
                    if (pidCbox == 1) {
                        pid = "\nPID: " + myDeptArr[4];
                    } else {
                        pid = "";
                    }
                    if (stockCbox == 1) {
                        stock = "\nStock: " + myDeptArr[5];
                    } else {
                        stock = "";
                    }
                    if (descCbox == 1) {
                        desc = "\nDescription: " + myDeptArr[6];
                    } else {
                        desc = "";
                    }
                    if (imageCbox==1){
                        allImage.add(myDeptArr[7]);
                    } else {
                        allImage.add(null); // set nothing into imageview
                    }
                    if (locationCbox==1){
                        location="\nLocation: A:"+myDeptArr[0] + " B:" +myDeptArr[1]+" S:"+myDeptArr[2];
                    } else {
                        location="";
                    }
                    if (departmentCbox==1){
                        dept="\nDepartment: " + myDeptArr[8];
                    } else {
                        dept="";
                    }
                    String currentDeptProd = pname + pid + stock + desc + location + dept;
                    currentSelectedLocation.add(currentDeptProd);
                }
            }
            /*-- Getting Aisle & Bay info --*/
            if (bayCbox==1 && shelfCbox==0){
                if (myDeptArr[0].trim().equals(String.valueOf(curAisle)) && myDeptArr[1].trim().equals(String.valueOf(curBay))){
                    //Aisle & Bay Product Info Only
                    totalNumProducts++;
                    if (pCbox == 1) {
                        pname = "Product: " + myDeptArr[3];
                    } else {
                        pname = "";
                    }
                    if (pidCbox == 1) {
                        pid = "\nPID: " + myDeptArr[4];
                    } else {
                        pid = "";
                    }
                    if (stockCbox == 1) {
                        stock = "\nStock: " + myDeptArr[5];
                    } else {
                        stock = "";
                    }
                    if (descCbox == 1) {
                        desc = "\nDescription: " + myDeptArr[6];
                    } else {
                        desc = "";
                    }
                    if (imageCbox==1){
                        allImage.add(myDeptArr[7]);
                    } else {
                        allImage.add(null); // set nothing into imageview
                    }
                    if (locationCbox==1){
                        location="\nLocation: A:"+myDeptArr[0] + " B:" +myDeptArr[1]+" S:"+myDeptArr[2];
                    } else {
                        location="";
                    }
                    if (departmentCbox==1){
                        dept="\nDepartment: " + myDeptArr[8];
                    } else {
                        dept="";
                    }
                    String currentDeptProd = pname + pid + stock + desc + location + dept;
                    currentSelectedLocation.add(currentDeptProd);
                }
            }

            /*-- Getting Aisle & Bay & Shelf info --*/
            if (shelfCbox==1){
                String curShelfz = shelf_spinner.getSelectedItem().toString();
                if (myDeptArr[0].trim().equals(String.valueOf(curAisle)) && myDeptArr[1].trim().equals(String.valueOf(curBay)) && myDeptArr[2].trim().equals(curShelfz)) {
                    //Aisle & Bay & Shelf info
                    totalNumProducts++;
                    if (pCbox == 1) {
                        pname = "Product: " + myDeptArr[3];
                    } else {
                        pname = "";
                    }
                    if (pidCbox == 1) {
                        pid = "\nPID: " + myDeptArr[4];
                    } else {
                        pid = "";
                    }
                    if (stockCbox == 1) {
                        stock = "\nStock: " + myDeptArr[5];
                    } else {
                        stock = "";
                    }
                    if (descCbox == 1) {
                        desc = "\nDescription: " + myDeptArr[6];
                    } else {
                        desc = "";
                    }
                    if (imageCbox==1){
                        allImage.add(myDeptArr[7]);
                    } else {
                        allImage.add(null); // set nothing into imageview
                    }
                    if (locationCbox==1){
                        location="\nLocation: A:"+myDeptArr[0] + " B:" +myDeptArr[1]+" S:"+myDeptArr[2];
                    } else {
                        location="";
                    }
                    if (departmentCbox==1){
                        dept="\nDepartment: " + myDeptArr[8];
                    } else {
                        dept="";
                    }
                    String currentDeptProd = pname + pid + stock + desc + location + dept;
                    currentSelectedLocation.add(currentDeptProd);
                }
            }
        }

        /*===Regenerate the Listview & create it with Products that match current Dept Selected====*/
        if (currentSelectedLocation.size() >= 1) {
            tvDisplayInfo.setText("Displaying " + totalNumProducts + " Product(s)");
            CustomImageList adapter = new CustomImageList(SearchInventoryActivity.this, currentSelectedLocation, allImage, userID);
            listView = (ListView)findViewById(R.id.listViewX);
            listView.setAdapter(adapter);

        } else {
            tvDisplayInfo.setText("Displaying " + totalNumProducts + " Product(s)");
            currentSelectedLocation.add("No Products To Display For This Department!");
            allImage.add(null); // set nothing into imageview
            CustomImageList adapter = new CustomImageList(SearchInventoryActivity.this, currentSelectedLocation, allImage, userID);
            listView = (ListView)findViewById(R.id.listViewX);
            listView.setAdapter(adapter);

        }
    }
    /*==Main Menu btn listener==*/
    public void mainMenuBtnListener(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchInventoryActivity.this, MainMenu.class);
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
