package fatworm.absyn;

import fatworm.dataentity.DataEntity;
import fatworm.query.Env;

public class FuncValue extends Value{
	String func;
	ColumnValue val;
	public FuncValue(String func, Value val){
		this.func = func;
		this.val = (ColumnValue)val;
	}
	@Override
	public DataEntity getValue(Env env) {
		// TODO Auto-generated method stub
		return null;
	}
	public String toString(){
		return func+"("+val.toString()+")";
	}
}
