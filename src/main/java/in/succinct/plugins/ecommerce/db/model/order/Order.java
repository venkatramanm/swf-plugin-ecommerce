package in.succinct.plugins.ecommerce.db.model.order;

import com.venky.swf.db.annotations.column.COLUMN_DEF;
import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.defaulting.StandardDefault;
import com.venky.swf.db.annotations.column.indexing.Index;
import com.venky.swf.db.annotations.column.ui.PROTECTION;
import com.venky.swf.db.annotations.column.ui.PROTECTION.Kind;
import com.venky.swf.db.annotations.column.validations.Enumeration;
import com.venky.swf.db.annotations.model.EXPORTABLE;
import com.venky.swf.db.annotations.model.MENU;
import com.venky.swf.db.model.Model;
import com.venky.swf.plugins.collab.db.model.CompanySpecific;
import com.venky.swf.plugins.collab.db.model.user.User;
import in.succinct.plugins.ecommerce.db.model.participation.MarketPlaceIntegration;
import in.succinct.plugins.ecommerce.db.model.participation.PreferredCarrier;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


@MENU("Fulfillment")
@EXPORTABLE(false)

public interface Order extends Model, CompanySpecific {
    @Index
    public String getReference();
    public void setReference(String reference);

    @IS_VIRTUAL
    public String getOrderNumber();
    public void setOrderNumber(String orderNumber);

    @IS_VIRTUAL
    public Long getPreferredCarrierId();
    public void setPreferredCarrierId(Long id);
    public PreferredCarrier getPreferredCarrier();

    @IS_NULLABLE
    @COLUMN_DEF(value = StandardDefault.SOME_VALUE, args = PreferredCarrier.FEDEX)
    @PROTECTION(Kind.NON_EDITABLE)
    public String getPreferredCarrierName();
    public void setPreferredCarrierName(String name);

    @COLUMN_DEF(StandardDefault.BOOLEAN_TRUE)
    public boolean isMrpInclusiveOfTaxes();
    public void setMrpInclusiveOfTaxes(boolean mrpInclusiveOfTaxes);


    @UNIQUE_KEY
    public long getId();

    public static final String FULFILLMENT_STATUS_DOWNLOADED = "DOWNLOADED";
    public static final String FULFILLMENT_STATUS_ACKNOWLEDGED = "ACKNOWLEDGED";
    public static final String FULFILLMENT_STATUS_PACKED = "PACKED";
    public static final String FULFILLMENT_STATUS_MANIFESTED = "MANIFESTED";
    public static final String FULFILLMENT_STATUS_SHIPPED = "SHIPPED";
    public static final String FULFILLMENT_STATUS_DELIVERED = "DELIVERED";
    public static final String FULFILLMENT_STATUS_CANCELLED = "CANCELLED";
    public static final String FULFILLMENT_STATUS_RETURNED = "RETURNED";

    public List<OrderStatus> getOrderStatuses();

    @Enumeration( FULFILLMENT_STATUS_DOWNLOADED + "," + FULFILLMENT_STATUS_ACKNOWLEDGED + ","  +
            FULFILLMENT_STATUS_PACKED + "," + FULFILLMENT_STATUS_MANIFESTED + "," + FULFILLMENT_STATUS_SHIPPED + "," + FULFILLMENT_STATUS_DELIVERED + "," +
            FULFILLMENT_STATUS_CANCELLED +"," + FULFILLMENT_STATUS_RETURNED)
    @COLUMN_DEF(value=StandardDefault.SOME_VALUE,args=FULFILLMENT_STATUS_DOWNLOADED)
    @Index
    public String getFulfillmentStatus();
    public void setFulfillmentStatus(String status);

    public static final String RETURN_STATUS_REQUESTED = "REQUESTED" ;
    public static final String RETURN_STATUS_REJECTED = "REJECTED" ;
    public static final String RETURN_STATUS_APPROVED = "APPROVED" ;
    public static final String RETURN_STATUS_RETURNED = "RETURNED" ;
    public static final String RETURN_STATUS_REFUNDED = "REFUNDED" ;
    public static final String RETURN_STATUS_REPLACED = "REPLACED" ;

    @Enumeration(" ," +RETURN_STATUS_REQUESTED +"," + RETURN_STATUS_REJECTED + "," + RETURN_STATUS_APPROVED + "," + RETURN_STATUS_RETURNED +"," + RETURN_STATUS_REFUNDED + "," + RETURN_STATUS_REPLACED)
    public String getReturnStatus();
    public void setReturnStatus(String status);

    public String getReturnReason();
    public void setReturnReason(String reason);

    @IS_NULLABLE
    public Double getSellingPrice();
    public void setSellingPrice(Double price);

    @Index
    public Timestamp getShipByDate();
    public void setShipByDate(Timestamp ts);

    @IS_NULLABLE
    @Index
    public Timestamp getShipAfterDate();
    public void setShipAfterDate(Timestamp ts);


    public List<OrderAddress> getAddresses();

    public List<OrderAttribute> getAttributes();
    public <T extends OrderLine> List<T> getOrderLines();

    public Map<String,OrderAttribute> getAttributeMap();
    public void saveAttributeMap(Map<String,OrderAttribute> map);
    public OrderAttribute getAttribute(String name);

    public void acknowledge();

    public void backorder();

    public void reject();
    public void cancel(String reason);
    public void cancel(String reason,String initiator);

    public void ship();

    public List<OrderPrint> getOrderPrints();
    public void pack();
    public void deliver();

    @COLUMN_DEF(StandardDefault.BOOLEAN_FALSE)
    public boolean isCOD();
    public void setCOD(Boolean valueOf);

    @IS_VIRTUAL
    public boolean isShort();

    public Double getIGst();
    public void setIGst(Double gst);

    public Double getCGst();
    public void setCGst(Double gst);

    public Double getSGst();
    public void setSGst(Double gst);

    public Double getPrice();
    public void setPrice(Double price);


    public Double getProductPrice();
    public void setProductPrice(Double productPrice);

    public Double getProductSellingPrice();
    public void setProductSellingPrice(Double productSellingPrice);

    @COLUMN_DEF(StandardDefault.ZERO)
    public double getShippingPrice();
    public void setShippingPrice(double shippingCharges);

    @COLUMN_DEF(StandardDefault.ZERO)
    public double getShippingSellingPrice();
    public void setShippingSellingPrice(double shippingCharges);


    @IS_VIRTUAL
    public Long getManifestId();
    public void setManifestId(Long id);
    public Manifest getManifest();

    @IS_NULLABLE
    public String getHoldReason();
    public void setHoldReason(String reason);
    public static final String HOLD_REASON_CATALOG_INCOMPLETE = "New item!";

    @IS_VIRTUAL
    public Date getExpectedDeliveryDate();


    public List<OrderIntransitEvent> getIntransitUpdates();

    @IS_VIRTUAL
    public User getShipFromContact();

    @IS_NULLABLE
    public Long getMarketPlaceIntegrationId();
    public void setMarketPlaceIntegrationId(Long id);
    public MarketPlaceIntegration getMarketPlaceIntegration();
}
