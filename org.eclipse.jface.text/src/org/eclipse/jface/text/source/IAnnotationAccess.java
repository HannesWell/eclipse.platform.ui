/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.text.source;

/**
 * IAnnotationAccess.java
 */
public interface IAnnotationAccess {

	/**
	 * Returns the type of the given annotation.
	 * @param annotation the annotation
	 * @return the type of the given annotation or <code>null</code> if the type
	 * is unknown
	 */
	Object getType(Annotation annotation);

	/**
	 * Returns whether the given annotation spans multiple lines.
	 * @param annotation the annotation
	 * @return <code>true</code> if the annotation spans multiple lines,
	 * <code>false</code> otherwise
	 */
	boolean isMultiLine(Annotation annotation);
	
	/**
	 * Returns whether the given annotation is temporary rather than persistent.
	 * @param annotation the annotation
	 * @return <code>true</code> if the annotation is temporary,
	 * <code>false</code> otherwise
	 */
	boolean isTemporary(Annotation annotation);
}
