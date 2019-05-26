package in.succinct.plugins.ecommerce.db.model.service;

import com.venky.swf.db.annotations.column.pm.PARTICIPANT;
import com.venky.swf.db.annotations.model.EXPORTABLE;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.User;
import in.succinct.plugins.ecommerce.db.model.catalog.Service;
@EXPORTABLE(false)

public interface ServiceResource extends Model {
    @PARTICIPANT
    public Long getServiceId();
    public void setServiceId(Long id);
    public Service getService();


    public Long getUserId();
    public void setUserId(Long userId);
    public User getUser();
}
