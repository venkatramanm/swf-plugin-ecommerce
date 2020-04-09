package in.succinct.plugins.ecommerce.extensions.participant;

import com.venky.core.collections.SequenceSet;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ParticipantExtension;
import com.venky.swf.db.model.User;
import com.venky.swf.pm.DataSecurityFilter;
import in.succinct.plugins.ecommerce.db.model.catalog.Service;
import in.succinct.plugins.ecommerce.db.model.service.ServiceResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceResourceParticipantExtension extends ParticipantExtension<ServiceResource> {
    static {
        registerExtension(new ServiceResourceParticipantExtension());
    }
    @Override
    protected List<Long> getAllowedFieldValues(User user, ServiceResource partiallyFilledModel, String fieldName) {
        com.venky.swf.plugins.collab.db.model.user.User u = user.getRawRecord().getAsProxy(com.venky.swf.plugins.collab.db.model.user.User.class);

        if (ObjectUtil.equals(fieldName,"SERVICE_ID")){
            if (!partiallyFilledModel.getReflector().isVoid(partiallyFilledModel.getServiceId())){
                if (partiallyFilledModel.getService().isAccessibleBy(user)){
                    return Arrays.asList(partiallyFilledModel.getServiceId());
                }else {
                    return new ArrayList<>();
                }
            }
        }
        return null;
    }
}
