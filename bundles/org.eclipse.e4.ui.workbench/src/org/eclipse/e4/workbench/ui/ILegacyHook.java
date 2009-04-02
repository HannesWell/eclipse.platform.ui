/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.workbench.MPerspective;

public interface ILegacyHook {
	public void loadMenu(IEclipseContext context, MMenu menuModel);

	public void loadToolbar(MToolBar toolbar);

	public void loadPerspective(MPerspective<?> perspModel);
}
