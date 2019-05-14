package in.succinct.plugins.ecommerce.db.model.inventory;

import com.venky.swf.db.model.Model;

public interface InventoryAudit extends Model {
    public long getInventoryId();
    public void setInventoryId(long id);
    public Inventory getInventory();

    public double getAuditQuantity();
    public void setAuditQuantity(double auditQuantity);

    public String getComment();
    public void setComment (String comments);
}