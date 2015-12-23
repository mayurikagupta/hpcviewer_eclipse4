package edu.rice.cs.hpc.data.experiment.metric.format;

import java.text.DecimalFormat;

public class FormatStyle 
{
	/** The kind of numeric display to be used, either FIXED or FLOAT. */
	public int kind;

	/** The number of characters to be ysed for the number. */
	public int fieldWidth;

	/** The number of digits to be used for the fractional part. */
	public int fractionDigits;

	/** Whether to show the actual value. */
	public boolean show;

	/** A Java formatter implementing the format specified for actual values. */
	public DecimalFormat formatter;

}
