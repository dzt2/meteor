package lab.meteor.core;

public abstract class MCollection implements MNotifiable {
	
	private MNotifiable parent;
	
	public MCollection(MNotifiable parent) {
		if (parent == null)
			throw new MException(MException.Reason.NULL_NOTIFICABLE);
	}
	
	@Override
	public void notifyChanged() {
		this.parent.notifyChanged();
	}
	
	public static interface ForEachCallback {
		void function(Object o);
	}
	
	public abstract void forEach(ForEachCallback callback);
	
	protected static Object toInputObject(Object o) {
		if (o instanceof MElement)
			return new MElementPointer((MElement) o);
		return o;
	}
	
	protected static Object toOutputObject(Object o) {
		if (o instanceof MElementPointer)
			return ((MElementPointer) o).getElement();
		return o;
	}
	
	protected static void checkType(Object o) {
		if (!MUtility.isValidValue(o))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
	}
}
