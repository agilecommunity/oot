package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import models.annotations.JodaTimestamp;
import org.joda.time.DateTime;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.json.JodaTimestampOperator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
public class GatheringSetting extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Constraints.Required
    public Boolean enabled;

    public Integer minOrders = 0;

    public BigDecimal discountPrice = BigDecimal.ZERO;

    // CreatedTimestampは使っているEbeanのバージョンが対応していないので使えない
    @JodaTimestamp
    @JsonSerialize(using= JodaTimestampOperator.JodaTimestampSerializer.class)
    @JsonDeserialize(using=JodaTimestampOperator.JodaTimestampDeserializer.class)
    public DateTime createdAt;

    // UpdatedTimestampは使っているEbeanのバージョンが対応していないので使えない
    @JodaTimestamp
    @JsonSerialize(using= JodaTimestampOperator.JodaTimestampSerializer.class)
    @JsonDeserialize(using=JodaTimestampOperator.JodaTimestampDeserializer.class)
    public DateTime updatedAt;

    public String createdBy;

    public String updatedBy;

    /**
     * Generic query helper for entity Lunch with id Long
     */
    public static Finder<Long,GatheringSetting> find = new Finder<Long,GatheringSetting>(Long.class, GatheringSetting.class);

}
