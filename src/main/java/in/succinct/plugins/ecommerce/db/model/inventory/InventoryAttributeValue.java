package in.succinct.plugins.ecommerce.db.model.inventory;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.model.Model;
import in.succinct.plugins.ecommerce.db.model.attributes.AttributeValue;

public interface InventoryAttributeValue extends Model {
    @UNIQUE_KEY
    public long getInventoryId();
    public void setInventoryId(long inventoryId);
    public Inventory getInventory();

    @UNIQUE_KEY
    public long getAttributeValueId();
    public void setAttributeValueId(long id);
    public AttributeValue getAttributeValue();

}
