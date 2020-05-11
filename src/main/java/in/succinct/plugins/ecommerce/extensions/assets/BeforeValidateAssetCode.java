package in.succinct.mandi.extensions;

import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.BeforeModelValidateExtension;
import in.succinct.plugins.ecommerce.db.model.attributes.AssetCode;

public class BeforeValidateAssetCode extends BeforeModelValidateExtension<AssetCode> {
    static {
        registerExtension(new BeforeValidateAssetCode());
    }
    @Override
    public void beforeValidate(AssetCode model) {
        if (ObjectUtil.isVoid(model.getCode())){
            throw new RuntimeException("Hsn/Sac Code cannot be blanks");
        }
        if (StringUtil.valueOf(model.getCode()).length() != 6){
            model.setCode(String.format("%08d",Long.valueOf(model.getCode())));
        }
    }
}
