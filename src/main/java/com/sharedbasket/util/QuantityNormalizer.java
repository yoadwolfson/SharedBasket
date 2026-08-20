package com.sharedbasket.util;

import com.sharedbasket.domain.ItemUnit;
import com.sharedbasket.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class QuantityNormalizer {

    private static final BigDecimal MAX_QUANTITY = new BigDecimal("9999");

    private QuantityNormalizer() {
    }

    public static BigDecimal normalize(BigDecimal quantity, ItemUnit unit) {
        ItemUnit resolved = unit == null ? ItemUnit.PIECE : unit;
        if (quantity == null) {
            return resolved.getMinQuantity().setScale(resolved.getScale(), RoundingMode.HALF_UP);
        }
        RoundingMode rounding = resolved == ItemUnit.PIECE ? RoundingMode.CEILING : RoundingMode.HALF_UP;
        BigDecimal scaled = quantity.setScale(resolved.getScale(), rounding);
        BigDecimal floor = resolved.getMinQuantity().setScale(resolved.getScale(), RoundingMode.HALF_UP);
        return scaled.compareTo(floor) < 0 ? floor : scaled;
    }

    public static void validate(BigDecimal quantity, ItemUnit unit) {
        ItemUnit resolved = unit == null ? ItemUnit.PIECE : unit;
        if (quantity == null || quantity.signum() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        if (quantity.compareTo(MAX_QUANTITY) > 0) {
            throw new BadRequestException("Quantity must be at most 9999");
        }
        if (quantity.compareTo(resolved.getMinQuantity()) < 0) {
            throw new BadRequestException("Quantity must be at least " + resolved.getMinQuantity().toPlainString()
                + " for unit " + resolved.getShortLabel());
        }
    }
}
