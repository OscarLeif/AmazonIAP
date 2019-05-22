package ata.games.amazon.amazoniap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.RequestId;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity
{
    private IapManager iapManager;
    private static final String TAG = "PluginIAPEntitlement";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupIAPOnCreate();
    }

    public void setupIAPOnCreate()
    {
        iapManager = new IapManager(this);
        final SamplePurchasingListener purchasingListener = new SamplePurchasingListener(iapManager);

        PurchasingService.registerListener(this.getApplicationContext(), purchasingListener);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //Initialize IAP
        //Populate the Entitlement SKU
        final Set<String> productSkus = new HashSet<String>();
        productSkus.add("FullGame");

        //So this triggers ?
        PurchasingService.getProductData(productSkus);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        iapManager.activate();
        Log.d(TAG, "onResume: call getUserData");
        PurchasingService.getUserData();

        Log.d(TAG, "onResume: getPurchaseUpdates");
        PurchasingService.getPurchaseUpdates(false);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        iapManager.deactivate();
    }

    public void PurchaseGame(View v)
    {
        //Fulfill after OK purchase
        MainActivity.this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(iapManager.IsSkuOwned("FullGame"))
                {
                    Toast.makeText(getApplicationContext(), "You already purchase the full game",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    final RequestId requestId = PurchasingService.purchase("FullGame");
                }
            }
        });
    }
}
