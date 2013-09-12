package lab.meteor.core;


public abstract class MCollection implements MNotifiable {
	
	public static Creator List = Creator.List;
	public static Creator Set = Creator.Set;
	public static Creator Dictionary = Creator.Dictionary;
	
	private MNotifiable parent;
	
	protected MCollection(MNotifiable parent) {
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
	
	protected Object toInputObject(Object o) {
		if (o instanceof MElement) {
			return new MElementPointer((MElement) o);
		} else if (o instanceof MCollection.Creator) {
			return MCollection.createCollection((MCollection.Creator) o, this);
		}
		return o;
	}
	
	protected Object toOutputObject(Object o) {
		if (o instanceof MElementPointer)
			return ((MElementPointer) o).getElement();
		return o;
	}
	
	protected static void checkType(Object o) {
		if (o instanceof MCollection.Creator)
			return;
		if (!MUtility.isValidValue(o))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
	}
	
	protected static MCollection createCollection(Creator creator, MNotifiable parent) {
		switch (creator) {
		case List:
			return new MList(parent);
		case Set:
			return new MSet(parent);
		case Dictionary:
			return new MDictionary(parent);
		}
		return null;
	}
	
	public enum Creator {
		List,
		Set,
		Dictionary
	}
}
