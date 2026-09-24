package org.eclipse.jface.resource.icon;

import java.util.Collection;
import java.util.function.BiConsumer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.4
 *
 */
public abstract class Icons {

	// TODO: Register packs/pack-fragments as OSGi service components
	// Load them at start-up or when checking which id's are available
	// At startup read the selected id and load/apply all overrides from all
	// pack-fragments with that id.
	// Consider service ranks and let higher ranked services override lower ranked
	// ones.

	// Additionally allow the user to define 'back-up' themes, i.e. a list of
	// themes, that are considered if the 'main' theme doesn't override an icon.

	public interface IconPackFragment {
		String id();

		Collection<IconOverride> overrides();
	}

	public interface IconPack extends IconPackFragment {
		// Intended to be registered as OSGi service (or loaded via the ServiceLoader).

		// Could also just be a collection of icon overrides.
		// But an iconPack could have an id too (but that could also be in the
		// override).

		/**
		 * Id of icIconPack (non fragment must be unique)!
		 */
		@Override
		String id();

		String label();

		String description();

//		@Override
//		Collection<IconOverride> overrides();
		// TODO: Maybe also provide IconPackFragments to allow contributions and
		// aggregation.
		// Or consider all icon packs as fragments and e.g. collect all (OSGi)
		// registered icon-packs with the same id respectively their overrides.

		// Then there could be preference to select the IconPack id. (maybe a 'main'
		// pack is necessary to also supply a nice label and description. So maybe again
		// explicit fragments).
	}

	// Fall back to override registered icons without having a dependency on the
	// original bundle
	public abstract IconReference reference(String iconID);

	// Fall back to override icons just based on their providing bundle and their
	// path.
	public abstract IconReference reference(String bundleSymbolicName, IPath iconPath);
	// We could still search in the registry for icons from the listed bundle with
	// the given path.

	// References created based on ID and bsn+path may find their icon image when
	// it's registered alter

	public interface IconReference {
		IconOverride overrideWith(IPath iconPath); // uses the caller bundle
	}

	public interface IconOverride {
	}

	public interface IconImage extends IconReference {

		static IconImage create(String id, IPath iconPath) {// uses the caller bundle
			throw new UnsupportedOperationException("Not yet implemented."); //$NON-NLS-1$
		}

		/**
		 * @return Returns the id.
		 */
		String getId(); // TODO: Required for users?

		public <W extends Widget> void applyTo(W widget, BiConsumer<W, Image> setter);

		// TODO: Introduce an interface in SWT like IconReceiving Widget
		interface ImageReciever {
			void setImage(Image image);
		}

		default void applyTo(Caret caret) {
			applyTo(caret, Caret::setImage);
		}

		default void applyTo(Item item) {
			applyTo(item, Item::setImage);
		}

		default void applyTo(Button button) {
			applyTo(button, Button::setImage);
		}

		default void applyTo(Label label) {
			applyTo(label, Label::setImage);
		}

		default void applyTo(Decorations widget) {
			widget.setImage(null);
		}

		default void applyTo(CLabel widget) {
			widget.setImage(null);
		}

	}

}
