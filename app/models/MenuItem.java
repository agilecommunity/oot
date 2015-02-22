package models;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonGetter;
import play.data.validation.Constraints;
import play.db.ebean.Model;

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
    public String shopName;

    @Constraints.MaxLength(20)
    public String registerNumber = "";

    @Constraints.MaxLength(20)
    public String itemNumber = "";

    @Constraints.Required
    @Constraints.MaxLength(255)
    public String name;

    @Constraints.Required
    public BigDecimal fixedOnOrder;

    @Constraints.Required
    public BigDecimal discountOnOrder = BigDecimal.ZERO;

    public BigDecimal fixedOnPurchaseIncTax = BigDecimal.ZERO;

    public BigDecimal fixedOnPurchaseExcTax = BigDecimal.ZERO;

    @Constraints.Required
    @Constraints.MaxLength(20)
    public String status;

    @Constraints.MaxLength(255)
    public String comment;

    @Constraints.MaxLength(255)
    public String code;

    public String itemImagePath;

    @JsonGetter
    public BigDecimal reducedOnOrder() {
        if (this.fixedOnOrder == null) {
            return BigDecimal.ZERO;
        }
        return this.fixedOnOrder.subtract(this.discountOnOrder);
    }

    /**
     * Generic query helper for entity Lunch with id Long
     */
    public static Finder<Long,MenuItem> find = new Finder<Long,MenuItem>(Long.class, MenuItem.class);

}

