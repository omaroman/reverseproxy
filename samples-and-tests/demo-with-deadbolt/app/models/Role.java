package models;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * Author: Omar
 * Date: 16/05/11
 * Time: 01:07 PM
 */

@Entity
@Table(name = "roles")
public class Role extends RoleAbs implements models.deadbolt.Role {

    /* ---TRANSIENT FIELDS--- */

		// Here goes attributes not stored in the DB
		// Note: All attrs. must be annotated with
		// @Transient

    // ++++++++++++++++++++++++++++++ //

    // DO NOT add any constructor use the default one

    /* ---TRIGGER METHODS--- */

    /* ---FINDER METHODS--- */

    public static Role findByName(String name) {
        return Role.find("byName", name).first();
    }

	/* ---UTILITY METHODS--- */

    public String getRoleName() {
        return name;
    }

}
