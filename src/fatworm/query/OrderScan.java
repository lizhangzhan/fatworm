package fatworm.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import fatworm.absyn.OrderByColumn;
import fatworm.dataentity.DataEntity;
import fatworm.record.RecordIterator;
import fatworm.util.Util;

public class OrderScan implements Scan {

	Scan scan;
	List<OrderByColumn> order;
	OrderContainer container;
	boolean doneorder;
	public OrderScan(Scan scan, List<OrderByColumn> order) {
		this.scan = scan;
		this.order = order;
		
	}
	@Override
	public void beforeFirst() {
	    if (!doneorder) {
    		scan.beforeFirst();				
    		container = Util.getOrderContainer(scan, order);
    		container.sort();
    		doneorder = true;
	    }
		container.beforeFirst();
	}
	
	@Override
	public boolean next() {
		return container.next();
	}

	@Override
	public DataEntity getField(String fldname) {
		return container.getColumnByIndex(indexOfField(fldname));
	}

	@Override
	public boolean hasField(String fldname) {
		return scan.hasField(fldname);
	}

	@Override
	public DataEntity getColumn(String colname) {
		return container.getColumnByIndex(indexOfColumn(colname));
	}

	@Override
	public boolean hasColumn(String colname) {
		return scan.hasColumn(colname);
	}

	@Override
	public Collection<String> fields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> columns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataEntity getColumnByIndex(int index) {
		return container.getColumnByIndex(index);
	}

	@Override
	public int getNumberOfColumns() {
		return scan.getNumberOfColumns();
	}

	@Override
	public int indexOfField(String field) {
		return scan.indexOfColumn(field);
	}

	@Override
	public int type(String colname) {
		return scan.type(colname);
	}

	@Override
	public int type(int index) {
		return scan.type(index);
	}

	@Override
	public String fieldName(int index) {
		return scan.fieldName(index);
	}

	@Override
	public String columnName(int index) {
		return scan.columnName(index);
	}

	@Override
	public RecordIterator getRecordFile() {
		return null;
	}

	@Override
	public DataEntity getFunctionValue(String func) {
		return scan.getFunctionValue(func);
	}

	@Override
	public boolean hasFunctionValue(String func) {
		return scan.hasFunctionValue(func);
	}
	@Override
	public int indexOfColumn(String column) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public DataEntity getOrderKey(String key) {
		return scan.getOrderKey(key);
	}
    @Override
    public boolean hasIndex(String colname) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public RecordIterator getIndex(String colname, DataEntity right, String cop) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public String getRealName(String alias) {
        return scan.getRealName(alias);
    }
    @Override
    public void setRealName(Map<String, String> map) {
        scan.setRealName(map);
    }

}
