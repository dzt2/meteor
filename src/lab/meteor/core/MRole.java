package lab.meteor.core;

public class MRole {
	
	private MClass class_a;
	private String name_a;
	private Multiplicity multi_a;
	
	private MClass class_b;
	private String name_b;
	private Multiplicity multi_b;
	
	public static enum Multiplicity {
		One,
		Multiple
	}
}
