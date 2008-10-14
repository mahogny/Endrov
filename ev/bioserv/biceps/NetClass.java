package bioserv.biceps;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation of a class where functions should be exposed using RMI.
 * The class loader has to replace it on callee side
 * 
 * @author Johan Henriksson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NetClass
	{
	}



//class loader: take a class, find annot, and return a generated stub from it