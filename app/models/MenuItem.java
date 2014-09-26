package models;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.data.validation.Constraints;
import play.db.ebean.Model;

/**
 * Lunch entity managed by Ebean
 */
@Entity
public class MenuItem extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Constraints.Required
    @Constraints.MaxLength(10)
    public String category;

    @Constraints.Required
    @Constraints.MaxLength(255)
    public String shop_name;

    @Constraints.Required
    @Constraints.MaxLength(255)
    public String name;

    @Constraints.Required
    public BigDecimal price_on_order;

    @Constraints.MaxLength(255)
    public String code;

    public String item_image_path;

    /**
     * Generic query helper for entity Lunch with id Long
     */
    public static Finder<Long,MenuItem> find = new Finder<Long,MenuItem>(Long.class, MenuItem.class);

}

