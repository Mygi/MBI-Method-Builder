package daris.gui.util;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;

public class ImageUtil {

	/**
	 * 
	 * @param ir
	 *            The image resource
	 * @param dir
	 *            The disabled image resource
	 * @param hir
	 *            The hover image resource
	 * @return
	 */
	public static arc.gui.gwt.widget.image.Image createImage(ImageResource ir,
			ImageResource dir, ImageResource hir) {

		arc.gui.gwt.widget.image.Image i = new arc.gui.gwt.widget.image.Image(
				ir.getURL(), ir.getWidth(), ir.getHeight());
		if (dir != null) {
			arc.gui.image.Image di = new arc.gui.image.Image(dir.getURL(),
					dir.getWidth(), dir.getHeight());
			i.setDisabledImage(di);
		}
		if (hir != null) {
			arc.gui.image.Image hi = new arc.gui.image.Image(hir.getURL(),
					hir.getWidth(), hir.getHeight());
			i.setHoverImage(hi);
		}
		i.style().setPaddingTop(0, Unit.PX);
		return i;
	}

	/**
	 * 
	 * @param ir
	 *            The image resource
	 * @param dir
	 *            The disabled image resource
	 * @return
	 */
	public static arc.gui.gwt.widget.image.Image createImage(ImageResource ir,
			ImageResource dir) {

		return createImage(ir, dir, null);
	}

	/**
	 * 
	 * @param ir
	 *            The image resource
	 * @return
	 */
	public static arc.gui.gwt.widget.image.Image createImage(ImageResource ir) {

		return createImage(ir, null, null);
	}
}
