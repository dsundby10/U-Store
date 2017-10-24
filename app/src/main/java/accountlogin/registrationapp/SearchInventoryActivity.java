package accountlogin.registrationapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
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
    ArrayList<String> bay_spinnerValues = new ArrayList<>();
    ArrayList<String> bayCheckerList = new ArrayList<>();
    String bayChecker = "";

    ArrayList<String> shelf_spinnerValues = new ArrayList<>();
    ArrayList<String> shelfCheckerList = new ArrayList<>();

    ArrayList<String> productChecker = new ArrayList<>();
    String allABSProductInfo = "";

    ArrayList<String> currentAisleProductInfo = new ArrayList<>();
    ArrayList<String> currentBayProductInfo = new ArrayList<>();
    ArrayList<String> currentShelfProductInfo = new ArrayList<>();

    int currentAisleSpinner = 0;
    int currentBaySpinner = 0;
    int currentShelfSpinner = 0;
    ArrayList<String> aisleCheckerList = new ArrayList<String>();
    ArrayList<String> bayCheckerListz = new ArrayList<String>();

    ArrayList<String> allABS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_inventory);
        setTitle("Search Inventory: Location");
        Intent intent = getIntent();

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        //variable initialization
        main_menu_btn = (Button)findViewById(R.id.view_layout_btn);
        aisle_spinner = (Spinner) findViewById(R.id.spinner0);
        bay_spinner = (Spinner) findViewById(R.id.spinner1);
        shelf_spinner = (Spinner) findViewById(R.id.spinner2);
        checkBox0 = (CheckBox)findViewById(R.id.checkBox0);
        checkBox1 = (CheckBox)findViewById(R.id.checkBox1);
        checkBox2 = (CheckBox)findViewById(R.id.checkBox2);
        listView = (ListView) findViewById(R.id.listviewX);
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
        ABS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!this.getClass().equals(SearchInventoryActivity.class)){
                    Log.i("REMOVING ProductRef", "");
                    ABS.removeEventListener(this);
                }
                productChecker = new ArrayList<String>();
                allABSProductInfo = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_aisle = data.child("P_Aisle").getValue().toString();
                    String p_bay = data.child("P_Bay").getValue().toString();
                    String p_shelf =  data.child("P_Shelf").getValue().toString();
                    String p_name = data.child("P_Name").getValue().toString();
                    // the ¿ servers as the element at which I split the string at to form a string (probably isnt the right way, but easiest?)
                    allABSProductInfo = p_aisle + "¿" + p_bay + "¿"+p_shelf + "¿" + p_name;
                    productChecker.add(allABSProductInfo);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*=====Aisle spinnner listener ====*/
        aisle_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                createBaySpinner();
                currentAisleSpinner = position;
                String findAisle = "";
                String[] myAisleArr;
                currentAisleProductInfo = new ArrayList<String>();
                for (int i = 0; i < productChecker.size(); i++) {
                    findAisle=productChecker.get(i);
                    myAisleArr = findAisle.split("¿");
                    if (Integer.parseInt(myAisleArr[0])==currentAisleSpinner){
                        /*=== Load Product info into the appropriate Aisle Spinner thats selected ===*/
                        String currentAisleBS = "Location: " + myAisleArr[0] + " Product: " + myAisleArr[3];
                        currentAisleProductInfo.add(currentAisleBS);
                    }
                }
                    /*===Regenerate the Listview & create it with Products that match Aisle ====*/
                    listView = (ListView)findViewById(R.id.listviewX);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(SearchInventoryActivity.this,android.R.layout.simple_list_item_1,currentAisleProductInfo);
                    listView.setAdapter(arrayAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        /*======= Bay Spinner Listener =====*/
        bay_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentBaySpinner = position;
                String findBay = "";
                String[] myBayArr;
                currentBayProductInfo = new ArrayList<String>();
                for (int i = 0; i <productChecker.size() ; i++) {
                    findBay=productChecker.get(i);
                    myBayArr = findBay.split("¿");
                    if (Integer.parseInt(myBayArr[0])==currentAisleSpinner && Integer.parseInt(myBayArr[1]) == currentBaySpinner) {
                        String currentABayS = "Location: " + myBayArr[0] + myBayArr[1] +" Product: " + myBayArr[3];
                        currentBayProductInfo.add(currentABayS);
                       // Log.i("tcurrentBay", myBayArr[0] + myBayArr[1] + myBayArr[3]);
                    }
                }
                /*===Regenerate the Listview & create it with Products that match Aisle bay ====*/
                listView = (ListView)findViewById(R.id.listviewX);
                ArrayAdapter arrayAdapter = new ArrayAdapter(SearchInventoryActivity.this,android.R.layout.simple_list_item_1,currentBayProductInfo);
                listView.setAdapter(arrayAdapter);
                createShelfSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*====Shelf Spinner Listener ====*/
        shelf_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentShelfSpinner = position;
                String findShelf;
                String[] myShelfArr;
                currentShelfProductInfo = new ArrayList<String>();

                //checking all the stores products
                for (int i = 0; i <productChecker.size(); i++) {
                    findShelf=productChecker.get(i);
                    myShelfArr = findShelf.split("¿");
                    if (Integer.parseInt(myShelfArr[0])==currentAisleSpinner && Integer.parseInt(myShelfArr[1]) == currentBaySpinner && Integer.parseInt(myShelfArr[2]) == currentShelfSpinner) {
                        String currentABShelf ="Location: "+ myShelfArr[0] + " "+ myShelfArr[1] + " "+ myShelfArr[2] + " Product: "+ myShelfArr[3];
                        currentShelfProductInfo.add(currentABShelf);
                       // Log.i("tcurrentBay", myShelfArr[0] + " "+ myShelfArr[1] + " "+ myShelfArr[2] + " "+ myShelfArr[3]);
                    }
                }
                /*===Regenerate the Listview & create it with Products that match Aisle bay shelf====*/
                listView = (ListView)findViewById(R.id.listviewX);
                ArrayAdapter arrayAdapter = new ArrayAdapter(SearchInventoryActivity.this,android.R.layout.simple_list_item_1,currentShelfProductInfo);
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*=====Aisle Check Box Listener ======= */
        checkBox0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             if (isChecked){
                 createSpinner();
                 checkBox1.setVisibility(View.VISIBLE);

             } else {
                 //aisle_spinner.setEnabled(false);
                 checkBox1.setVisibility(View.GONE);
                 checkBox2.setVisibility(View.GONE);
                 checkBox1.setChecked(false);
                 checkBox2.setChecked(false);

             }
         }
     });
        /*=====Bay Check Box Listener ======= */
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    checkBox2.setVisibility(View.VISIBLE);
                    createBaySpinner();

                } else {
                    //bay_spinner.setEnabled(false);
                    //shelf_spinner.setEnabled(false);
                    checkBox2.setChecked(false);
                    checkBox2.setVisibility(View.GONE);

                }
            }
        });
        /*=====Shelf Check Box Listener ======= */
        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    createShelfSpinner();
                } else {
                    //shelf_spinner.setEnabled(false);
                    checkBox2.setChecked(false);
                    checkBox2.setVisibility(View.GONE);

                }
            }
        });

         /*======= Aisle DB Reference =======*/
        AisleBayShelfRef = mFirebaseDatabase.getReference().child(userID);
        AisleBayShelfRef.addValueEventListener(new ValueEventListener() {
            String strHold;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    /*=== If User has an Existing Aisle Entry in the Database ===*/
                    if (data.getKey().equals("aisles") && !data.getValue().toString().trim().equals("")) {
                        aisleChecker = data.getValue().toString();
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
                if(!this.getClass().equals(SearchInventoryActivity.class)){
                    Log.i("REMOVING ABS LISTENER!", "");
                    ABS.removeEventListener(this);
                }
                int count  = 0;
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

                    Log.i("zxo: shelfCheck: ", allABS.get(count));
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
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchInventoryActivity.this, MainMenu.class);
                finish(); //testing
                startActivity(intent);

            }
        });
    }

    //Create Aisle Spinner
    public void createSpinner() {
        aisle_spinner = (Spinner) findViewById(R.id.spinner0);
        aisle_spinnerValues = new ArrayList<>(); //ensures spinner values wont duplicate
        if (Integer.parseInt(aisleChecker) < 0) {
           // Log.i("Checking Aisle in DB: ", "There's no value!");
        } else { //Generate Spinner
            for (int i = 0; i < Integer.parseInt(aisleChecker); i++) {
                String x = String.valueOf(i);
                aisle_spinnerValues.add(x);
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, aisle_spinnerValues);
            dataAdapter.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            aisle_spinner.setAdapter(dataAdapter);
        }
    }
    //Create Bay Spinner
    public void createBaySpinner(){
        bay_spinner = (Spinner)findViewById(R.id.spinner1);
        currentAisleSpinner = aisle_spinner.getSelectedItemPosition();
        bay_spinnerValues = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(aisleChecker); i++) {
            if (i == currentAisleSpinner) {
                bayChecker = bayCheckerList.get(i);
                for (int j = 0; j < Integer.parseInt(bayChecker); j++) {
                    bay_spinnerValues.add(String.valueOf(j));
                }
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, bay_spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bay_spinner.setAdapter(dataAdapter);
    }
    //Create Shelf Spinner
    public void createShelfSpinner(){
        String currentShelf = "";
        shelf_spinner = (Spinner)findViewById(R.id.spinner2);
        String crntA = aisle_spinner.getSelectedItem().toString();
        String crntB= bay_spinner.getSelectedItem().toString();
        Log.i("xcv: CrntAisleBay:", crntA+ " "+ crntB);

        String abshold = "";
        String[] absArr;
        for (int i = 0; i < allABS.size(); i++) {
            abshold = allABS.get(i);
            absArr = abshold.split("¿");
            if (absArr[0].equals(crntA) && absArr[1].equals(crntB)){
                currentShelf = absArr[2];
                Log.i("xcv:SHELFCURRRENT", currentShelf);
            }
        }
            shelf_spinnerValues = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(currentShelf) ; i++) {
            shelf_spinnerValues.add(String.valueOf(i));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, shelf_spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shelf_spinner.setAdapter(dataAdapter);
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
