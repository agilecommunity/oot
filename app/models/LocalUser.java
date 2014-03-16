package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class LocalUser extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public String id;

    public String provider;

    public String first_name;

    public String last_name;

    public String email;

    public Boolean is_admin;

    @JsonIgnore
    public String password;

    @JsonIgnore
    public Date created_at;

    @JsonIgnore
    public Date updated_at;

    /**
     * Generic query helper for entity with id Long
     */
    public static Finder<String,LocalUser> find = new Finder<String,LocalUser>(String.class, LocalUser.class);

}
