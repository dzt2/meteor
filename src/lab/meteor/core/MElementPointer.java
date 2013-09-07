package lab.meteor.core;

import lab.meteor.core.MElement.MElementType;

public class MElementPointer {
	
	private long id;
	
	private MElementType eType;
	
	public MElementPointer() {
		this(null);
	}
	
	public MElementPointer(long id, MElementType eType) {
		this.id = id;
		this.eType = eType;
	}
	
	public MElementPointer(MElement ele) {
		if (ele == null) {
			this.id = MElement.NULL_ID;
			this.eType = null;
		} else {
			this.id = ele.id;
			this.eType = ele.getElementType();
		}
	}
	
	public long getID() {
		return id;
	}
	
	public MElementType getElementType() {
		return this.eType;
	}
	
	public void setPointer(MElement ele) {
		this.id = ele.id;
		this.eType = ele.getElementType();
	}
	
	public MElement getElement() {
		switch (this.eType) {
		case Class:
			return MDatabase.getInstance().getClass(id);
		case Attribute:
			return MDatabase.getInstance().getAttribute(id);
		case Enum:
			return MDatabase.getInstance().getEnum(id);
		case Symbol:
			return MDatabase.getInstance().getSymbol(id);
		case Package:
			return MDatabase.getInstance().getPackage(id);
		case Object:
			return MDatabase.getInstance().getLazyObject(id);
		case Tag:
			return MDatabase.getInstance().getLazyTag(id);
		default:
			return null;
		}
	}
	
	/**
	 * Same with Long.
	 */
	@Override
	public int hashCode() {
        return (int)(id ^ (id >>> 32));
    }
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MElementPointer) {
			return ((MElementPointer) obj).id == this.id;
		} else if (obj instanceof MElement) {
			return ((MElement) obj).id == this.id;
		}
		return false;
	}
	
}
