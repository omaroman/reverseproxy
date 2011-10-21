package play.modules.reverseproxy.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SwitchScheme {

    SchemeType type() default SchemeType.UNSPECIFIED;
    boolean keepUrl() default false;

//    /**
//    * Does not intercept these actions
//    */
//    String[] unless() default {};
//    String[] only() default {};
//
//    /**
//    * Interceptor priority (0 is high priority)
//    */
//    int priority() default 0;
}
