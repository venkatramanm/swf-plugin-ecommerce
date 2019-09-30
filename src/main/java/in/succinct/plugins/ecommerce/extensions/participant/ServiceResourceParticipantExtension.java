package in.succinct.plugins.ecommerce.extensions.participant;

import com.venky.core.collections.SequenceSet;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ParticipantExtension;
import com.venky.swf.db.model.User;
import com.venky.swf.pm.DataSecurityFilter;
import in.succinct.plugins.ecommerce.db.model.catalog.Service;
import in.succinct.plugins.ecommerce.db.model.service.ServiceResource;

import java.util.List;

public class ServiceResourceParticipantExtension extends ParticipantExtension<ServiceResource> {
    @Override
    protected List<Long> getAllowedFieldValues(User user, ServiceResource partiallyFilledModel, String fieldName) {
        if (ObjectUtil.equals(fieldName,"SERVICE_ID")){
            return DataSecurityFilter.getIds(DataSecurityFilter.getRecordsAccessible(Service.class,user));
        }else if (ObjectUtil.equals(fieldName,"USER_ID")){
            SequenceSet<Long> ret = new SequenceSet<>();
            com.venky.swf.plugins.collab.db.model.user.User u = user.getRawRecord().getAsProxy(com.venky.swf.plugins.collab.db.model.user.User.class);
            if (u.isStaff()){
                ret.addAll(u.getCompany().getStaffUserIds());
            }
            return ret;
        }
        return null;
    }
}