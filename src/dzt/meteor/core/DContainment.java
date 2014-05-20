package dzt.meteor.core;

public interface DContainment {
	
	public DContainment getParent();
	public void setParent(DContainment parent);
	
	public DContainment[] getChildren();
	public void addChild(DContainment child);
	public DContainment removeChild(int i);
	public DContainment removeChild(String name);
	public DContainment getChild(int i);
	public int getChildrenSize();
	public DContainment getChild(String name);
	public String[] getChildNames();
	
	public String getName();
	public void setName(String name);
	
	public boolean isLeaf();
}
