package authorization;

import config.DynamicAuthorization;
import controllers.deadbolt.RestrictedResourcesHandler;
import models.Role;
import models.User;
import models.deadbolt.AccessResult;
import net.parnassoft.playutilities.SessionUtility;
import net.parnassoft.playutilities.exceptions.UnfoundRecordException;
import org.apache.commons.beanutils.PropertyUtils;
import play.modules.spring.Spring;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class DynamicAccess implements RestrictedResourcesHandler {

    // Get the current signin user
    private String user_email = (String) SessionUtility.get("user_email");
    private String username = (String) SessionUtility.get("username");
    private User user;

    // Get the Spring list of allowed roles for each RestrictedResource
    private DynamicAuthorization dynamicAuthorization = Spring.getBeanOfType(DynamicAuthorization.class);

    public AccessResult checkAccess(List<String> resourceName) {
        // Verifying if exist any user with that user_email
        try {
            user = User.findByEmail(user_email);
        } catch (UnfoundRecordException e) {
            try {
                user = User.findByUsername(username);
            } catch (UnfoundRecordException e1) {
                // DO Nothing...
            }
        }
        // Getting of current user its roles
        List<Role> roles = user.getRoles();

        String resource = resourceName.get(0);

        // if the user don't have any role the access is DENIED
        if (roles != null) {
            // In the case of user be a superadmin - ACCESS FULL
            if (roles.contains(Role.findByName("GOD"))) {
                return AccessResult.ALLOWED;
            } else {
                // In other case, the user have a check of roles and its access for each resource
                return verifyRoles(resource, roles);
            }
        }
        return AccessResult.DENIED;
    }

    // Method to compare the roles of USER against the roles ALLOWED for EACH RESOURCE
    public AccessResult verifyRoles(String resourceName, List<Role> roles) {

        List<Role> mappedRoles = new ArrayList<Role>();

        // Using apache BeanUtils to invoke method get for the current resource list of roles allowed
        try {
            // Get a List of Roles by Map key
            mappedRoles = (List<Role>) PropertyUtils.getMappedProperty(dynamicAuthorization, "resourceAuthorizations", resourceName);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Checking if the user have access to the resource required
        for (Role role : roles) {
            if (mappedRoles.contains(role)) {
//                Logger.debug("%s HAVE PERMISSION", role);
                return AccessResult.ALLOWED;
            }
        }
//        Logger.debug("%s DOESN'T HAVE PERMISSION", role);
        return AccessResult.DENIED;
    }
}