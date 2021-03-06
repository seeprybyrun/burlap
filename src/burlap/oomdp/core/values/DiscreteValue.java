package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;


/**
 * A discrete value subclass in which discrete values are stored as int values. The int values correspond to the attributes
 * categorical list of discrete values, so the int values should always be >= 0 unless it is unset, which is specified by a value of -1.
 * @author James MacGlashan
 *
 */
public class DiscreteValue extends Value{
	
	/**
	 * The discrete value stored as an integer. If the attribute
	 * defines categorical values with strings, this int value represents
	 * the index in the list of categorical values. The default value
	 * of -1 indicates an unset attribute value.
	 */
	protected int			discVal = -1;
	
	
	/**
	 * Initializes this value to be an assignment for Attribute attribute.
	 * @param attribute
	 */
	public DiscreteValue(Attribute attribute){
		super(attribute);
	}
	
	/**
	 * Initializes this value as a copy from the source Value object v.
	 * @param v the source Value to make this object a copy of.
	 */
	public DiscreteValue(Value v){
		super(v);
		DiscreteValue dv = (DiscreteValue)v;
		this.discVal = dv.discVal;
	}

	@Override
	public boolean valueHasBeenSet() {
		return this.discVal != -1;
	}

	@Override
	public Value copy(){
		return new DiscreteValue(this);
	}
	
	@Override
	public void setValue(int v){
		this.discVal = v;
	}
	
	@Override
	public void setValue(double v){
		this.discVal = (int)v;
	}
	
	@Override
	public void setValue(boolean v) {
		if(v){
			this.discVal = 1;
		}
		else{
			this.discVal = 0;
		}
	}
	
	@Override
	public void setValue(String v){
		int intv = attribute.discValuesHash.get(v);
		discVal = intv;
	}
	
	@Override
	public void addRelationalTarget(String t) {
		throw new UnsupportedOperationException("Value is discrete, cannot add relational target");
	}
	
	@Override
	public void addAllRelationalTargets(Collection<String> targets) {
		throw new UnsupportedOperationException("Value is discrete, cannot add relational targets");
	}
	
	@Override
	public void clearRelationTargets() {
		throw new UnsupportedOperationException("Value is discrete, cannot clear relational targets");
	}
	
	@Override
	public void removeRelationalTarget(String target) {
		throw new UnsupportedOperationException("Value is discrete, cannot modify relational target");
	}
	
	@Override
	public int getDiscVal(){
		if(this.discVal == -1){
			throw new UnsetValueException();
		}
		return this.discVal;
	}
	
	@Override
	public double getRealVal(){
		throw new UnsupportedOperationException("Value is discrete, cannot return real value");
	}
	
	@Override
	public String getStringVal(){
		if(this.discVal == -1){
			throw new UnsetValueException();
		}
		return attribute.discValues.get(discVal);
	}
	
	@Override
	public Set<String> getAllRelationalTargets() {
		throw new UnsupportedOperationException("Value is discrete, cannot return relational values");
	}
	
	
	
	@Override
	public double getNumericRepresentation() {
		if(this.discVal == -1){
			throw new UnsetValueException();
		}
		return (double)this.discVal;
	}
	
	@Override
	public boolean equals(Object obj){
		
		if(!(obj instanceof DiscreteValue)){
			return false;
		}
		
		DiscreteValue op = (DiscreteValue)obj;
		if(!op.attribute.equals(attribute)){
			return false;
		}
		
		return discVal == op.discVal;
		
	}

	@Override
	public boolean getBooleanValue() {
		return this.discVal != 0;
	}

	
	@Override
	public void setValue(int[] intArray) {
		throw new UnsupportedOperationException("Value is discrete; cannot be set to an int array.");
	}


	@Override
	public void setValue(double[] doubleArray) {
		throw new UnsupportedOperationException("Value is discrete; cannot be set to a double array.");
	}


	@Override
	public int[] getIntArray() {
		throw new UnsupportedOperationException("Value is discrete; cannot return an int array.");
	}


	@Override
	public double[] getDoubleArray() {
		throw new UnsupportedOperationException("Value is discrete; cannot return a double array.");
	}
	

	


	
	
	
}
