package in.succinct.plugins.ecommerce.db.model.order;

import com.venky.cache.Cache;
import com.venky.core.date.DateUtils;
import com.venky.core.util.ObjectUtil;
import com.venky.extension.Registry;
import com.venky.swf.plugins.collab.db.model.user.User;
import com.venky.swf.plugins.collab.db.model.user.UserFacility;
import in.succinct.plugins.ecommerce.agents.order.tasks.OrderStatusMonitor;
import com.venky.cache.UnboundedCache;
import com.venky.core.util.Bucket;
import com.venky.swf.db.Database;
import com.venky.swf.db.JdbcTypeHelper.TypeConverter;
import com.venky.swf.db.table.ModelImpl;
import com.venky.swf.plugins.background.core.TaskManager;
import in.succinct.plugins.ecommerce.db.model.inventory.InventoryCalculator.ATP;
import in.succinct.plugins.ecommerce.db.model.participation.Facility;
import in.succinct.plugins.ecommerce.integration.fedex.RateWebServiceClient;
import org.apache.poi.ss.formula.functions.Rate;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderImpl  extends ModelImpl<Order>{
	public OrderImpl() {
		super();
	}

	public OrderImpl(Order proxy) {
		super(proxy);
	}
	
	private Map<String,OrderAttribute> map = null;

	String orderNumber = null;
	public String getOrderNumber(){
		if (orderNumber == null){
			orderNumber = getProxy().getId() == 0 ? "" : String.format("%019d", Long.valueOf(getProxy().getId()));
		}
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber){
		this.orderNumber = orderNumber;
	}


	public synchronized Map<String,OrderAttribute> getAttributeMap() {
		if (map == null) {
			map = new  UnboundedCache<String,OrderAttribute>() {

				private static final long serialVersionUID = 1L;

				@Override
				protected OrderAttribute getValue(String name) {
					OrderAttribute attr =  Database.getTable(OrderAttribute.class).newRecord();
					attr.setName(name);
					if(!getProxy().getRawRecord().isNewRecord()){
						attr.setOrderId(getProxy().getId());
					}
					return attr;
				}
			};
			getProxy().getAttributes().forEach(a->{
				map.put(a.getName(),a);
			});
		}
		return map;
	}
	public void saveAttributeMap(Map<String, OrderAttribute> map) { 
		map.keySet().stream().sorted().forEach(a->{
			OrderAttribute oa = map.get(a);
			oa.setOrderId(getProxy().getId());
			oa.save();
		});
	}
	public OrderAttribute getAttribute(String name) { 
		Map<String,OrderAttribute> map = getProxy().getAttributeMap();
		if (map.containsKey(name)) {
			return map.get(name);
		}
		return null;
	}

	public void backorder(){
		Order order = getProxy();
		order.getOrderLines().forEach(ol->{
			ol.backorder();
		});
	}
	public void acknowledge() { 
		Order order = getProxy();
		Registry.instance().callExtensions("order.before.acknowledge",order);

		TypeConverter<Long> iConv = order.getReflector().getJdbcTypeHelper().getTypeRef(Long.class).getTypeConverter();
		List<OrderLine> orderLines = order.getOrderLines();

		Map<Long, List<ATP>> skuATP = new Cache<Long, List<ATP>>() {
			@Override
			protected List<ATP> getValue(Long aLong) {
				return new ArrayList<>();
			}
		};

		Bucket acknowledgedLineCount = new Bucket();
		Bucket rejectedLineCount = new Bucket();

		orderLines.forEach(ol->{
			ol.acknowledge(skuATP,acknowledgedLineCount,rejectedLineCount,false);
		});

		TaskManager.instance().execute(new OrderStatusMonitor(order.getId()));

	}
    public void reject() {
	    cancel(OrderLine.CANCELLATION_REASON_OUT_OF_STOCK,OrderLine.CANCELLATION_INITIATOR_COMPANY);
    }
	public void cancel(String reason,String initiator){
        Order order = getProxy();
        order.getOrderLines().forEach(ol->{
            ol.cancel(reason,initiator);
        });
		TaskManager.instance().execute(new OrderStatusMonitor(order.getId()));

    }
	public void cancel(String reason) {
        cancel(reason,OrderLine.CANCELLATION_INITIATOR_USER);
	}

	public void pack() {
		Order order = getProxy();
		// Prevent Dead Lock
		List<OrderLine> toSort = order.getOrderLines();

		// Prevent Dead Lock
		toSort.sort((o1, o2) -> {
			long ret = o1.getSkuId() - o2.getSkuId();
			if (ret == 0L) {
				ret = o1.getShipFromId() - o2.getShipFromId();
			}
			return (int) (ret);
		});
		for (OrderLine ol : toSort) {
			ol.pack();
		}
		TaskManager.instance().execute(new OrderStatusMonitor(order.getId()));
	}
	public void ship() {
		Order order = getProxy(); 
		order.getOrderLines().stream().sorted(new Comparator<OrderLine>() {
			// Prevent Dead Lock
			@Override
			public int compare(OrderLine o1, OrderLine o2) {
				long ret = o1.getSkuId() - o2.getSkuId();
				if (ret == 0L){
					ret = o1.getShipFromId() - o2.getShipFromId();
				}
				return (int)(ret);
			}
		}).forEach(ol-> ol.ship());
		TaskManager.instance().execute(new OrderStatusMonitor(order.getId()));
	}
	public void deliver() {
		Order order = getProxy();
		order.getOrderLines().stream().sorted(new Comparator<OrderLine>() {
			// Prevent Dead Lock
			@Override
			public int compare(OrderLine o1, OrderLine o2) {
				long ret = o1.getSkuId() - o2.getSkuId();
				if (ret == 0L){
					ret = o1.getShipFromId() - o2.getShipFromId();
				}
				return (int)(ret);
			}
		}).forEach(ol-> ol.deliver());
		TaskManager.instance().execute(new OrderStatusMonitor(order.getId()));
	}

	public boolean isShort() {
		Bucket numShortLines = new Bucket();
		getProxy().getOrderLines().forEach(ol->{
			if (ol.isShortage() && ol.getToShipQuantity() > 0){
				numShortLines.increment();
			}
		});
		return numShortLines.intValue() > 0 ;
	}

	public Long getManifestId() {
		String value = getAttribute("manifest_id").getValue();
		long manifestId = getReflector().getJdbcTypeHelper().getTypeRef(Long.class).getTypeConverter().valueOf(value) ;
		if (manifestId != 0){
			return manifestId;
		}
		return null;
	}
	public void setManifestId(Long id) {
		//
	}

	public Date getExpectedDeliveryDate(){
		Order order =  getProxy();
		Timestamp shipByDate = order.getShipByDate();
		Date expectedDeliveryDate = null;
                Optional<OrderStatus> orderShippedAudit = order.getOrderStatuses().stream().filter(os->{
                    return ObjectUtil.equals(os.getFulfillmentStatus(),Order.FULFILLMENT_STATUS_SHIPPED);
                }).findFirst();

                long shipDate = -1;
                if (orderShippedAudit.isPresent()){
                    shipDate = orderShippedAudit.get().getStatusDate().getTime();
                }else {
        	    long today = DateUtils.getStartOfDay(System.currentTimeMillis());
                    shipDate = (shipByDate != null && shipByDate.getTime() >= today) ? shipByDate.getTime() : today ;
                }
		
		expectedDeliveryDate = new Date(shipDate + getTransitDays() * 24L * 60L * 60L * 1000L);

		return expectedDeliveryDate;
	}
	public int getTransitDays(){
		Order order =  getProxy();
		int transitDays = RateWebServiceClient.MAX_TRANSIT_DAYS;
		Optional<OrderAddress> optionalShipTo = order.getAddresses().stream().filter(a-> ObjectUtil.equals(a.getAddressType(),OrderAddress.ADDRESS_TYPE_SHIP_TO)).findFirst();
		if (optionalShipTo.isPresent()) {
			OrderAddress shipTo = optionalShipTo.get();
			List<OrderLine> lines = order.getOrderLines();
			if (!lines.isEmpty()){
				OrderLine line = lines.get(0);
				if (line.getShipFromId() != null){
					Facility facility = line.getShipFrom();
					if (facility != null){
						transitDays = new RateWebServiceClient<OrderAddress>(facility,shipTo).getTransitTime().getTransitDays();
					}
				}
			}
		}
		return transitDays + 2; //Buffer
	}
	public User getShipFromContact(){
		Order order = getProxy();
		User user = null;
		OrderLine sample = order.getOrderLines().get(0);
		if (sample.getShipFromId() != null){
			for (UserFacility fu : sample.getShipFrom().getFacilityUsers()){
				if (fu.getUser().isStaff()){
					user = fu.getUser();
					break;
				}
			}
		}
		return user;
	}

	public Long getPreferredCarrierId() {
		return preferredCarrierId;
	}

	public void setPreferredCarrierId(Long preferredCarrierId) {
		this.preferredCarrierId = preferredCarrierId;
		if (this.preferredCarrierId != null){
			getProxy().setPreferredCarrierName(getProxy().getPreferredCarrier().getName()); //This will make model marked dirty.
		}
	}

	Long preferredCarrierId ;
}
