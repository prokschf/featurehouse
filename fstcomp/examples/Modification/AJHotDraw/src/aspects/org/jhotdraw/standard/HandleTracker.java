
package org.jhotdraw.standard; 
import java.awt.event.MouseEvent; 
import org.jhotdraw.framework.*; 
public  class  HandleTracker  extends AbstractTool {
		private Handle fAnchorHandle;

		public HandleTracker(DrawingEditor newDrawingEditor, Handle anchorHandle) {	super(newDrawingEditor);	fAnchorHandle = anchorHandle;	}

		public void mouseDown(MouseEvent e, int x, int y) {	super.mouseDown(e, x, y);	fAnchorHandle.invokeStart(x, y, view());	}

		public void mouseDrag(MouseEvent e, int x, int y) {	super.mouseDrag(e, x, y);	fAnchorHandle.invokeStep(x, y, getAnchorX(), getAnchorY(), view());	}

		public void mouseUp(MouseEvent e, int x, int y) {	super.mouseUp(e, x, y);	fAnchorHandle.invokeEnd(x, y, getAnchorX(), getAnchorY(), view());	}

		public void activate() {	}


}