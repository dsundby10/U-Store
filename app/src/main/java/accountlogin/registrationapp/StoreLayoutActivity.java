package accountlogin.registrationapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class StoreLayoutActivity extends AppCompatActivity {
    Draw drawView;
    HorizontalScrollView hsv;
    ConstraintLayout c_layout;
    Button btn;
    TextView shelfKey;
    //add firebase stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference BayRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference AisleRef;
    private DatabaseReference shelfRef;
    private String userID;

    ArrayList<String> ABSarr = new ArrayList<>();
    ArrayList<String> Aarr = new ArrayList<String>();
    ArrayList<String> Barr = new ArrayList<String>();
    ArrayList<String> Sarr = new ArrayList<String>();
    ArrayList<String> findMaxBay = new ArrayList<>();

    ArrayList<Integer> intMaxBay = new ArrayList<>();

    ArrayList<String>  zAisle = new ArrayList<>();
    ArrayList<String>  zBay = new ArrayList<>();
    ArrayList<Integer> mySortedMaxBays = new ArrayList<>();
    ArrayList<Integer> mySortedMaxAisles = new ArrayList<>();
    ArrayList<Integer> sortCrntBays = new ArrayList<Integer>();
    ArrayList<Integer> sortCrntAisles = new ArrayList<Integer>();

    int currentBay = 0;
    int currentShelf = 0;
    int currentAisle= 0;
    String currentABS = "";
    String maxAisles = "";
    String maxBays = "";

    int amaxAisles = 0;
    int amaxBays = 0;

    int l = 50;
    int t = 50;
    int r = 150;
    int b = 150;

    int ICOUNT=1;
    int nextAisle= 1;

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

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
        AisleRef = mFirebaseDatabase.getReference().child(userID);
        AisleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                maxAisles = "";
                amaxAisles = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    if (data.getKey().equals("aisles")){
                        maxAisles = data.getValue().toString();
                        amaxAisles = Integer.parseInt(data.getValue().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

            BayRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
           //BayRef.addValueEventListener(new ValueEventListener() {
            BayRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //if(!this.getClass().equals(StoreLayoutActivity.class)){
                    // BayRef.removeEventListener(this);
                    //}
                    intMaxBay = new ArrayList<Integer>();
                    mySortedMaxBays = new ArrayList<Integer>();
                    mySortedMaxAisles = new ArrayList<Integer>();
                    amaxBays = 0;
                    int size = 0;
                    zAisle = new ArrayList<>();
                    zBay = new ArrayList<>();

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                            intMaxBay.add(Integer.parseInt(data.child("bay_num").getValue().toString()));
                            String tAisle = data.child("aisle_num").getValue().toString();
                            String tBay = data.child("bay_num").getValue().toString();
                            zAisle.add(tAisle);
                            zBay.add(tBay);
                    }
                    int crntSize;
                    ICOUNT = 1;
                    sortCrntBays = new ArrayList<Integer>();
                    for (int i = 0; i < zAisle.size(); i++) {
                        if (i == zAisle.size() - 1) {
                            Collections.sort(sortCrntBays);
                            crntSize = sortCrntBays.size() + 1;
                            mySortedMaxBays.add(crntSize);
                            mySortedMaxAisles.add(ICOUNT);
                            sortCrntBays.clear();
                            ICOUNT++;
                            sortCrntBays.add(Integer.parseInt(zBay.get(i)));
                        } else {
                            if (Integer.parseInt(zAisle.get(i)) != ICOUNT) {
                                crntSize = sortCrntBays.size();
                                Collections.sort(sortCrntBays);
                                mySortedMaxBays.add(crntSize);
                                mySortedMaxAisles.add(ICOUNT);
                                sortCrntBays.clear();
                                ICOUNT++;
                                sortCrntBays.add(Integer.parseInt(zBay.get(i)));
                            } else {
                                sortCrntBays.add(Integer.parseInt(zBay.get(i)));

                            }
                        }
                    }
                    System.out.println(mySortedMaxBays.size());
                    for (int i = 0; i < mySortedMaxBays.size(); i++) {
                        System.out.println(mySortedMaxBays.get(i));
                    }
                    // Get the MaxBay (for screen sizing)
                    Collections.sort(intMaxBay);
                    size = intMaxBay.size();
                    if (size != 0) {
                        amaxBays = intMaxBay.get(size - 1);
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
                ABSarr = new ArrayList<>();
                Aarr = new ArrayList<>();
                Barr = new ArrayList<>();
                Sarr = new ArrayList<>();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String aisleNum = (String) childSnapShot.child("aisle_num").getValue();
                    String bay_num = (String) childSnapShot.child("bay_num").getValue();
                    String num_of_shelves = (String) childSnapShot.child("num_of_shelves").getValue();
                    String shelfSet = aisleNum +","+ bay_num +","+  num_of_shelves;
                    System.out.println("Second shelfSetup: " + shelfSet);
                    ABSarr.add(shelfSet);
                    Aarr.add(aisleNum);
                    Barr.add(bay_num);
                    Sarr.add(num_of_shelves);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //setContentView(R.layout.activity_store_layout);
        //c_layout = (ConstraintLayout) findViewById(R.id.c_layout);
        //HorizontalScrollView hsv = (HorizontalScrollView)findViewById(R.id.hsv);
        //hsv = (HorizontalScrollView)findViewById(R.id.hsv);
        //drawView = new Draw(this); //Create a new instance of your drawview class
        //hsv.addView(drawView);
        updateLayout();
        btn = (Button)findViewById(R.id.btn);
        shelfKey = (TextView)findViewById(R.id.shelfKey);

        placeButton();

    }
    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
    }

    public void updateLayout(){


        setContentView(R.layout.activity_store_layout);
        hsv = (HorizontalScrollView)findViewById(R.id.hsv);

        drawView = new Draw(this); //Create a new instance of your drawview class
        hsv.addView(drawView);
    }
    public void placeShelfKey(int xPosition, int yPosition){
        int xPos = xPosition * 25;
        int yPos = yPosition * 200;
        shelfKey.setText("Shelf Color Key\nShelf 1: Blue\nShelf 2: Red\nShelf 3:Green");
        shelfKey.setX(xPos);
        shelfKey.setY(yPos);
    }

    public void placeButton(){

        AisleRef = mFirebaseDatabase.getReference().child(userID);
        //AisleRef.addListenerForSingleValueEvent(new ValueEventListener() {
        AisleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                maxAisles = "";
                amaxAisles = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    if (data.getKey().equals("aisles")){
                        maxAisles = data.getValue().toString();
                        amaxAisles = Integer.parseInt(maxAisles);
                    }
                }
                btn.setVisibility(View.INVISIBLE);
               // btn = (Button)findViewById(R.id.btn);
               // int v = amaxAisles*50;
               // btn.setX(v);
               // btn.setY(400);
                placeShelfKey(amaxAisles,amaxBays);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public class Draw extends View {
        Paint paint = new Paint();
        public Draw(Context context) {
            super(context);
        }
        public Draw(Context context, AttributeSet attrs) {super(context, attrs);}
        public Draw(Context context, AttributeSet attrs, int defStyle) {super(context, attrs, defStyle);}

        @Override
        public void onDraw(Canvas canvas) {
            int spacing = 50;
            int aisleCounter = 1;
            int bayTop = 50;
            int bayBottom = 175;
            int bayLeft = 50;
            int bayRight = 175;

            int spaceBay = 5;
            int baySpacer = (bayBottom - bayTop) + spaceBay;

            int spaceAisle = 10;
            int aisleSpacer = (bayRight - bayLeft) + spaceAisle;

            nextAisle = 1;
            String[] splitABS = new String[3];
            for (int i = 0; i < ABSarr.size(); i++) {
                currentABS = ABSarr.get(i);
                splitABS = currentABS.split(",");
                currentAisle = Integer.parseInt(splitABS[0]);
                currentBay = Integer.parseInt(splitABS[1]);
                currentShelf = Integer.parseInt(splitABS[2]);

                if (currentAisle == mySortedMaxAisles.get(nextAisle-1)) {

                    t = (baySpacer * currentBay) + bayTop;
                    b = (baySpacer * currentBay) + bayBottom;
                    l = (aisleSpacer * currentAisle) + bayLeft;
                    r = (aisleSpacer * currentAisle) + bayRight;

                    //Draw the Bay
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(l, t, r, b, paint);

                    //Draw the Shelves
                    drawShelves(canvas, currentShelf,l, t, r, b);
                    if (currentBay==1) {
                        paint.setColor(Color.BLACK);
                        paint.setTextSize(40);
                        canvas.drawText("A: " + aisleCounter, l + ((r-l)/4), 150, paint);
                        aisleCounter++;
                    }

                } else {
                    t = (baySpacer * currentBay) + bayTop;
                    b = (baySpacer * currentBay) + bayBottom;
                    l = (aisleSpacer * currentAisle) + bayLeft;
                    r = (aisleSpacer * currentAisle) + bayRight;
                    //Draw the Bay
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(l, t, r, b, paint);

                    //Draw the Shelves
                    drawShelves(canvas, currentShelf,l, t, r, b);
                    nextAisle++;

                    if (currentBay==1) {
                        paint.setColor(Color.BLACK);
                        paint.setTextSize(40);
                        canvas.drawText("A: " + aisleCounter, l+ ((r-l)/4), 150, paint);
                        aisleCounter++;
                    }
                }
            }
        }

        @Override
        /*Determine the size of Canvas based on maxAisles & maxBays
          along with the initial starting bottom and right dimensions*/
        public void onMeasure(int widthMeasureSpe, int heightMeasureSpe) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            setMeasuredDimension(2000,2000);
           //setMeasuredDimension((amaxAisles*200)+50, (amaxBays*205) + 300);
        }
    }

    public void drawShelves(Canvas canvas, int currentShelf, int l, int t, int r, int b){
        Paint shelf1 = new Paint();
        Paint shelf2 = new Paint();
        Paint shelf3 = new Paint();
        Paint shelf4 = new Paint();
        Paint shelf5 = new Paint();
        Paint shelf6 = new Paint();
        Paint shelf7 = new Paint();
        shelf1.setColor(Color.BLUE);
        shelf2.setColor(Color.RED);
        shelf3.setColor(Color.GREEN);
        shelf4.setColor(Color.CYAN);
        shelf5.setColor(Color.MAGENTA);
        shelf6.setColor(Color.YELLOW);
        shelf7.setColor(Color.DKGRAY);
        int drawLine=0;
        int drawLineEnd = 0;
        int shelfDraw = 0;
        int shelfCount = 1;

        for (int i = 0; i < this.currentShelf ; i++) {
            shelfDraw = ((this.r - this.l) / (this.currentShelf + 1));
            drawLine = this.l + (shelfDraw * shelfCount) - 2;
            drawLineEnd = this.l + (shelfDraw * shelfCount) + 2;
            if (i == 0){
                canvas.drawRect(drawLine, this.t, drawLineEnd, this.b, shelf1);
            }
            if (i==1){
                canvas.drawRect(drawLine, this.t, drawLineEnd, this.b, shelf2);
            }
            if (i==2){
                canvas.drawRect(drawLine, this.t, drawLineEnd, this.b, shelf3);
            }
            if (i==3) {
                canvas.drawRect(drawLine, this.t, drawLineEnd, this.b, shelf4);
            }
            if (i==4){
                canvas.drawRect(drawLine, this.t, drawLineEnd, this.b, shelf5);
            }
            if (i==5){
                canvas.drawRect(drawLine, this.t, drawLineEnd, this.b, shelf6);
            }
            if (i==6){
                canvas.drawRect(drawLine, this.t, drawLineEnd, this.b, shelf7);
            }
            shelfCount++;
        }

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