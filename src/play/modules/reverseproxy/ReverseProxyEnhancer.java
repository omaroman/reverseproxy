/**
 * Author: OMAROMAN
 * Date: 9/21/11
 * Time: 6:08 PM
 *
 * NOTE:
 * This class is no longer used, but it stays in the source code as an example of Bytecode Enhancement
 */

package play.modules.reverseproxy;

import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ConstPool;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.*;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

import play.modules.reverseproxy.annotations.GlobalSwitchScheme;
import play.modules.reverseproxy.annotations.SchemeType;
import play.modules.reverseproxy.annotations.SwitchScheme;

import play.mvc.With;

import play.Logger;

import net.parnassoft.playutilities.EnhancerUtility;

/**
 * Inject @With(ReverseProxyController.class) if controller has at least one method annotated with @SwitchScheme
 * or controller is annotated with @GlobalSwitchScheme
 */
@Deprecated
public class ReverseProxyEnhancer extends Enhancer {

    @Override
	public void enhanceThisClass(ApplicationClass appClass) throws Exception {

		CtClass ctClass = makeClass(appClass);

        // Only enhance class if is-a Controller
        if (!EnhancerUtility.isAController(classPool, ctClass)) {
             return;
        }

        // Only enhance controller classes that are NOT annotated with @Interceptor
        if (EnhancerUtility.isAnInterceptor(ctClass)) { // "play.mvc.Controller"
			return;
		}

        if (containsSwitchSchemeAnnot(ctClass) || containsGlobalSwitchSchemeAnnot(ctClass)) {
            addWithAnnotToController(ctClass);
        } else {
            // Enhance controller if is annotated with @GlobalSwitchScheme, or
            // at least one of its actions is annotated with @SwitchScheme
            Logger.debug("%s enhanced: %s", ctClass.getName(), "NO");
            return;
        }

        // Done - Enhance Class.
		appClass.enhancedByteCode = ctClass.toBytecode();
		ctClass.defrost();
        Logger.debug("%s enhanced: %s", ctClass.getName(), "YES");
    }

    private void addGlobalSwitchSchemeAnnotToController(CtClass ctClass) throws Exception {
        // Add @GlobalSwitchScheme annotation to class

        if (containsGlobalSwitchSchemeAnnot(ctClass)) {
            // Already contains @GlobalSwitchScheme, Do Nothing...
            return;
        }

        final SchemeType DEFAULT_SCHEME_TYPE = SchemeType.HTTP;

        ConstPool constpool = ctClass.getClassFile().getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);

        // Creates an annotation @GlobalSwitchScheme
        Annotation annot = new Annotation(GlobalSwitchScheme.class.getName(), constpool);

        EnumMemberValue enumValue = new EnumMemberValue(constpool);
        enumValue.setType(SchemeType.class.getName());
		enumValue.setValue(DEFAULT_SCHEME_TYPE.name());
		annot.addMemberValue("type", enumValue);
		attr.addAnnotation(annot);

        ctClass.getClassFile().addAttribute(attr);  // Adds the attr-annotation to the Class
    }

    private void addSwitchSchemeAnnotToAllMethods(CtClass ctClass) throws Exception {

        final SchemeType DEFAULT_SCHEME_TYPE = SchemeType.HTTP;

        ConstPool constpool = ctClass.getClassFile().getConstPool();

        // Get SchemeType value
        Annotation ann = getAnnotations(ctClass).getAnnotation(GlobalSwitchScheme.class.getName());

        EnumMemberValue enumMemberValue = ann != null ? (EnumMemberValue) ann.getMemberValue("type") : null;
        String schemeType = enumMemberValue != null ? enumMemberValue.getValue() : DEFAULT_SCHEME_TYPE.name();

        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            // If method is annotated with @SwitchScheme, skip it
            if (EnhancerUtility.hasAnnotation(ctMethod, SwitchScheme.class.getName())) {
                continue;
            }

            if (EnhancerUtility.isPublicStaticVoid(ctMethod)) {
//                Logger.debug("%s is public static void", ctMethod.getName());

                // Add SwitchScheme annotation  @SwitchScheme(type = SchemeType.XXX)
                AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
                Annotation annot = new Annotation(SwitchScheme.class.getName(), constpool);
                EnumMemberValue enumValue = new EnumMemberValue(constpool);
                enumValue.setType(SchemeType.class.getName());
                enumValue.setValue(schemeType);
                annot.addMemberValue("type", enumValue); // attr value must match, in this case "type"

                attr.addAnnotation(annot);
                ctMethod.getMethodInfo().addAttribute(attr);
//                Logger.debug(">>> SchemeType: %s", schemeType);
            }
	    }
    }

    private void addWithAnnotToController(CtClass ctClass) throws Exception {

        final String THE_INTERCEPTOR = "controllers.reverseproxy.ReverseProxyInterceptor";

        ConstPool constpool = ctClass.getClassFile().getConstPool();
		AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation annot;

        // Add @With annotation to class
        annot = new Annotation(play.mvc.With.class.getName(), constpool);   // Creates an annotation @With

        // The Array that will contain the values of @With
        ArrayMemberValue arrayValue = new ArrayMemberValue(constpool);

        if (!hasAnnotation(ctClass, With.class.getName())) {    // The class is NOT annotated with @With
            // Creates the annotation @With
            ClassMemberValue classValue = new ClassMemberValue(constpool);
            classValue.setValue(THE_INTERCEPTOR);
            arrayValue.setValue(new ClassMemberValue[]{classValue});
        } else {
            // The class is annotated with @With an has an array values, e.g., @With({Deadebolt.class, Excel.class, Etc.class}) or @With(Deadbolt.class)

            Annotation ann = getAnnotations(ctClass).getAnnotation(With.class.getName());
            MemberValue[] values = ((ArrayMemberValue) ann.getMemberValue("value")).getValue();

            // Gets all values from @With and stores them into a List
            List<String> withAnnotValuesList = new ArrayList<String>();
            for (MemberValue value : values) {
                String val = value.toString().substring(1, value.toString().indexOf(' '));
                withAnnotValuesList.add(val);
            }

            // Creates new values for @With
            List<String> newWithAnnotValuesList = new ArrayList<String>();

            // NOTE: It's extremely important that ReverseProxyController is the very first element of the List
            newWithAnnotValuesList.add(THE_INTERCEPTOR);

            // Add the already existing values to the List
            newWithAnnotValuesList.addAll(withAnnotValuesList);

            // ####################################################################################################
            // @With annotation example:
            // @With({Deadebolt.class, Excel.class}) --> @With({@ReverseProxyInterceptor.class, Deadbolt.class, Excel.class})
            // @With(Deadbolt.class) --> @With({@ReverseProxyInterceptor.class, Deadbolt.class})
            // ####################################################################################################

            // Create and fill the Array with the values for @With
            ClassMemberValue[] classValueArray = new ClassMemberValue[newWithAnnotValuesList.size()];
            for (int i = 0; i < newWithAnnotValuesList.size(); i++) {
                String val = newWithAnnotValuesList.get(i);
                ClassMemberValue classValue = new ClassMemberValue(constpool);
                classValue.setValue(val);
                classValueArray[i] = classValue;
            }
            arrayValue.setValue(classValueArray);
        }

        annot.addMemberValue("value", arrayValue);
        attr.addAnnotation(annot);
        ctClass.getClassFile().addAttribute(attr);  // Adds the attr-annotation to the Class
    }

    /**
     * Check if Class is annotated with @GlobalSwitchScheme
     * @param ctClass - A CtClass obj.
     * @return - A boolean value: true if it indeed contains, false otherwise
     * @throws Exception -
     */
    private boolean containsGlobalSwitchSchemeAnnot(CtClass ctClass) throws Exception {
        return EnhancerUtility.hasAnnotation(ctClass, GlobalSwitchScheme.class.getName());
    }

    /**
     * Check if at least one of Class methods is annotated with @SwitchScheme
     * @param ctClass - A CtClass obj.
     * @return - A boolean value: true if it indeed contains, false otherwise
     * @throws Exception -
     */
    private boolean containsSwitchSchemeAnnot(CtClass ctClass) throws Exception {
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            if (EnhancerUtility.hasAnnotation(ctMethod, SwitchScheme.class.getName())) {
                return true;
            }
        }
        return false;
    }
}
