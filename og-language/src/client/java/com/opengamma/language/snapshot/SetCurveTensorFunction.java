/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.language.Value;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.CoreModelTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Modifies a yield curve to take values from the updated 1D matrix tensor.
 */
public class SetCurveTensorFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetCurveTensorFunction INSTANCE = new SetCurveTensorFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return ImmutableList.of(new MetaParameter("snapshot", CoreModelTypes.MANAGEABLE_CURVE_SNAPSHOT), new MetaParameter("overrideValue", JavaTypeInfo.builder(Value.class).arrayOf()
        .allowNull().get()), new MetaParameter("marketValue", JavaTypeInfo.builder(Value.class).arrayOf().allowNull().get()));
  }

  private SetCurveTensorFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetCurveTensor", getParameters(), this));
  }

  protected SetCurveTensorFunction() {
    this(new DefinitionAnnotater(SetCurveTensorFunction.class));
  }

  public static ManageableCurveSnapshot invoke(final ManageableCurveSnapshot snapshot, final Value[] overrideValue, final Value[] marketValue) {
    int i = 0;
    final ManageableUnstructuredMarketDataSnapshot values = snapshot.getValues();
    for (final ExternalIdBundle target : values.getTargets()) {
      final Map<String, ValueSnapshot> entries = values.getTargetValues(target);
      if (marketValue != null) {
        for (final Map.Entry<String, ValueSnapshot> entry : new ArrayList<>(entries.entrySet())) {
          final Double override;
          if (overrideValue != null) {
            if (overrideValue.length < i) {
              throw new InvokeInvalidArgumentException(1, "Vector too short");
            }
            override = overrideValue[i].getDoubleValue();
          } else {
            override = UnstructuredMarketDataSnapshotUtil.getOverrideValue(entry.getValue());
          }
          values.putValue(target, entry.getKey(), ValueSnapshot.of(marketValue[i].getDoubleValue(), override));
          i++;
        }
      } else {
        if (overrideValue != null) {
          for (final ValueSnapshot entry : entries.values()) {
            if (overrideValue.length < i) {
              throw new InvokeInvalidArgumentException(1, "Vector too short");
            }
            entry.setOverrideValue(overrideValue[i].getDoubleValue());
            i++;
          }
        }
      }
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableCurveSnapshot) parameters[0], (Value[]) parameters[1], (Value[]) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
