/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.source;

/**
 * Extension interface for <code>ISourceViewer</code>.
 * Extends the source viewer with the concept of an annotation overview.
 * The annotation overview differs from the annotation presentation in that it is
 * independent from the viewer's viewport, i.e. the annotations of the 
 * whole document are visible. There are no assumptions about 
 * the area in which the annotation overview is shown.<p>
 * As the visibility of annotation overview can dynamically be changed, 
 * it is assumed that the presentation area can dynamically 
 * be hidden if it is different from the text widget.<p>
 * Clients may implement this interface or use the default implementation provided
 * by <code>SourceViewer</code>.
 * 
 * @since 2.1
 */
public interface ISourceViewerExtension {
	
	/**
	 * Shows/hides an overview representation of the annotations of the whole document of this viewer.
	 * 
	 * @param show <code>true</code> if annotation overview should be visible, <code>false</code> otherwise
	 */
	void showAnnotationsOverview(boolean show);
}
