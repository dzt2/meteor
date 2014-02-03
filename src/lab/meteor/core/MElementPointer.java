package lab.meteor.core;

import lab.meteor.core.MElement.MElementType;

/**
 * A pointer to an element.
 * @author Qiang
 * @see MElement
 */
public class MElementPointer implements Comparable<MElementPointer> {
	
	/**
	 * ID of element.
	 */
	private long id;
	
	/**
	 * The type of element. It's easier to get the origin element with element type.
	 */
	private MElementType eType;
	
	/**
	 * Create a null pointer.
	 */
	public MElementPointer() {
		this(null);
	}
	
	public MElementPointer(long id) {
		this(id, MDatabase.getDB().getElementType(id));
	}
	
	/**
	 * Create a pointer.
	 * @param id The ID of element.
	 * @param eType The element type.
	 */
	public MElementPointer(long id, MElementType eType) {
		this.id = id;
		this.eType = eType;
	}
	
	/**
	 * Create a pointer.
	 * @param ele The element.
	 */
	public MElementPointer(MElement ele) {
		if (ele == null) {
			this.id = MElement.NULL_ID;
			this.eType = null;
		} else {
			this.id = ele.id;
			this.eType = ele.getElementType();
		}
	}
	
	/**
	 * ID of element.
	 * @return
	 */
	public long getID() {
		return id;
	}
	
	/**
	 * Type of element.
	 * @return
	 */
	public MElementType getElementType() {
		return this.eType;
	}
	
	/**
	 * Point an element.
	 * @param ele The element.
	 */
	public void setPointer(MElement ele) {
		this.id = ele.id;
		this.eType = ele.getElementType();
	}
	
	/**
	 * Get pointed element.
	 * I can get Object in DataBase from an Element Pointer!
	 * (1) MElementPointer p=new MElementPointer(id,etype);
	 * (2) p.getElement();
	 * @return
	 */
	public MElement getElement() {
		switch (this.eType) {
		case Class:
			return MDatabase.getDB().getClass(id);
		case Attribute:
			return MDatabase.getDB().getAttribute(id);
		case Reference:
			return MDatabase.getDB().getReference(id);
		case Enum:
			return MDatabase.getDB().getEnum(id);
		case Symbol:
			return MDatabase.getDB().getSymbol(id);
		case Package:
			return MDatabase.getDB().getPackage(id);
		case Object:
			return MDatabase.getDB().getLazyObject(id);
		case Tag:
			return MDatabase.getDB().getLazyTag(id);
		default:
			return null;
		}
	}
	
	public boolean isNull() {
		return id == MElement.NULL_ID;
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


	@Override
	public int compareTo(MElementPointer o) {
		return Long.compare(id, o.id);
	}
	
}
