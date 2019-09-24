package in.succinct.plugins.ecommerce.db.model.inventory;

import com.venky.cache.UnboundedCache;
import com.venky.core.util.Bucket;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.pm.DataSecurityFilter;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import in.succinct.plugins.ecommerce.db.model.demand.Demand;
import in.succinct.plugins.ecommerce.db.model.order.OrderLine;
import in.succinct.plugins.ecommerce.db.model.participation.Facility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InventoryCalculator {
	//OrderLine orderLine = null;
	Facility facility = null;
	Sku sku = null;
	public InventoryCalculator(OrderLine orderLine) {
		//this.orderLine = orderLine;
		this.facility = orderLine.getShipFrom();
		this.sku = orderLine.getSku();
	}
	public InventoryCalculator(Sku sku){
		this(sku,null);
	}
	public InventoryCalculator(Sku sku, Facility facility){
		this.sku = sku ;
		this.facility = facility;
	}

	public Double getTotalInventory() { 
		Bucket total = new Bucket();
		getInventory().forEach(atp->{
			total.increment(atp.getQuantity());
		});
		return total.doubleValue();
	}
	
	public static class ATP { 
		Inventory inventory;
		List<Demand> pendShip;
		public Inventory getInventory() {
			return inventory;
		}
        public double getQuantity() {
			Bucket total = new Bucket();
			Bucket pendShipQuantity = new Bucket(); 
			pendShip.forEach(demand->{
				pendShipQuantity.increment(demand.getQuantity().doubleValue());
			});
			total.increment(inventory.isInfinite() ? Double.POSITIVE_INFINITY : inventory.getQuantity() - pendShipQuantity.doubleValue());
			return Math.max(0, total.doubleValue());
		}
	}

	private List<Demand> getDemands(Set<Long> inventoryIds){
		ModelReflector<Demand> ref = ModelReflector.instance(Demand.class);

		Expression where = new  Expression(ref.getPool(), Conjunction.AND);
		where.add(new Expression(ref.getPool(),"INVENTORY_ID" , Operator.IN, inventoryIds.toArray()));
		List<Demand> demands = new Select().from(Demand.class).where(where).execute();
		return demands;
	}
	public List<ATP> getInventory(){
		List<ATP> inv = new ArrayList<>();

		ModelReflector<Inventory> ref = ModelReflector.instance(Inventory.class);
		Expression where = new  Expression(ref.getPool(), Conjunction.AND);
		if (facility != null){
			where.add(new Expression(ref.getPool(),"FACILITY_ID" , Operator.EQ, facility.getId()));
		}
		where.add(new Expression(ref.getPool(),"SKU_ID" , Operator.EQ, sku.getId()));

		List<Inventory> inventories = new Select().from(Inventory.class).where(where).execute();

		Set<Long> inventoryIds = DataSecurityFilter.getIds(inventories);
		List<Demand> demands = getDemands(inventoryIds);
		Map<Long,List<Demand>> map = new UnboundedCache<Long, List<Demand>>() {
			@Override
			protected List<Demand> getValue(Long inventoryId) {
				return new ArrayList<>();
			}
		};
		demands.forEach(demand -> map.get(demand.getInventoryId()).add(demand));

		inventories.forEach(inventory->{
			ATP atp = new ATP();
			atp.inventory = inventory;
			atp.pendShip = map.get(inventory.getId());
			inv.add(atp);
		});

		return inv;
	}
}
