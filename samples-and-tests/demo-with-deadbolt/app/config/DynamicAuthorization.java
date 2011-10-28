/**
 * Author: Omar
 * Date: 8/04/11
 * Time: 12:33 PM
 */

package config;

import models.Role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is for having a List of Roles in order to manage Authorizations to several Controllers
 * The attributes are injected via Spring
 */
public class DynamicAuthorization {

//    private List<Role> controllers_Purchases_do;

//    private List<Role> controllers_admin_Admin_do;

//    private List<Role> controllers_admin_Clients_do;

    private Map<String, List<Role>> resourceAuthorizations = new HashMap<String, List<Role>>();

//    public List<Role> getControllers_Purchases_do() {
//        return controllers_Purchases_do;
//    }
//
//    public void setControllers_Purchases_do(List<Role> controllers_Purchases_do) {
//        this.controllers_Purchases_do = controllers_Purchases_do;
//    }
//
//    public List<Role> getControllers_admin_Admin_do() {
//        return controllers_admin_Admin_do;
//    }
//
//    public void setControllers_admin_Admin_do(List<Role> controllers_admin_Admin_do) {
//        this.controllers_admin_Admin_do = controllers_admin_Admin_do;
//    }
//
//    public List<Role> getControllers_admin_Clients_do() {
//        return controllers_admin_Clients_do;
//    }
//
//    public void setControllers_admin_Clients_do(List<Role> controllers_admin_Clients_do) {
//        this.controllers_admin_Clients_do = controllers_admin_Clients_do;
//    }

    public Map<String, List<Role>> getResourceAuthorizations() {
        return resourceAuthorizations;
    }

    public void setResourceAuthorizations(Map<String, List<Role>> resourceAuthorizations) {
        this.resourceAuthorizations = resourceAuthorizations;
    }
}