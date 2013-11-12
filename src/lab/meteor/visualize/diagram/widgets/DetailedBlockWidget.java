package lab.meteor.visualize.diagram.widgets;

import co.gongzh.snail.View;
import co.gongzh.snail.event.EventHandler;
import co.gongzh.snail.event.Key;
import co.gongzh.snail.text.TextView;
import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.diagram.ListView;

public abstract class DetailedBlockWidget extends BlockWidget {

	ListView listView;
	final static int margin = 2;
	
	public DetailedBlockWidget(DiagramView v) {
		super(v);
		listView = new ListView();
		titleView.addEventHandler(TextView.TEXT_LAYOUT_CHANGED, new EventHandler() {
			
			@Override
			public void handle(View sender, Key key, Object arg) {
				titleView.setSize(getWidth() - 2, titleView.getPreferredHeight());
				listView.setPosition(margin, titleView.getHeight() + margin);
				listView.setSize(getWidth() - margin * 2, getHeight() - titleView.getHeight() - margin * 2);
			}
		});
		
		contentView.addSubview(listView);
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		listView.setPosition(margin, titleView.getHeight() + margin);
		listView.setSize(width - margin * 2, height - titleView.getHeight() - margin * 2);
	}
	
}
