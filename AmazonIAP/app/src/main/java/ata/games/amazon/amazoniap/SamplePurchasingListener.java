package ata.games.amazon.amazoniap;

import android.util.Log;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;

import java.util.Set;

public class SamplePurchasingListener implements PurchasingListener
{
    private static final String TAG = "SampleIAPEntitlements";

    private final IapManager iapManager;

    public SamplePurchasingListener(final IapManager iapManager)
    {
        this.iapManager = iapManager;
    }

    @Override
    public void onUserDataResponse(UserDataResponse userDataResponse)
    {
        Log.d(TAG, "onGetUserDataResponse: requestId (" + userDataResponse.getRequestId()
                + ") userIdRequestStatus: "
                + userDataResponse.getRequestStatus()
                + ")");
        final UserDataResponse.RequestStatus status = userDataResponse.getRequestStatus();

        switch(status)
        {
            case SUCCESSFUL:
                Log.d(TAG, "onUserDataResponse: get user id (" + userDataResponse.getUserData().getUserId()
                        + ", marketplace ("
                        + userDataResponse.getUserData().getMarketplace()
                        + ") ");
                //TODO here update all SKUs
                iapManager.setAmazonUserId(userDataResponse.getUserData().getUserId(), userDataResponse.getUserData().getMarketplace());

                break;
            case FAILED:
                break;
            case NOT_SUPPORTED:
                Log.d(TAG, "onUserDataResponse failed, status code is "+status);
                iapManager.setAmazonUserId(null,null);
                break;
        }
    }
    // First method to retrieve SKU purchases
    @Override
    public void onProductDataResponse(ProductDataResponse productDataResponse)
    {
        final ProductDataResponse.RequestStatus status = productDataResponse.getRequestStatus();
        Log.d(TAG, "onProductDataResponse: RequestStatus (" + status + ")");

        switch(status)
        {
            case SUCCESSFUL:
                Log.d(TAG, "onProductDataResponse: successful.  The item data map in this response includes the valid SKUs");
                final Set<String> unavailableSkus = productDataResponse.getUnavailableSkus();
                Log.d(TAG, "onProductDataResponse: " + unavailableSkus.size() + " unavailable skus");
                iapManager.enablePurchaseForSkus(productDataResponse.getProductData());
                iapManager.disablePurchaseForSkus(productDataResponse.getUnavailableSkus());
                iapManager.refreshLevel2Availability();//Here is when Update SKU
                break;
            case FAILED:
                break;
            case NOT_SUPPORTED:
                Log.d(TAG, "onProductDataResponse: failed, should retry request");
                break;
        }
    }

    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse)
    {
        Log.d(TAG, "onPurchaseUpdatesResponse: requestId (" + purchaseUpdatesResponse.getRequestId()
                + ") purchaseUpdatesResponseStatus ("
                + purchaseUpdatesResponse.getRequestStatus()
                + ") userId ("
                + purchaseUpdatesResponse.getUserData().getUserId()
                + ")");
        final PurchaseUpdatesResponse.RequestStatus status = purchaseUpdatesResponse.getRequestStatus();
        switch (status)
        {
            case SUCCESSFUL:

                iapManager.setAmazonUserId(purchaseUpdatesResponse.getUserData().getUserId(), purchaseUpdatesResponse.getUserData().getMarketplace());
                for (final Receipt receipt : purchaseUpdatesResponse.getReceipts())
                {
                    iapManager.handleReceipt(purchaseUpdatesResponse.getRequestId().toString(), receipt, purchaseUpdatesResponse.getUserData());
                }
                if (purchaseUpdatesResponse.hasMore())
                {
                    PurchasingService.getPurchaseUpdates(false);
                }
                iapManager.refreshLevel2Availability();

                break;
            case FAILED:
            case NOT_SUPPORTED:
                Log.d(TAG, "onProductDataResponse: failed, should retry request");
                iapManager.disableAllPurchases();
                break;
        }
    }

    // Notify Fulfillment Here
    @Override
    public void onPurchaseResponse(PurchaseResponse purchaseResponse)
    {
        final String requestId = purchaseResponse.getRequestId().toString();
        final String userId = purchaseResponse.getUserData().getUserId();
        final PurchaseResponse.RequestStatus status = purchaseResponse.getRequestStatus();
        Log.d(TAG,"PurchaseResponse");

        Log.d(TAG, "onPurchaseResponse: requestId (" + requestId
                + ") userId ("
                + userId
                + ") purchaseRequestStatus ("
                + status
                + ")");
        switch (status)
        {
            case SUCCESSFUL:
                final Receipt receipt = purchaseResponse.getReceipt();
                PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
                break;
            case ALREADY_PURCHASED:
                break;
            case INVALID_SKU:
                break;
            case FAILED:
                break;
            case NOT_SUPPORTED:
                break;
        }
    }
}
