package anna.tcc.atlasindoor;

import android.Manifest;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.indooratlas.android.sdk.resources.IAResult;
import com.indooratlas.android.sdk.resources.IAResultCallback;
import com.indooratlas.android.sdk.resources.IATask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements IALocationListener, IARegion.Listener {
    private final int CODE_PERMISSIONS = 1;

    private IALocationManager mIALocationManager;
    private IAResourceManager mResourceManager;
    private ImageView mFloorPlanImage;

    private static final String TAG = "desenvolvimento2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("desenvolvimento", "*** 1 *************");
        obtemPermissaoDoUsuario();
        Log.d("desenvolvimento", "*** 2 *************");
        mIALocationManager = IALocationManager.create(this);
        Log.d("desenvolvimento", "*** 3 *************");
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), this);
        mIALocationManager.registerRegionListener(this);

        mFloorPlanImage = (ImageView) findViewById(R.id.image);
        mResourceManager = IAResourceManager.create(this);
    }

    private void obtemPermissaoDoUsuario() {
        String[] neededPermissions = {
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        ActivityCompat.requestPermissions(this, neededPermissions, CODE_PERMISSIONS);
    }

    @Override
    public void onLocationChanged(IALocation localizacao) {
        Log.d("desenvolvimento", "*** 4 *");
        Log.d("desenvolvimento", "Latitude: " + localizacao.getLatitude());
        Log.d("desenvolvimento", "Longitude: " + localizacao.getLongitude());
        Log.d("desenvolvimento", "Floor number: " + localizacao.getFloorLevel());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("desenvolvimento", "*** 5 : " + s + " ** " + i + " ** " + bundle);
    }

    @Override
    public void onEnterRegion(IARegion region) {
        Log.d(TAG, "*** 1 : " + region.getName());
        if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
            // triggered when entering the mapped area of the given floor plan
            Log.d(TAG, "*** 2 : Entered " + region.getName());
            Log.d(TAG, "*** 3 : floor plan ID: " + region.getId());
            fetchFloorPlan(region.getId());
        } else if (region.getType() == IARegion.TYPE_VENUE) {
            // triggered when near a new location
            Log.d(TAG, "*** 4 : Location changed to " + region.getName());
        }
    }

    @Override
    public void onExitRegion(IARegion region) {
        Log.d(TAG, "*** 5 : " + region.getName());
    }

    private void fetchFloorPlan(String id) {
        IATask<IAFloorPlan> mPendingAsyncResult = mResourceManager.fetchFloorPlanWithId(id);
        if (mPendingAsyncResult != null) {
            mPendingAsyncResult.setCallback(new IAResultCallback<IAFloorPlan>() {
                @Override
                public void onResult(IAResult<IAFloorPlan> result) {
                    Log.d(TAG, "onResult: %s" + result);

                    if (result.isSuccess()) {
                        handleFloorPlanChange(result.getResult());
                    } else {
                        // do something with error
                        Toast.makeText(MainActivity.this,
                                "loading floor plan failed: " + result.getError(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }, Looper.getMainLooper()); // deliver callbacks in main thread
        }
    }

    private void handleFloorPlanChange(IAFloorPlan newFloorPlan) {
        Picasso.get()
                .load(newFloorPlan.getUrl())
                .into(mFloorPlanImage);
    }
}
