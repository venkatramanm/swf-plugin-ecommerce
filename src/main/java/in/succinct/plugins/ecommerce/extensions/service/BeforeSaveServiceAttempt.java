package in.succinct.plugins.ecommerce.extensions.service;

import com.venky.swf.db.extensions.BeforeModelSaveExtension;
import in.succinct.plugins.ecommerce.db.model.service.ServiceAttempt;
import in.succinct.plugins.ecommerce.db.model.service.ServiceAttempt.AttemptStatus;
import in.succinct.plugins.ecommerce.db.model.service.ServiceOrder;

public class BeforeSaveServiceAttempt extends BeforeModelSaveExtension<ServiceAttempt> {
    static {
        registerExtension(new BeforeSaveServiceAttempt());
    }
    @Override
    public void beforeSave(ServiceAttempt model) {
        ServiceOrder order = model.getServiceOrder();
        if (AttemptStatus.valueOf(model.getStatus()) == AttemptStatus.SUCCESS && model.getRawRecord().isFieldDirty("STATUS")){
            order.setFulfillmentStatus(ServiceOrder.FULFILLMENT_STATUS_COMPLETE);
        }
        if (AttemptStatus.valueOf(model.getStatus()) == AttemptStatus.FAIL && model.getRawRecord().isFieldDirty("STATUS")){
            order.setFulfillmentStatus(ServiceOrder.FULFILLMENT_STATUS_SERVICE_ATTEMPTED);
        }
        order.save();
    }
}
