package lab.meteor.visualize.io;

import lab.meteor.visualize.MainView;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.event.EventHandler;
import co.gongzh.snail.event.Key;

public class ImportButton extends View {

	ImportDialog dialog;
	
	MainView rootView;
	
	public ImportButton(MainView root) {
		setSize(48, 48);
		setBackgroundColor(null);
		this.rootView = root;
		dialog = new ImportDialog();
		dialog.setAlpha(0);
		this.addEventHandler(MOUSE_CLICKED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				if (dialog.isOpened)
					return;
				dialog.setPosition((rootView.getWidth() - dialog.getWidth()) / 2,
						(rootView.getHeight() - dialog.getHeight()) / 2);
				rootView.addSubview(dialog);
				ImportButton.this.dialog.show();
			}
			
		});
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.drawImage(Resources.IMG_IMPORT, 0, 0, getWidth(), getHeight());
	}
}
