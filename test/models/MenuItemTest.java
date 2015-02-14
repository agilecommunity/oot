package models;

import org.junit.Test;

import java.math.BigDecimal;

import static org.fest.assertions.Assertions.assertThat;

public class MenuItemTest {

    @Test
    public void reducedOnOrderは値引き後の金額を返すこと() {
        MenuItem item = new MenuItem();
        assertThat(item.reducedOnOrder()).isEqualTo(BigDecimal.valueOf(0L));

        item.fixedOnOrder = BigDecimal.valueOf(100L);
        item.discountOnOrder = BigDecimal.valueOf(5L);

        assertThat(item.reducedOnOrder()).isEqualTo(BigDecimal.valueOf(95L));
    }
}
