package in.succinct.plugins.ecommerce.extensions.inventory;

import com.venky.core.math.DoubleUtils;
import in.succinct.plugins.ecommerce.db.model.inventory.Inventory;
import com.venky.swf.db.extensions.BeforeModelSaveExtension;
import in.succinct.plugins.ecommerce.db.model.participation.MarketPlaceInventoryUpdateQueue;

public class BeforeSaveInventory extends BeforeModelSaveExtension<Inventory> {
	static {
		registerExtension(new BeforeSaveInventory());
	}
	@Override
	public void beforeSave(Inventory model) {
        model.setCompanyId(model.getFacility().getCompanyId());
		if (model.isInfinite() && model.getQuantity() > 0) {
			model.setQuantity(0.0);
		}
		if (!model.isInfinite() ){
			MarketPlaceInventoryUpdateQueue.push(model);
		}
	}

}
