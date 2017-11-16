package accountlogin.registrationapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchInventoryByKeyword extends AppCompatActivity {
    Button search_btn;
    EditText keywordSearch;
    ListView listView;

    //add firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference ProductRef;
    private String userID;

    ArrayList<String> productChecker = new ArrayList<>();
    String deptProductInfo = "";

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_inventory_by_keyword);
        setTitle("Search Products by Keyword");

        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        search_btn = (Button)findViewById(R.id.search_btn);
        keywordSearch = (EditText)findViewById(R.id.keywordText);
        listView = (ListView)findViewById(R.id.listView);


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
        ProductRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productChecker = new ArrayList<String>();
                deptProductInfo = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_dept = data.child("P_Dept").getValue().toString();
                    String p_name = data.child("P_Name").getValue().toString();
                    // the ¿ servers as the element at which I split the string at to form a string (probably isnt the right way, but easiest?)
                    deptProductInfo = p_name;
                    productChecker.add(deptProductInfo);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = keywordSearch.getText().toString();
                String currentProduct = "";
                ArrayList<String>showProducts = new ArrayList<String>();
                for (int i = 0; i < productChecker.size(); i++) {
                    currentProduct = productChecker.get(i);
                   //if (currentProduct.toLowerCase().equals(keyword.toLowerCase())){
                        //showProducts.add(currentProduct);
                   //}
                    if (currentProduct.toLowerCase().contains(keyword.toLowerCase())){
                        showProducts.add(currentProduct);
                    }
                }
                if (showProducts.size() != 0){
                    listView = (ListView) findViewById(R.id.listView);
                    ArrayAdapter emptyAdapter = new ArrayAdapter(SearchInventoryByKeyword.this, android.R.layout.simple_list_item_1, showProducts);
                    listView.setAdapter(emptyAdapter);
                } else {
                    showProducts.add("No results match your keyword.");
                    listView = (ListView) findViewById(R.id.listView);
                    ArrayAdapter emptyAdapter = new ArrayAdapter(SearchInventoryByKeyword.this, android.R.layout.simple_list_item_1, showProducts);
                    listView.setAdapter(emptyAdapter);
                }

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


