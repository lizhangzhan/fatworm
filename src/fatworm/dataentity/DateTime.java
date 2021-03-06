package fatworm.dataentity;

import fatworm.util.ByteBuffer;

import static java.sql.Types.*;

public class DateTime extends DataEntity
{
    java.sql.Timestamp value;
    public DateTime(java.sql.Timestamp v) {
        value = v;
    }

    public DateTime(ByteBuffer buffer) {
        value = new java.sql.Timestamp(buffer.getLong());
    }

    public void getBytes(ByteBuffer buffer) {
        buffer.putLong(value.getTime());
    }

    public int type() {
        return DATE;
    }

    public int estimatedSize() {
        return 8;
    }

    public int compareTo(DataEntity t) {
        if(t instanceof DateTime) {
            return value.compareTo(((DateTime)t).value);
        }
        if(t instanceof TimeStamp) {
            return value.compareTo(((TimeStamp)t).value);
        }
        if(t instanceof FixChar) {
            return value.toString().compareTo(((FixChar)t).value);
        }
        if(t instanceof VarChar) {
            return value.toString().compareTo(((VarChar)t).value);
        }
        return -1;
    }
	@Override
	public DataEntity opWith(DataEntity o, String op) {
		// TODO Auto-generated method stub
		return new NullDataEntity();
	}

    public String toString() {
        return value.toString();
    }
	public DataEntity toType(int type) {
		if (type == java.sql.Types.CHAR)
			return new FixChar(value.toString(), value.toString().length());
		else if (type == java.sql.Types.VARCHAR)
			return new VarChar(value.toString());
		return this;
	}
	
    public Object toJavaType() {
    	return value;
    }	
}
