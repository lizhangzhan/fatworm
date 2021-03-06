package fatworm.query;

import java.util.HashSet;
import java.util.Set;

//import fatworm.record.Schema;

/**
 * The interface implemented by each query plan.
 * There is a Plan class for each relational algebra operator.
 * @author Edward Sciore
 *
 */
public abstract class QueryPlan {
   
	Set<String> funcSet = new HashSet<String>();
    /**
     * Opens a scan corresponding to this plan.
     * The scan will be positioned before its first record.
     * @return a scan
     */
    abstract public Scan   open();

    /**
     *
     */   
    public void addFunctionsToCalc(Set<String> funcs){
    	funcSet.addAll(funcs);
    }
   // /**
   //  * Returns an estimate of the number of block accesses
   //  * that will occur when the scan is read to completion.
   //  * @return the estimated number of block accesses
   //  */
   // public int    blocksAccessed();
   
   // /**
   //  * Returns an estimate of the number of records
   //  * in the query's output table.
   //  * @return the estimated number of output records
   //  */
   // public int    recordsOutput();
   
   // /**
   //  * Returns an estimate of the number of distinct values
   //  * for the specified field in the query's output table.
   //  * @param fldname the name of a field
   //  * @return the estimated number of distinct field values in the output
   //  */
   // public int    distinctValues(String fldname);
   
   // /**
   //  * Returns the schema of the query.
   //  * @return the query's schema
   //  */
   //  public Schema schema();
}
