package ata.games.amazon.iap;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.RequestId;
import com.unity3d.player.UnityPlayer;

import java.util.HashSet;
import java.util.Set;

public class AmazonPurchaser extends Fragment
{
    private static AmazonPurchaser instance;

    private SampleIapManager iapManager;

    private AmazonIapCallback unityCallbackReference;

    public static final String TAG = "Ata-AmazonIAP";

    public static void init(AmazonIapCallback callback)
    {
        if (instance == null)
        {
            instance = new AmazonPurchaser();
            instance.unityCallbackReference = callback;
            Log.d(TAG, "Singleton Amazon Purchase initialize");
            UnityPlayer.currentActivity.getFragmentManager().beginTransaction().add(instance, AmazonPurchaser.TAG).commit();
        } else
        {
            Toast.makeText(UnityPlayer.currentActivity, "Already Initialize just call once", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupIAPOnCreate();
    }

    private void setupIAPOnCreate()
    {
        iapManager = new SampleIapManager(this);
        final SamplePurchasingListener purchasingListener = new SamplePurchasingListener(iapManager);

        PurchasingService.registerListener(UnityPlayer.currentActivity, purchasingListener);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //Initialize IAP
        //Populate the Entitlement SKU
        final Set<String> productSkus = new HashSet<String>();
        productSkus.add("FullGame");
        PurchasingService.getProductData(productSkus);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        iapManager.activate();
        Log.d(TAG, "onResume: call getUserData");
        PurchasingService.getUserData();

        Log.d(TAG, "onResume: getPurchaseUpdates");
        PurchasingService.getPurchaseUpdates(false);
        unityCallbackReference.onInitialize(true);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        iapManager.deactivate();
    }

    public void Purchase(String sku)
    {
        //String userMarketPlace = iapManager.getUserMarketPlace();

        if(iapManager.IsSkuOwned(sku))
        {
            Toast.makeText(UnityPlayer.currentActivity, "You already purchase the full game",Toast.LENGTH_SHORT).show();
        }
        else
        {
            final RequestId requestId = PurchasingService.purchase(sku);
        }
    }

    public boolean CheckSkuOwned(String sku)
    {
        if (iapManager.IsSkuOwned(sku))
        {
            return true;
        } else
        {
            return false;
        }
    }
}

interface AmazonIapCallback
{
    void onInitialize(boolean isInit);
}
