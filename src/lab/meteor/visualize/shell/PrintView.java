package lab.meteor.visualize.shell;

import java.awt.Color;

import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.View;
import co.gongzh.snail.text.TextView;

public class PrintView extends View implements IPrinter {
	
	TextView textView;
	
	final static int padding = 10;
	
	public PrintView() {
		textView = new TextView();
		textView.setPosition(padding, padding);
		textView.setDefaultTextColor(Color.white);
		textView.setFont(Resources.FONT_CMD_PRINT);
		setBackgroundColor(new Color(0xee000000));
	}
	
	@Override
	public void setSize(int width, int height) {
		textView.setSize(width - padding * 2, height - padding * 2);
		super.setSize(width, height);
	}

	@Override
	public void print(String content) {
		textView.getText().append(content, Resources.FONT_CMD_PRINT, Color.white);
	}

	@Override
	public void onCommandFinished(String command, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printError(String message) {
		textView.getText().append(message, Resources.FONT_CMD_PRINT, Color.red);
	}
}
