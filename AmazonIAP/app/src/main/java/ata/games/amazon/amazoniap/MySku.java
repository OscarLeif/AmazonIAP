package ata.games.amazon.amazoniap;

/**
 * MySku enum contains all In App Purchase products definition that the sample
 * app will use. The product definition includes two properties: "SKU" and
 * "Available Marketplace".
 */
public enum MySku
{
    // The only entitlement product used in this sample app
    LEVEL2_US("FullGame", "US"),
    // We have two one for the US market and other fot the JP market.
    LEVEL2_JP("com.amazon.sample.iap.entitlement.level2", "JP");

    private final String sku;
    private final String availableMarkpetplace;

    /**
     * Returns the MySku object from the specified Sku and marketplace value.
     *
     * @param sku
     * @param marketplace
     * @return
     */
    public static MySku fromSku(final String sku, final String marketplace)
    {
        if (LEVEL2_US.getSku().equals(sku) && (marketplace == null || LEVEL2_US.getAvailableMarketplace().equalsIgnoreCase(marketplace)))
        {
            return LEVEL2_US;
        }
        return null;
    }

    /**
     * Returns the Sku string of the MySku object
     *
     * @return
     */
    public String getSku()
    {
        return this.sku;
    }

    /**
     * Returns the Available Marketplace of the MySku object
     *
     * @return
     */
    public String getAvailableMarketplace()
    {
        return this.availableMarkpetplace;
    }

    private MySku(final String sku, final String availableMarkpetplace)
    {
        this.sku = sku;
        this.availableMarkpetplace = availableMarkpetplace;
    }
}