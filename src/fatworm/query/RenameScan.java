package fatworm.query;

import java.util.Collection;
import java.util.LinkedList;

import fatworm.dataentity.DataEntity;
import fatworm.record.RecordFile;
import fatworm.util.Util;

public class RenameScan implements Scan{

    String alias;
    Scan scan;
    public RenameScan(Scan scan, String name){
        this.alias = name;
        this.scan = scan;
    }
	@Override
	public void beforeFirst() {
		scan.beforeFirst();
	}
	@Override
	public boolean next() {
		return scan.next();
	}
	@Override
	public DataEntity getField(String fldname) {
		return scan.getField(fldname);
	}
	@Override
	public boolean hasField(String fldname) {
		return scan.hasField(fldname);
	}
	@Override
	public DataEntity getColumn(String colname) {
        if (Util.isFieldSuffix(colname)) {
            if ( alias.equals(Util.getColumnTableName(colname)))
                return getField(Util.getColumnFieldName(colname));
        }
        if (Util.isSimpleColumn(colname)) {
            return getField(colname);
        }
        return null;
	}
	
	@Override
	public boolean hasColumn(String colname) {
        if (Util.isFieldSuffix(colname)) {
            if ( alias.equals(Util.getColumnTableName(colname)))
                return hasField(Util.getColumnFieldName(colname));
        }
        if (Util.isSimpleColumn(colname)) {
            return hasField(colname);
        }
        return false;
	}
	@Override
	public Collection<String> fields() {
		return scan.fields();
	}
	@Override
	public Collection<String> columns() {
        LinkedList<String> result = new LinkedList<String>();
        for(String f:scan.fields()) {
            result.add(Util.makeColumnName(alias,f));
        }
        return result;
	}
	@Override
	public DataEntity getColumnByIndex(int index) {
		return scan.getColumnByIndex(index);
	}
	@Override
	public int getNumberOfColumns() {
		return scan.getNumberOfColumns();
	}
	
	@Override
	public int indexOf(String colname) {
		return scan.indexOf(colname);
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
		return fieldName(index);
	}
	@Override
	public String columnName(int index) {
		String field = fieldName(index);
		if (Util.isSimpleColumn(field))
			return alias + "." + field;
		else 
			return null;
	}
	@Override
	public RecordFile getRecordFile() {
		// TODO Auto-generated method stub
		return null;
	}
	
}