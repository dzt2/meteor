package lab.meteor.core;

public class MObject extends MElement {

	protected MObject() {
		super(MElementType.Object);
	}
	
	protected MObject(long id) {
		super(id, MElementType.Object);
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
