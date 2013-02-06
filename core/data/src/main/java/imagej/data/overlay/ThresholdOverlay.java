/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.data.overlay;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Displayable;
import imagej.util.Colors;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.condition.Condition;
import net.imglib2.ops.condition.WithinRangeCondition;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.pointset.ConditionalPointSet;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetRegionOfInterest;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.numeric.RealType;

/**
 * A {@link ThresholdOverlay} is an {@link Overlay} that represents the set of
 * points whose data values are in a range prescribed by API user.
 * 
 * @author Barry DeZonia
 */
public class ThresholdOverlay extends AbstractOverlay {

	// -- instance variables --

	private Displayable figure;
	private final Dataset dataset;
	private final ConditionalPointSet points;
	private final WithinRangeCondition<? extends RealType<?>> condition;
	private final RegionOfInterest regionAdapter;

	// -- ThresholdOverlay methods --

	/**
	 * Construct a {@link ThresholdOverlay} on a {@link Dataset} given an
	 * {@link ImageJ} context.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ThresholdOverlay(ImageJ context, Dataset dataset)
	{
		setContext(context);
		this.dataset = dataset;
		ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		Function<long[], ? extends RealType<?>> function =
			new RealImageFunction(imgPlus, imgPlus.firstElement());
		condition =
			new WithinRangeCondition(function, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		long[] dims = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dims);
		HyperVolumePointSet volume = new HyperVolumePointSet(dims);
		points = new ConditionalPointSet(volume, condition);
		regionAdapter = new PointSetRegionOfInterest(points);
		figure = null;
		setName();
		setAlpha(255);
		setFillColor(Colors.RED);
		setLineColor(Colors.RED);
		setLineEndArrowStyle(ArrowStyle.NONE);
		setLineStartArrowStyle(ArrowStyle.NONE);
		setLineStyle(LineStyle.NONE);
		setLineWidth(1);
		initColor();
		resetThreshold();
	}
	
	/**
	 * Construct a {@link ThresholdOverlay} on a {@link Dataset} given an
	 * {@link ImageJ} context, and a numeric range within which the data values of
	 * interest exist.
	 */
	public ThresholdOverlay(ImageJ context, Dataset ds, double min, double max)
	{
		this(context, ds);
		setRange(min,max);
	}

	/**
	 * Helper method used by services to tie this overlay to its graphic
	 * representation. Sometimes this overlay needs to redraw its graphics in
	 * response to changes in the threshold values. Various services may use this
	 * method but this method is not for general consumption.
	 */
	public void setFigure(Displayable figure) {
		this.figure = figure;
	}

	/**
	 * Returns the {@link Displayable} figure associated with this overlay.
	 */
	public Displayable getFigure() {
		return figure;
	}

	/**
	 * Sets the range of interest for this overlay. As a side effect the name of
	 * the overlay is updated.
	 */
	public void setRange(double min, double max) {
		condition.setMin(min);
		condition.setMax(max);
		points.setCondition(condition); // this lets PointSet know it is changed
		setName();
	}

	/**
	 * Gets the lower end of the range of interest for this overlay.
	 */
	public double getRangeMin() {
		return condition.getMin();
	}

	/**
	 * Gets the upper end of the range of interest for this overlay.
	 */
	public double getRangeMax() {
		return condition.getMax();
	}

	/**
	 * Resets the range of interest of this overlay to default values as provided
	 * by the {@link ThresholdService}.
	 */
	public void resetThreshold() {
		ThresholdService threshSrv =
			getContext().getService(ThresholdService.class);
		double min = threshSrv.getDefaultRangeMin();
		double max = threshSrv.getDefaultRangeMax();
		setRange(min, max);
	}

	/**
	 * Returns the set of points whose data values are within the range of
	 * interest.
	 */
	public PointSet getPoints() {
		return points;
	}
	
	/**
	 * Returns the {@link Condition} of the {@link ThresholdOverlay}. This is used
	 * by others (like the rendering code) to quickly iterate the portion of the
	 * data points they are interested in.
	 * <p>
	 * By design the return value is not a {@link WithinRangeCondition}. Users
	 * cannot poke the threshold values via this Condition. This would bypass
	 * internal communication. API users should call setRange(min, max) on this
	 * {@link ThresholdOverlay} if they want to manipulate the display range.
	 */
	public Condition<long[]> getCondition() {
		return condition;
	}

	// -- Overlay methods --

	@Override
	public void update() {
		if (figure != null) figure.draw();
	}

	@Override
	public void rebuild() {
		update(); // TODO - is this all we need to do? I think so.
	}

	@Override
	public boolean isDiscrete() {
		return true;
	}

	@Override
	public int getAxisIndex(AxisType axis) {
		return dataset.getAxisIndex(axis);
	}

	@Override
	public AxisType axis(int d) {
		return dataset.axis(d);
	}

	@Override
	public void axes(AxisType[] axes) {
		dataset.axes(axes);
	}

	@Override
	public void setAxis(AxisType axis, int d) {
		dataset.setAxis(axis, d);
	}

	@Override
	public double calibration(int d) {
		return dataset.calibration(d);
	}

	@Override
	public void setCalibration(double cal, int d) {
		if (cal == 1 && (d == 0 || d == 1)) return;
		throw new IllegalArgumentException(
			"Cannot set calibration of a ThresholdOverlay");
	}

	@Override
	public int numDimensions() {
		return points.numDimensions();
	}

	@Override
	public long min(int d) {
		return points.min(d);
	}

	@Override
	public long max(int d) {
		return points.max(d);
	}

	@Override
	public double realMin(int d) {
		return min(d);
	}

	@Override
	public double realMax(int d) {
		return max(d);
	}

	@Override
	public void dimensions(long[] dimensions) {
		points.dimensions(dimensions);
	}

	@Override
	public long dimension(int d) {
		return points.dimension(d);
	}

	@Override
	public RegionOfInterest getRegionOfInterest() {
		return regionAdapter;
	}

	@Override
	public ThresholdOverlay duplicate() {
		ThresholdOverlay overlay =
			new ThresholdOverlay(getContext(), dataset, condition.getMin(), condition
				.getMax());
		overlay.setFillColor(getFillColor());
		return overlay;
	}

	@Override
	public void move(double[] deltas) {
		// do nothing - thresholds don't move though space
	}

	// -- helpers --

	private void setName() {
		setName("Threshold: " + condition.getMin() + " to " + condition.getMax());
	}

	private void initColor() {
		ThresholdService threshSrv =
			getContext().getService(ThresholdService.class);
		setFillColor(threshSrv.getDefaultColor());
	}
}