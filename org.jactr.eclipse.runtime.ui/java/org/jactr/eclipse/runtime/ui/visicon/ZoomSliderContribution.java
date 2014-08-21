package org.jactr.eclipse.runtime.ui.visicon;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;

public class ZoomSliderContribution extends ControlContribution {

	private final CTabFolder tabs;
	private Slider slider;
	private Listener zoomListener;
	private int magnification;
	
	protected ZoomSliderContribution(CTabFolder tabs, float magnification) {
		super("zoom-slider");
		this.tabs = tabs;
		this.magnification = (int)(magnification*100);
		this.zoomListener = new ZoomListener();
		tabs.addListener(SWT.Selection, zoomListener);
	}

	protected void zoomCurrentTab() {
		if(tabs.getSelection() != null) {
			System.err.println("Selection="+slider.getSelection());
			((VisiconComponent)tabs.getSelection().getControl()).setMagnification((float)slider.getSelection()/100.0f);
		}
	}
	
	float getMagnification() {
		if(slider == null)
			return (float)magnification/100.0f;
		else
			return (float)slider.getSelection()/100.0f;
	}

	@Override
	protected Control createControl(Composite parent) {
		slider = new Slider(parent, SWT.NONE);
		slider.setValues(magnification, 20, 200, slider.getThumb(), slider.getIncrement(), slider.getPageIncrement());
		slider.addListener(SWT.Selection, zoomListener);
		return slider;
	}
	
	private class ZoomListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			zoomCurrentTab();
		}
		
	}
}
