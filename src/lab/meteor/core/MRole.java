package lab.meteor.core;

public class MRole extends MElement {
	
	public MRole(MClass clsa, MClass clsb, 
			String namea, String nameb, 
			Multiplicity multia, Multiplicity multib) {
		super(MElementType.Role);
		
		if (clsa == null || clsa.isDeleted() || clsb == null || clsb.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (clsa.hasChild(namea) || clsb.hasChild(nameb))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		if (multia == Multiplicity.None && multib == Multiplicity.None)
			throw new MException(MException.Reason.INVALID_MULTIPLICITY);
		
		this.initialize();
		this.class_a = clsa;
		this.class_b = clsb;
		this.name_a = namea;
		this.name_b = nameb;
		this.multi_a = multia;
		this.multi_b = multib;
		
		MDatabase.getDB().createElement(this);
	}
	
	protected MRole(long id) {
		super(id, MElementType.Role);
	}

	private MClass class_a;
	private String name_a;
	private Multiplicity multi_a;
	
	private MClass class_b;
	private String name_b;
	private Multiplicity multi_b;
	
	public static enum Multiplicity {
		None,
		One,
		Multiple
	}
	
	public MClass getClassA() {
		return this.class_a;
	}
	
	public MClass getClassB() {
		return this.class_b;
	}
	
	public String getNameA() {
		return this.name_a;
	}
	
	public void setNameA(String name) {
		// TODO
	}
	
	public String getNameB() {
		return this.name_b;
	}
	
	public void setNameB(String name) {
		// TODO
	}
	
	public Multiplicity getMultiplicityA() {
		return this.multi_a;
	}
	
	public void setMultiplicityA(Multiplicity multi) {
		if (this.multi_b == Multiplicity.None && multi == Multiplicity.None)
			throw new MException(MException.Reason.INVALID_MULTIPLICITY);
		this.multi_a = multi;
	}
	
	public Multiplicity getMultiplicityB() {
		return this.multi_b;
	}
	
	public void setMultiplicityB(Multiplicity multi) {
		if (this.multi_a == Multiplicity.None && multi == Multiplicity.None)
			throw new MException(MException.Reason.INVALID_MULTIPLICITY);
		this.multi_b = multi;
	}

	@Override
	void loadFromDBInfo(Object dbInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void saveToDBInfo(Object dbInfo) {
		// TODO Auto-generated method stub
		
	}
	
	
}
