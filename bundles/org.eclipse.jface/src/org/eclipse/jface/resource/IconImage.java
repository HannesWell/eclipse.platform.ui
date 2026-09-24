/*******************************************************************************
 * Copyright (c) 2026, 2026 Hannes Wellmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hannes Wellmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.41
 *
 */
public final class IconImage {

	public interface Redirector {
		ImageDescriptor get(String id);
	}
	// TODO: Try to avoid ImageDescriptor in the API, maybe it's unnecessary one
	// day.


	public interface IconOverride {

		public static IconOverride create(IconImage original, IPath iconPath) {
			throw new UnsupportedOperationException("Not yet implemented.");
		}

		// Fall back to override icons just based on their providing bundle and their path.
		public static IconOverride create(String originalBundle, String originalPath, IPath iconPath) {
			throw new UnsupportedOperationException("Not yet implemented.");
		}
	}

	public interface IconPack {
		// Could also just be a collection of icon overrides.
		// But an iconPack could have an id too (but that could also be in the
		// override).
		String id();

		Collection<IconOverride> overrides();
		// TODO: Maybe also provide IconPackFragments to allow contributions and
		// aggregation.
		// Or consider all icon packs as fragments and e.g. collect all (OSGi)
		// registered icon-packs with the same id respectively their overrides.

		// Then there could be preference to select the IconPack id. (maybe a 'main'
		// pack is necessary to also supply a nice label and description. So maybe again
		// explicit fragments).
	}

	public static void setIconPack(Collection<IconOverride> allOverrides) { // TODO: should effectively be a set? But to
																			// allow override overrides a collection
																			// would be more flexible.

		Map<String, IconOverride> overrides = allOverrides.stream()
				.collect(Collectors.toMap(o -> o.original().id, o -> o,
						// Take the last override
						(o1, o2) -> o2));

		// TODO: Apply all overrides
		// Reset all not overriden icons? A way to combine icon packs should be
		// possible. Maybe at API level by combining the sets of overrides?
	}

	// TODO: Or add a method like:
	// An icon pack can then call this method on the exposed constant
	// Requires a dependency on the original bundle. Is this good or bad?
	// Would it be better to override just based on Strings? Would make the coupling
	// more loose but easier to do wrong
	// Plus without the need for IDs, we could avoid the need for a global registry
	// and specifying an ID. But without a global registry resetting all would also
	// be impossible. But at least specifing an id is avoided.
	// Having a dedicated IconReplacement/Override class could make assembling packs
	// easier since multiple overrides could be resolved in some way and packs could
	// be stored as Collections of overrides.
	// To be more flexible Overrides it should probably also be possible to define
	// them by ID and maybe even by bundle and path since not all plugins will
	// probably adapt to this API.

	// TODO: Or maybe batch this somehow and only create override instances that
	// carry the information and are applied from an IconPack instance?
	public void overrideWith(IPath iconPath) {
		Class<?> callerClass = STACK_WALKER.getCallerClass();
		ImageDescriptor newImage = createDescriptor(iconPath, callerClass);
		ImageDescriptor previousImage = getActiveImage();

		Image image = RESOURCES.create(newImage); // count each widget!
		referencingWidgets.forEach((widget, setter) -> {
			setter.accept(widget, image);
		});
		RESOURCES.destroy(previousImage);

		this.overrideImage = newImage;
	} // TODO: Or make this just a factory for an IconOverride instance?

	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	private static final Map<String, IconImage> REGISTRY = new ConcurrentHashMap<>();

	private final String id;
	private final ImageDescriptor defaultImage;
	private final Map<Widget, BiConsumer<Widget, Image>> referencingWidgets = new HashMap<>();
	private ImageDescriptor overrideImage;

	private IconImage(String id, ImageDescriptor image) {
		this.id = id;
		this.defaultImage = image;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	public static IconImage create(String id, IPath iconPath) {
		Class<?> callerClass = STACK_WALKER.getCallerClass();
		ImageDescriptor image = createDescriptor(iconPath, callerClass);
		IconImage iconImage = new IconImage(id, image);
		if (REGISTRY.putIfAbsent(id, iconImage) != null) {
			// TODO: Externalize message
			throw new IllegalArgumentException("Icon with id is already registered: " + id); //$NON-NLS-1$
		}
		return iconImage;
	}

	private static ImageDescriptor createDescriptor(IPath iconPath, Class<?> callerClass) {
		String relativeIconPath = iconPath.makeRelative().toPortableString();
		return ImageDescriptor.createFromFile(callerClass, relativeIconPath);
	}

	// TODO: Because of this this class may only be initialized after the UI thread
	// was set. Delay it? A lazy constant would be nice to have here?
	private static final ResourceManager RESOURCES = JFaceResources.getResources(Display.getDefault());

	/**
	 * May only be called from the UI thread.
	 *
	 * @param <W>
	 * @param widget
	 * @param setter
	 */
	public <W extends Widget> void applyTo(W widget, BiConsumer<W, Image> setter) {
		ImageDescriptor activeImage = getActiveImage();
		Image image = referencingWidgets.isEmpty() // own reference-counting to reduce ResourceManager lookups
				? RESOURCES.create(activeImage)
				: RESOURCES.find(activeImage);
		setter.accept(widget, image);
		referencingWidgets.put(widget, (BiConsumer<Widget, Image>) setter);

		widget.addDisposeListener(e -> {
			referencingWidgets.remove(e.widget);
			if (referencingWidgets.isEmpty()) {
				ImageDescriptor nowActiveImage = getActiveImage();
				RESOURCES.destroy(nowActiveImage);
			}
		});
	}

	private ImageDescriptor getActiveImage() {
		return overrideImage != null ? overrideImage : defaultImage;
	}

	public void applyTo(Caret caret) {
		applyTo(caret, Caret::setImage);
	}

	public void applyTo(Item item) {
		applyTo(item, Item::setImage);
	}

	public void applyTo(Button button) {
		applyTo(button, Button::setImage);
	}

	public void applyTo(Label label) {
		applyTo(label, Label::setImage);
	}

	public void applyTo(Decorations widget) {
		widget.setImage(null);
	}

	public void applyTo(CLabel widget) {
		widget.setImage(null);
	}

}
