package models;

import net.parnassoft.playutilities.exceptions.UnfoundRecordException;
import models.deadbolt.RoleHolder;
import play.Logger;
import play.data.validation.Equals;
import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends UserAbs implements RoleHolder{

	/* ---TRANSIENT FIELDS--- */

	@Transient
    @Required(message = "req pass_conf")
    @Equals("password")
    public String password_confirm;

	// DO NOT add any constructor use the default one

    /* ---TRIGGER METHODS--- */

	/* ---FINDER METHODS--- */

    public static User findByUsername(String username) throws UnfoundRecordException {
        User user = find("byUsername", username).first();
        if (user == null) {
            throw new UnfoundRecordException(String.format("No hay ningun Usuario registrado con este username: %s", username));
        }
        return user;
    }

    public static User findByEmail(String email) throws UnfoundRecordException {
        User user = find("byEmail", email).first();
        if (user == null) {
            throw new UnfoundRecordException(String.format("No hay ningun Usuario registrado con este email: %s", email));
        }
        return user;
    }

    /* ---UTILITY METHODS--- */

    public void associateWithRole(String name) {
		role = Role.findByName(name);
	}

    public boolean authenticate(String password) {
        boolean result = this.password.equals(password);
        if (result) {
            Logger.debug("%s is AUTHENTIC", password);
        } else {
            Logger.debug("%s is COPYCAT", password);
        }
        return result;
    }

//    public List<? extends models.deadbolt.Role> getRoles() {
    public List<Role> getRoles() {
        List<Role> roles = new ArrayList<Role>();
        roles.add(role);    // Deadbolt requires a List of Roles
        return roles;
    }

}
