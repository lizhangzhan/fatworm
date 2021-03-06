package fatworm.dataentity;

import fatworm.util.ByteBuffer;

import static java.sql.Types.*;

public class NullDataEntity extends DataEntity
{
    public boolean isNull(){
        return true;
    }

    public void getBytes(ByteBuffer buffer) {
    }

    public int type() {
        return NULL;
    }

    public int estimatedSize() {
        return 1;
    }

	@Override
	public int compareTo(DataEntity o) {
		return 0;
	}
	@Override
	public DataEntity opWith(DataEntity o, String op) {
		return new NullDataEntity();
	}
	
	public String toString() {
		return "null";
	}
	
    public Object toJavaType() {
    	return null;
    }	
	
}
