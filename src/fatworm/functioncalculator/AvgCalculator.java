package fatworm.functioncalculator;

import fatworm.dataentity.DataEntity;
import fatworm.dataentity.Int;
import fatworm.dataentity.NullDataEntity;

public class AvgCalculator extends FunctionCalculator {

	@Override
	public void update(FuncValue oldValue, DataEntity nextValue) {
		if (nextValue.isNull())
			return;
		if (oldValue.value.isNull()) 
			oldValue.value = nextValue.toType(java.sql.Types.FLOAT);
		else 
			oldValue.value = oldValue.value.opWith(nextValue, "+");
		oldValue.count++;
	}

	@Override
	public DataEntity getResult(FuncValue value) {
		if (value.count > 0)
			return value.value.opWith(new Int(value.count), "/");
		return new NullDataEntity();
	}
}
