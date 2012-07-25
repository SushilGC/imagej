/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ext.ui.swt;

import imagej.ext.module.ui.ObjectWidget;
import imagej.ext.module.ui.WidgetModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * SWT implementation of multiple choice selector widget.
 * 
 * @author Curtis Rueden
 */
public class SWTObjectWidget extends SWTInputWidget<Object> implements
	ObjectWidget
{

	private final Combo combo;
	private final Object[] items;

	public SWTObjectWidget(final Composite parent, final WidgetModel model,
		final Object[] items)
	{
		super(parent, model);
		this.items = items;

		combo = new Combo(this, SWT.DROP_DOWN);
		for (final Object item : items) combo.add(item.toString());

		refreshWidget();
	}

	// -- InputWidget methods --

	@Override
	public boolean isCompatible(final WidgetModel model) {
		return true;
	}

	@Override
	public Object getValue() {
		return items[combo.getSelectionIndex()];
	}

	@Override
	public void refreshWidget() {
		final Object value = getModel().getValue();
		for (int i = 0; i < items.length; i++) {
			final Object item = items[i];
			if (item == value) {
				combo.select(i);
				break;
			}
		}
	}

}
