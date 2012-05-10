package fatworm.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fatworm.absyn.ProjectionAllColumnValue;
import fatworm.absyn.ProjectionRenameValue;
import fatworm.absyn.ProjectionSimpleValue;
import fatworm.absyn.ProjectionValue;
import fatworm.dataentity.DataEntity;
import fatworm.dataentity.Int;
import fatworm.dataentity.Float;
import fatworm.dataentity.NullDataEntity;
import fatworm.record.RecordFile;
import fatworm.util.Util;

public class ProjectionScan implements Scan {
    Scan scan;
    List<ProjectionValue> projections;
    List<Set<String>> usefulColumnList;
    Map<String, DataEntity> oneGroupFunctionValue;
    Map<String, Integer> oneGroupFunctionType;
    int[] typeArray;
    boolean iterTable = false;
    boolean startOne = true;
    int width;
    Env env;
    public ProjectionScan(Scan scan, List<ProjectionValue> projections, Env env) {
        this.scan = scan;
        this.projections = projections;
        this.env = env;
        typeArray = new int[projections.size()];
        usefulColumnList = new ArrayList<Set<String>>();
        for (ProjectionValue projection: projections) {
        	usefulColumnList.add(projection.dumpUsefulColumns());
        	if (projection instanceof ProjectionAllColumnValue) {
        		iterTable = true;
        		width += scan.getNumberOfColumns();
        	}
        	else 
        		width++;
        }
        
        
        Set<String> oneGroupFunction = new HashSet<String>();
        for (Set<String> usefulColumn: usefulColumnList) {
	        Iterator<String> iter = usefulColumn.iterator();
	        while (iter.hasNext()) {
	            String column = iter.next();
	        	if (!scan.hasColumn(column))
	                oneGroupFunction.add(column);
	        	else {
	        		iterTable = true;
	        	}
	        }
        }
        oneGroupFunctionType = new HashMap<String, Integer>();
        oneGroupFunctionValue = new HashMap<String, DataEntity>();
        for (String s: oneGroupFunction) {
            oneGroupFunctionValue.put(s, calcFunction(s,scan));
        }
        calcType();
    }
    
    DataEntity calcFunction(String s, Scan scan) {
    	String column = Util.getFuncVariable(s);
    	String func = Util.getFuncName(s);
    	scan.beforeFirst();
    	if (func.compareToIgnoreCase("COUNT") == 0) {
    		oneGroupFunctionType.put(s, new Integer(java.sql.Types.INTEGER));
    		int count = 0;
    		while(scan.next()) {
    			if (!scan.getColumn(column).isNull())
    				count++;
    		}
    		return new Int(count);
    	}
    	else if (func.compareToIgnoreCase("AVG") == 0) { 
    		oneGroupFunctionType.put(s, new Integer(java.sql.Types.FLOAT));
    		int count = 0;
    		Float sum = new Float(0);    		    		
    		while(scan.next()) {
    			DataEntity entry = scan.getColumn(column); 
    			if (!entry.isNull()) {
    				count++;
    				sum.opWith(entry, "+");
    			}
    		}
    		if (count > 0)
    			return sum.opWith(new Int(count), "/");
    	}
    	else {
    		oneGroupFunctionType.put(s, new Integer(scan.type(column)));
    		DataEntity result = null;
    		if (func.compareToIgnoreCase("SUM") == 0) {
	    		while(scan.next()) {
	    			DataEntity entry = scan.getColumn(column); 
	    			if (!entry.isNull()) {
	    				if (result == null)
	    					result = entry;
	    				else 
	    					result.opWith(entry, "+");
	    			}
	    		}
	    	}
    		else if (func.compareToIgnoreCase("MAX") == 0) {
	    		while(scan.next()) {
	    			DataEntity entry = scan.getColumn(column); 
	    			if (!entry.isNull()) {
	    				if (result == null)
	    					result = entry;
	    				else if (result.compareTo(entry) < 0)
	    					result = entry;
	    			}
	    		}
	    	}
    		else if (func.compareToIgnoreCase("MIN") == 0) {
	    		while(scan.next()) {
	    			DataEntity entry = scan.getColumn(column); 
	    			if (!entry.isNull()) {
	    				if (result == null)
	    					result = entry;
	    				else if (result.compareTo(entry) > 0)
	    					result = entry;
	    			}
	    		}
	    	}
    		if (result == null)
    			return new NullDataEntity();
    		else 
    			return result;
    	}
    	return null;
    }
	@Override
	public void beforeFirst() {
		if (iterTable)
			scan.beforeFirst();
		else 
			startOne = true;
	}
	@Override
	public boolean next() {
		if (iterTable)
			return scan.next();
		else { 
			if (startOne) {
				startOne = false;
				return true;
			}
			else 
				return false;
		}
	}
	@Override
	public DataEntity getField(String fldname) {
		int i = 0;
		DataEntity result = null;
		for (ProjectionValue proj: projections) {
			if (proj instanceof ProjectionSimpleValue) {
				if (Util.hasField(proj.toString(), fldname)) {
					env.beginScope();
					for (String colname: usefulColumnList.get(i)) {
						if (oneGroupFunctionValue.get(colname) != null)
							env.putValue(colname, oneGroupFunctionValue.get(colname));
						else 
							env.putValue(colname, scan.getColumn(colname));
					}
					result = proj.getValue(env);
					env.endScope();
				}
			}
			if (proj instanceof ProjectionRenameValue) {
				if (((ProjectionRenameValue)proj).getAlias().equals(fldname)) {
					env.beginScope();
					for (String colname: usefulColumnList.get(i)) {
						if (oneGroupFunctionValue.get(colname) != null)
							env.putValue(colname, oneGroupFunctionValue.get(colname));
						else 
							env.putValue(colname, scan.getColumn(colname));
					}
					result = proj.getValue(env);
					env.endScope();
				}
			}
			if (proj instanceof ProjectionAllColumnValue) {
				if (scan.hasField(fldname))
					result = scan.getField(fldname);
			}
			i++;
		}
		return result;
	}
	@Override
	public boolean hasField(String fldname) {
		return getField(fldname) != null;
	}
	
	@Override
	public DataEntity getColumn(String colname) {
		if (Util.isFieldSuffix(colname)) // it may not be reached, just for care.
			return getField(Util.getColumnFieldName(colname));
		else if (Util.isSimpleColumn(colname)) {
			return getField(colname);
		}
		return null;
	}
	
	@Override
	public boolean hasColumn(String colname) {
		return getColumn(colname) != null;
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
		int i = 0;
		for (ProjectionValue proj: projections) {
			if (proj instanceof ProjectionAllColumnValue) {
				if (scan.getNumberOfColumns() <=index )
					index -= scan.getNumberOfColumns();
				else 
					scan.getColumnByIndex(index);
			}
			else {
				if(index == 0) {
					env.beginScope();
					for (String colname: usefulColumnList.get(i)) {
						if (oneGroupFunctionValue.get(colname) != null)
							env.putValue(colname, oneGroupFunctionValue.get(colname));
						else 
							env.putValue(colname, scan.getColumn(colname));
					}
					DataEntity result = proj.getValue(env);
					env.endScope();
					return result;
				}
				else 
					index--;
			}
			i++;
		}
		return null;
	}
	@Override
	public int getNumberOfColumns() {
		return width;
	}

	@Override
	public int indexOf(String column) {
		String fldname = column;
		if (Util.isFieldSuffix(column))
			fldname = Util.getColumnFieldName(column);
		
		int count = 0;
		int result = -1;
		for (ProjectionValue proj: projections) {
			if (proj instanceof ProjectionSimpleValue) {
				if (Util.hasField(proj.toString(), fldname)) {
					result = count;
				}
				else count++;
			}
			if (proj instanceof ProjectionRenameValue) {
				if (((ProjectionRenameValue)proj).getAlias().equals(fldname)) {
					result = count;
				}
				else count++;
			}
			if (proj instanceof ProjectionAllColumnValue) {
				if (scan.hasField(fldname))
					result = count + scan.indexOf(fldname);
				else 
					count += scan.getNumberOfColumns();
			}
		}
		return result;
	}

	@Override
	public int type(String colname) {
		return type(indexOf(colname));
	}

	@Override
	public int type(int index) {
		int i = 0;
		for (ProjectionValue proj: projections) {
			if (proj instanceof ProjectionAllColumnValue) {
				if (scan.getNumberOfColumns() <=index )
					index -= scan.getNumberOfColumns();
				else 
					return scan.type(index);
			}
			else {
				if(index == 0) {
					return typeArray[i];
				}
				else 
					index--;
			}
			i++;
		}
		return -1;
	}
	
	void calcType() {
		int i = 0;
		for (ProjectionValue proj: projections) {
			if (proj instanceof ProjectionAllColumnValue)
				continue;
			Map<String, Integer> typemap = new HashMap<String, Integer>();
			for (String column: usefulColumnList.get(i)) {
				if (oneGroupFunctionType.get(column) != null)
					typemap.put(column, oneGroupFunctionType.get(column));
				else 
					typemap.put(column, new Integer(scan.type(column)));
			}
			typeArray[i] = proj.getType(typemap);
			i++;
		}
	}

	@Override
	public String fieldName(int index) {
		for (ProjectionValue proj: projections) {
			if (proj instanceof ProjectionAllColumnValue) {
				if (scan.getNumberOfColumns() <=index)
					index -= scan.getNumberOfColumns();
				else 
					return scan.fieldName(index);
			}
			else {
				if(index == 0) {
					if (proj instanceof ProjectionRenameValue)
						return ((ProjectionRenameValue) proj).alias; 
					else  {
						String name = proj.toString();
						if(Util.isFunction(name) || Util.isSimpleColumn(name))
							return name;
						if(Util.isFieldSuffix(name))
							return Util.getColumnFieldName(name);
					}
				}
				else 
					index--;
			}
		}
		return null;
	}

	@Override
	public String columnName(int index) {
		for (ProjectionValue proj: projections) {
			if (proj instanceof ProjectionAllColumnValue) {
				if (scan.getNumberOfColumns() <=index)
					index -= scan.getNumberOfColumns();
				else 
					return scan.columnName(index);
			}
			else {
				if(index == 0) {
					if (proj instanceof ProjectionRenameValue)
						return ((ProjectionRenameValue) proj).alias; 
					else  {
						String name = proj.toString();
						if(Util.isFunction(name) || Util.isSimpleColumn(name) || Util.isFieldSuffix(name))
							return name;
					}
				}
				else 
					index--;
			}
		}
		return null;
	}

	@Override
	public RecordFile getRecordFile() {
		// TODO Auto-generated method stub
		return null;
	}
}