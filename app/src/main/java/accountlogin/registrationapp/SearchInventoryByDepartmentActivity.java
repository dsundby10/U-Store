package accountlogin.registrationapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class SearchInventoryByDepartmentActivity extends AppCompatActivity {
    Spinner dept_spinner;
    ListView listView;
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

    String deptString = "";
    String[] deptArr;

    ArrayList<String> dept_spinnerValues = new ArrayList<>();
    ArrayList<String> currentDeptProductInfo = new ArrayList<>();
    ArrayList<String> productChecker = new ArrayList<>();

    String deptProductInfo = "";
    String currentDept = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_inventory_by_department);
        setTitle("Search by Department");
        Intent intent = getIntent();
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
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

          /*==== Product listener - pulling all the stores product info and forming it into an arrayList ====*/
        ABS = mFirebaseDatabase.getReference().child(userID).child("Products");
        ABS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productChecker = new ArrayList<String>();
                deptProductInfo = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_dept = data.child("P_Dept").getValue().toString();
                    String p_name = data.child("P_Name").getValue().toString();
                    // the ¿ servers as the element at which I split the string at to form a string (probably isnt the right way, but easiest?)
                    deptProductInfo = p_dept+ "¿" + p_name;
                    productChecker.add(deptProductInfo);
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
                currentDept = dept_spinner.getItemAtPosition(position).toString();
                String findDept;
                String[] myDeptArr;
                currentDeptProductInfo = new ArrayList<>();
                /*===Find all the products that are assigned to this department===*/
                for (int i = 0; i < productChecker.size(); i++) {
                    findDept = productChecker.get(i);
                    myDeptArr = findDept.split("¿");
                    if (myDeptArr[0].equals(currentDept)){
                        String currentDeptProd = "Department: " + myDeptArr[0] + " Product: " + myDeptArr[1];
                        currentDeptProductInfo.add(currentDeptProd);
                        //Log.i("tcurrentDeptProd ", currentDeptProd);
                    }
                }
                /*===Regenerate the Listview & create it with Products that match current Dept Selected====*/
                if (currentDeptProductInfo.size() >= 1) {
                    listView = (ListView) findViewById(R.id.listView);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(SearchInventoryByDepartmentActivity.this, android.R.layout.simple_list_item_1, currentDeptProductInfo);
                    listView.setAdapter(arrayAdapter);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*======= Aisle DB Reference =======*/
        AisleBayShelfRef = mFirebaseDatabase.getReference().child(userID);
        AisleBayShelfRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deptString = "";
                dept_spinnerValues = new ArrayList<String>();
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    /*=== If User has an Existing Aisle Entry in the Database ===*/
                    if (data.getKey().equals("deptNames") && !data.getValue().toString().trim().equals("")) {
                        deptString = data.getValue().toString();
                        Log.i("bCurrentdeptnames: " , data.getValue().toString());
                    }
                }
                createDeptSpinner();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
/*===Generate Dept Spinner Values ====*/
public void createDeptSpinner() {
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
}


