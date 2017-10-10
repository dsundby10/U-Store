package accountlogin.registrationapp;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class AddInventory extends AppCompatActivity {
    static List<String> arrMaxAisles = new ArrayList<>(); //place holder for the Array
    EditText product_name, num_stock, product_id, product_desc;
    Spinner dept_spinner, aisle_spinner, bay_spinner, shelf_spinner;
    Button add_product_btn, take_image_btn, upload_image_btn;


    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory);
        Intent intent = getIntent();

        //Variable initialization
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
        aisle_spinner = (Spinner)findViewById(R.id.aisle_spinner);
        bay_spinner = (Spinner)findViewById(R.id.bay_spinner);
        shelf_spinner = (Spinner)findViewById(R.id.shelf_spinner);

        product_name = (EditText)findViewById(R.id.product_name);
        num_stock = (EditText)findViewById(R.id.num_stock);
        product_id = (EditText)findViewById(R.id.product_id);
        product_desc = (EditText)findViewById(R.id.product_desc);

        add_product_btn = (Button)findViewById(R.id.add_product_btn);
        take_image_btn = (Button)findViewById(R.id.take_image_btn);
        upload_image_btn = (Button)findViewById(R.id.upload_image_btn);

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
                    // user auth state is changed - user is null
                    // launch login activity
                    //startActivity(new Intent(MainActivity.this, LoginActivity.class));

                }
            }
        };
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            //https://stackoverflow.com/questions/42257480/retrieving-nested-data-in-firebase-android
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        take_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddInventory.this, EditStoreAndDepartmentActivity.class);
                startActivity(intent);
            }
        });
        add_product_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // String x = getSpinnerValues();
               // String[] xx = x.split("\\s*,\\s*");
                String p_id = product_id.getText().toString();
                String p_name = product_name.getText().toString();
                String stock_amt = num_stock.getText().toString();
                String p_desc = product_desc.getText().toString();
                
            }
        });
    }
    private void showData(DataSnapshot dataSnapshot) {
        String str = ""; //placeHolder for maxAisles
        String deptListStr="";
        String[] deptListArr;
        zAllUserData zInfo = new zAllUserData();
        for(DataSnapshot ds : dataSnapshot.getChildren()) {


            zInfo.setDeptNames(ds.child("deptNames").getValue(String.class));

            //Set the non separated department list taken from database to deptListStr
            deptListStr = zInfo.getDeptNames();

            //Split the string at the commas and extra spaces & place into deptListArr
            deptListArr = deptListStr.split("\\s*,\\s*");
            //Create a new ArrayList and get add each value from deptListArr to deptArrList
            ArrayList<String> deptArrList = new ArrayList<>();
            for (int i=0; i<deptListArr.length; i++){
                deptArrList.add(deptListArr[i]);
            }
            //Load deptArrList into the department spinner
            ArrayAdapter arrayAdapter = new ArrayAdapter(AddInventory.this,android.R.layout.simple_spinner_dropdown_item, deptArrList);
            dept_spinner.setAdapter(arrayAdapter);

            //Get current number of aisles from database & set equal to maxAisles
            zInfo.setNumAisles(ds.child("aisles").getValue(String.class));
            int maxAisles = Integer.parseInt(zInfo.getNumAisles());

            ArrayList<String> arrListMaxAisles = new ArrayList<>();
            //loop through and add num of aisles from 0 to maxAisles to the arrListMaxAisles
            for (int i=0; i < maxAisles; i++) {
                arrListMaxAisles.add(i,"Aisle: " + str.valueOf(i));
            }
            //Load arrListMaxAisles to the Aisle Spinner
            ArrayAdapter arrayAdapter1 = new ArrayAdapter(AddInventory.this,android.R.layout.simple_spinner_dropdown_item, arrListMaxAisles);
            aisle_spinner.setAdapter(arrayAdapter1);

            //Get current number of bays from database & set equal to maxBays
            zInfo.setNumBays(ds.child("genBays").getValue(String.class));
            int maxBays = Integer.parseInt(zInfo.getNumBays());

            ArrayList<String> arrListMaxBays = new ArrayList<>();
            //loop through and add num of bays from 0 to maxBays to the arrListMaxBays
            for (int i=0; i < maxBays; i++) {
                arrListMaxBays.add(i,"Bay: " + str.valueOf(i));
            }
            //Load arrListMaxBays to the Bay Spinner
            ArrayAdapter arrayAdapter2 = new ArrayAdapter(AddInventory.this,android.R.layout.simple_spinner_dropdown_item, arrListMaxBays);
            bay_spinner.setAdapter(arrayAdapter2);

        }

    }
    public String getSpinnerValues(){
        aisle_spinner = (Spinner)findViewById(R.id.aisle_spinner);
        String aisleSpinTxt = aisle_spinner.getSelectedItem().toString();
        //aisle_spin_int = Integer.parseInt(aisleSpinTxt);

        bay_spinner = (Spinner)findViewById(R.id.bay_spinner);
        String baySpinTxt = bay_spinner.getSelectedItem().toString();

        shelf_spinner = (Spinner)findViewById(R.id.shelf_spinner);
        String shelfSpinTxt = shelf_spinner.getSelectedItem().toString();

        return aisleSpinTxt +","+baySpinTxt+","+shelfSpinTxt;
    }

}
