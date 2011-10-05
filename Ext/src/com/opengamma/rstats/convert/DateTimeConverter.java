/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.rstats.convert;

import static com.opengamma.language.convert.TypeMap.MINOR_LOSS;
import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Converts dates and times to an R representation.
 * <p>
 * The following are represented as strings:
 * <ul>
 *  <li>LocalDate
 * </ul>
 * The following are represented as doubles (seconds since 1970-01-01 epoch):
 * <ul>
 *  <li>Instant
 * </ul>
 */
public class DateTimeConverter extends AbstractTypeConverter {

  private static final JavaTypeInfo<Value> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<Value> VALUE_ALLOW_NULL = JavaTypeInfo.builder(Value.class).allowNull().get();
  private static final JavaTypeInfo<Instant> INSTANT = JavaTypeInfo.builder(Instant.class).get();
  private static final JavaTypeInfo<Instant> INSTANT_ALLOW_NULL = JavaTypeInfo.builder(Instant.class).allowNull().get();
  private static final JavaTypeInfo<LocalDate> LOCAL_DATE = JavaTypeInfo.builder(LocalDate.class).get();
  private static final JavaTypeInfo<LocalDate> LOCAL_DATE_ALLOW_NULL = JavaTypeInfo.builder(LocalDate.class).allowNull().get();

  private static final TypeMap TO_VALUE = TypeMap.of(ZERO_LOSS, INSTANT, LOCAL_DATE);
  private static final TypeMap TO_VALUE_ALLOW_NULL = TypeMap.of(ZERO_LOSS, INSTANT_ALLOW_NULL, LOCAL_DATE_ALLOW_NULL);
  private static final TypeMap FROM_VALUE = TypeMap.of(MINOR_LOSS, VALUE);
  private static final TypeMap FROM_VALUE_ALLOW_NULL = TypeMap.of(MINOR_LOSS, VALUE_ALLOW_NULL);

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return true;
    } else {
      for (JavaTypeInfo<?> toData : (targetType.isAllowNull() ? TO_VALUE_ALLOW_NULL : TO_VALUE).keySet()) {
        if (clazz == toData.getRawClass()) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if ((value == null) && type.isAllowNull()) {
      conversionContext.setResult(null);
      return;
    }
    final Class<?> clazz = type.getRawClass();
    if (clazz == Value.class) {
      if (value instanceof LocalDate) {
        conversionContext.setResult(ValueUtils.of(((LocalDate) value).toString()));
      } else if (value instanceof Instant) {
        conversionContext.setResult(ValueUtils.of((double) ((Instant) value).toEpochMillisLong() / 1000d));
      } else {
        conversionContext.setFail();
      }
    } else if (clazz == LocalDate.class) {
      final String str = ((Value) value).getStringValue();
      if (str != null) {
        conversionContext.setResult(LocalDate.parse(str));
      } else {
        conversionContext.setFail();
      }
    } else if (clazz == Instant.class) {
      final Double epochSeconds = ((Value) value).getDoubleValue();
      if (epochSeconds != null) {
        conversionContext.setResult(Instant.ofEpochSeconds(epochSeconds.longValue()));
      } else {
        conversionContext.setFail();
      }
    } else {
      conversionContext.setFail();
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return targetType.isAllowNull() ? TO_VALUE_ALLOW_NULL : TO_VALUE;
    } else {
      return targetType.isAllowNull() ? FROM_VALUE_ALLOW_NULL : FROM_VALUE;
    }
  }

}
