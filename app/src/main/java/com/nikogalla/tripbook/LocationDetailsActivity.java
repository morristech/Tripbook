package com.nikogalla.tripbook;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.nikogalla.tripbook.data.FirebaseHelper;
import com.nikogalla.tripbook.data.LocationContract;
import com.nikogalla.tripbook.data.LocationDbHelper;
import com.nikogalla.tripbook.models.Comment;
import com.nikogalla.tripbook.models.Location;
import com.nikogalla.tripbook.prefs.SettingsActivity;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationDetailsActivity extends AppCompatActivity {
    private final String TAG = LocationDetailsActivity.class.getSimpleName();
    private Context mContext;
    @BindView(R.id.tbLocationDetails)
    Toolbar tbLocationDetails;
    @BindView(R.id.ivLocationPicture)
    ImageView ivLocationPicture;
    @BindView(R.id.tvLocationRating)
    TextView tvLocationRating;
    @BindView(R.id.tvLocationComments)
    TextView tvLocationComments;
    @BindView(R.id.tvLocationDescription)
    TextView tvLocationDescription;
    @BindView(R.id.tvLocationAddress)
    TextView tvLocationAddress;
    @BindView(R.id.llComments)
    LinearLayout llComments;
    @BindView(R.id.llRates)
    LinearLayout llRates;
    @BindView(R.id.ibDirections)
    ImageButton ibDirections;
    FirebaseDatabase mDatabase;
    private String mLocationKey;
    Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        ButterKnife.bind(this);
        mContext = this;
        mDatabase = FirebaseHelper.getDatabase();
        mLocationKey = getIntent().getStringExtra(getString(R.string.location_key_id));
        String locationTitle = getIntent().getStringExtra(getString(R.string.location_name_id));
        setSupportActionBar(tbLocationDetails);;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(locationTitle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference locationRef = mDatabase.getReference(Location.LOCATION_TABLE_NAME);
        locationRef.orderByChild("key").equalTo(mLocationKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mLocation = dataSnapshot.getValue(Location.class);
                loadLocationPicture();
                loadLocationInfos();
                llRates.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext,RateActivity.class);
                        intent.putExtra(getString(R.string.location_id),mLocation);
                        startActivity(intent);
                    }
                });
                llComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext,CommentActivity.class);
                        intent.putExtra(getString(R.string.location_id),mLocation);
                        startActivity(intent);
                    }
                });
                ibDirections.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptGoogleMapsLaunch();
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Toast.makeText(mContext, R.string.location_removed,Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadLocationInfos(){
        tvLocationAddress.setText(mLocation.address);
        tvLocationDescription.setText(mLocation.description);
        float rating = mLocation.getRate();
        if (rating>0){
            tvLocationRating.setText(mLocation.getRateString(mContext));
        }
        if (mLocation.comments.size()>0){
            tvLocationComments.setText(mLocation.comments.size() + " " + mContext.getString(R.string.comments));
        }

    }

    private void loadLocationPicture(){
        try{
            String photoUrl = mLocation.getMainPhotoUrl();
            Picasso.with(mContext).load(photoUrl).into(ivLocationPicture);
        }catch (Exception e){
            Log.d(TAG,"No photo for location: " + mLocation.name + " " + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_location:
                deleteCurrentLocation();
                return true;
            case android.R.id.home:
                supportFinishAfterTransition();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.current_location, menu);
        return true;
    }

    public void deleteCurrentLocation(){
        DatabaseReference locationRef = mDatabase.getReference(Location.LOCATION_TABLE_NAME);
        locationRef.child(mLocationKey).removeValue();
    }

    public void attemptGoogleMapsLaunch(){
        // Create a Uri from an intent string. Use the result to create an Intent.

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+ mLocation.latitude
                +","+ mLocation.longitude+"");

// Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
// Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

// Attempt to start an activity that can handle the Intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }

    }
}
