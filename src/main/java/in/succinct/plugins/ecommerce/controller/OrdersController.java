package in.succinct.plugins.ecommerce.controller;

import com.venky.swf.controller.annotations.SingleRecordAction;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.User;
import com.venky.swf.integration.IntegrationAdaptor;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.background.core.Task;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.controller.ModelController;
import com.venky.swf.views.RedirectorView;
import com.venky.swf.views.View;
import in.succinct.plugins.ecommerce.agents.order.tasks.OrderStatusMonitor;
import in.succinct.plugins.ecommerce.agents.order.tasks.pack.PacklistPrintTask;
import in.succinct.plugins.ecommerce.db.model.order.Order;
import in.succinct.plugins.ecommerce.db.model.order.OrderAddress;
import in.succinct.plugins.ecommerce.db.model.order.OrderLine;
import in.succinct.plugins.ecommerce.db.model.order.OrderPrint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrdersController extends ModelController<Order> {

	public OrdersController(Path path) {
		super(path);
	}
	
	@SingleRecordAction(icon="glyphicon-thumbs-up",tooltip="Accept Order for Fulfillment")
	public View acknowledge(long orderId) {
		Order order = Database.getTable(Order.class).get(orderId);
		order.acknowledge();
        if (getIntegrationAdaptor() != null) {
            return show(order);
        }else {
            return back();
        }
	}
	@SingleRecordAction(icon="glyphicon-thumbs-down",tooltip="Back order")
	public View backorder(long  orderId){
		Order order = Database.getTable(Order.class).get(orderId);
		order.backorder();
		if (getIntegrationAdaptor() != null) {
			return show(order);
		}else {
			return back();
		}
	}

	@SingleRecordAction(icon="glyphicon-thumbs-down",tooltip="Reject order fulfillment")
	public View reject(long orderId) {
		Order order = Database.getTable(Order.class).get(orderId);
		order.reject();
        if (getIntegrationAdaptor() != null) {
            return show(order);
        }else {
            return back();
        }
	}
	@SingleRecordAction(icon="glyphicon-thumbs-down",tooltip="Cancel order fulfillment")
	public View cancel(long orderId) {
		Order order = Database.getTable(Order.class).get(orderId);
		order.cancel("No Longer Required",OrderLine.CANCELLATION_INITIATOR_USER);
		if (getIntegrationAdaptor() != null) {
			return show(order);
		}else {
			return back();
		}
	}


	@Override
    protected Set<Class<? extends Model>> getIgnoredParentModels() {
		Set<Class<? extends Model>> set = new HashSet<>();
		set.add(User.class);
		return set;
	}


	@Override
	protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
		Map<Class<? extends Model>,List<String>> map =  new HashMap<>();
		map.put(OrderLine.class, null);
		//map.put(OrderAttribute.class, null);
		//map.put(OrderLineAttribute.class, null);
		map.put(OrderAddress.class, null);

		return map;
	}
	
	@SingleRecordAction(icon="glyphicon-gift", tooltip="Pack")
	public View pack(long orderId) {
		Order order = Database.getTable(Order.class).get(orderId);
		order.pack();
        if (getIntegrationAdaptor() != null) {
            return show(order);
        }else {
            return back();
        }
	}


	@SingleRecordAction(icon="glyphicon-barcode", tooltip="Print Pack List")
	public View print(long orderId) {
		Order order = Database.getTable(Order.class).get(orderId);
		OrderPrint print = null;
		for (OrderPrint p : order.getOrderPrints()) {
			if (p.getDocumentType().equals(OrderPrint.DOCUMENT_TYPE_PACK_SLIP)) {
				print=p;
				break;
			}
		}
		if (print == null) {
			boolean orderPacked = true;
			for (OrderLine line : order.getOrderLines()){
				if (line.getToPackQuantity() > 0){
					orderPacked = false;
					break;
				}
			}
			if (!orderPacked){
				throw new RuntimeException("Order is not yet packed");
			}
			TaskManager.instance().executeAsync(getTasksToPrint(orderId),false);
			if (getReturnIntegrationAdaptor() != null){
				return IntegrationAdaptor.instance(OrderPrint.class,getReturnIntegrationAdaptor().getFormatClass()).createResponse(getPath(),
						new ArrayList<>());
			}else {
				getPath().addInfoMessage("Labels being generated.. check after some time.");
				return back();
			}
		}else {
			if (getReturnIntegrationAdaptor() != null){
				return IntegrationAdaptor.instance(OrderPrint.class,getReturnIntegrationAdaptor().getFormatClass()).createResponse(getPath(),
						Arrays.asList(print),Arrays.asList("ID"));
			}else {
				return new RedirectorView(getPath(), getPath().controllerPath() + "/show/" + orderId + "/order_prints", "view/" + print.getId());
			}
		}
	}

	@SingleRecordAction(icon="glyphicon-road", tooltip="Ship")
	public View ship(long orderId) {
		Order order = Database.getTable(Order.class).get(orderId);
		order.ship();
		if (getIntegrationAdaptor() != null) {
			return show(order);
		}else {
			return back();
		}
	}

	@SingleRecordAction(icon = "glyphicon-envelope",tooltip = "Deliver")
	public View deliver(long orderId){
		Order order = Database.getTable(Order.class).get(orderId);
		order.deliver();
		if (getIntegrationAdaptor() != null) {
			return show(order);
		}else {
			return back();
		}
	}

	protected List<Task> getTasksToPrint(long orderId){
		List<Task> printTasks = new ArrayList<>();
		printTasks.add(new PacklistPrintTask(orderId));
		return printTasks;
	}
	
	@SingleRecordAction(icon="glyphicon-refresh",tooltip="Refresh")
	public View refresh(long orderId) {

        List<Task> tasks = new ArrayList<>();
        tasks.add(new OrderStatusMonitor(orderId));

        TaskManager.instance().executeAsync(tasks,false);
        if (getIntegrationAdaptor() != null) {
			return show(orderId);
        }else {
            return back();
        }
    }

}
