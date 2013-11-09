package lab.meteor.visualize.diagram;

import lab.meteor.core.MElement;

public interface IModelView<T extends MElement> {
	void setModel(T model);
	T getModel();
}
