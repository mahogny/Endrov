package bioserv.biceps;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation of a function that should be exposed with RMI
 * @author Johan Henriksson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NetFunc
	{
	String name(); //default "[unassigned]"; 

	
	}


/*
@RequestForEnhancement(
    id       = 2868724,
    synopsis = "Enable time-travel",
    engineer = "Mr. Peabody",
    date     = "4/1/3007"
)
public static void travelThroughTime(Date destination) { ... }
*/