/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.CoreModelTypes;
import com.opengamma.language.definition.types.OpenGammaTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates a "global value" component of a snapshot
 */
public class SetSnapshotGlobalValueFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetSnapshotGlobalValueFunction INSTANCE = new SetSnapshotGlobalValueFunction();

  private final MetaFunction _meta;

  private static final int SNAPSHOT = 0;
  private static final int VALUE_NAME = 1;
  private static final int IDENTIFIER = 2;
  private static final int OVERRIDE_VALUE = 3;
  private static final int MARKET_VALUE = 4;

  private static List<MetaParameter> parameters() {
    return ImmutableList.of(new MetaParameter("snapshot", CoreModelTypes.MANAGEABLE_MARKET_DATA_SNAPSHOT), new MetaParameter("valueName", PrimitiveTypes.STRING), new MetaParameter(
        "identifier", OpenGammaTypes.EXTERNAL_ID), new MetaParameter("overrideValue", PrimitiveTypes.DOUBLE_ALLOW_NULL), new MetaParameter("marketValue", PrimitiveTypes.DOUBLE_ALLOW_NULL));
  }

  private SetSnapshotGlobalValueFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetSnapshotGlobalValue", getParameters(), this));
  }

  protected SetSnapshotGlobalValueFunction() {
    this(new DefinitionAnnotater(SetSnapshotGlobalValueFunction.class));
  }

  public static ManageableMarketDataSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String valueName, final ExternalId identifier, final Double overrideValue,
      final Double marketValue) {
    if (snapshot.getGlobalValues() == null) {
      snapshot.setGlobalValues(new ManageableUnstructuredMarketDataSnapshot());
    }
    UnstructuredMarketDataSnapshotUtil.setValue(snapshot.getGlobalValues(), valueName, identifier, overrideValue, marketValue);
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableMarketDataSnapshot) parameters[SNAPSHOT], (String) parameters[VALUE_NAME], (ExternalId) parameters[IDENTIFIER], (Double) parameters[OVERRIDE_VALUE],
        (Double) parameters[MARKET_VALUE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
