package org.jactr.core.module.procedural.six.learning;

public class CannotCompileException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2133137119773723951L;

	public CannotCompileException(String message) {
		super(message);
	}

    /**
     * no need for stack traces here
     * 
     * @see java.lang.Throwable#fillInStackTrace()
     */
    @Override
    public Throwable fillInStackTrace()
    {
      return this;
    }

}
