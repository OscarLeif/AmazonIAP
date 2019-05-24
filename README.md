# AmazonIAP
Basic Amazon IAP Example here they're working

Most of the Documentation can be found on the Amazon website, but here I leave some tips that I couldn't found.

You can Test the App using
 
* Sandbox mode
* Live App test mode.

Some methods will not work as you think

For example

> PurchasingService.getPurchaseUpdates(false);

Is designed to refresh all IAP from the Server, the purpose of this method is to know what IAP the user already have, but this method will not always work when you called it.

I mean this works in these scenarios.
First you install the app, resume() call the method, it will trigger Amazon service to get the data.
But if you call this method again it will not work.
The only way to have the method working again is making a purchase, but the user should accept the purchase this could be a problem when you have Consumable items.

This means the only way to get the data is like this. In the first start after install an app you should get the data there, the only way to do this without uninstall and install is by deleting the app data.


Man I should update this again.

Warning Live test mode

Basically this is like using a real app mode.
You should have all the IAP data complete, images price, description if something is missing Fire OS will have problems.

But Live app test mode works really like a real app, if you purchase an Entitlement you cannot purchase again. 
So the only way to make that purchase again is to create another Amazon account that really is a lot of time 
  

I will update that later I'm just fixing libraries issues bad code. Graddle stuff.


* SampleIAPEntitlementsApp [Amazon Original example working on Android Studio]
* AmazonIAP [Custom example working ]
