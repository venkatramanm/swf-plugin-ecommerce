package in.succinct.plugins.ecommerce.db.model.order;

import com.venky.swf.db.model.Model;
import com.venky.swf.plugins.collab.db.model.config.City;

public interface FedexTransitTime extends Model {
    public Long getOriginCityId();
    public void setOriginCityId(Long id);
    public City getOriginCity();
    public String getOriginCityName();
    public void setOriginCityName(String name);

    public Long getDestinationCityId();
    public void setDestinationCityId(Long id);
    public City getDestinationCity();
    public String getDestinationCityName();
    public void setDestinationCityName(String name);

    public int getTransitDays();
    public void setTransitDays(int days);
}
