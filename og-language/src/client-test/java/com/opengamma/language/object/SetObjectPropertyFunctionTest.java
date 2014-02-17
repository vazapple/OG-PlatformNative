/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.UnmodifiableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.beans.ImmutableBean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.financial.legalentity.LegalEntitySector;
import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.FudgeTypeConverter;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link SetObjectPropertyFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class SetObjectPropertyFunctionTest {

  public void testFlexiBean() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final FlexiBean bean = new FlexiBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "foo", DataUtils.of("XYZ"), null);
    assertEquals(((FlexiBean) result).get("foo"), "XYZ");
    result = SetObjectPropertyFunction.invoke(context, beanMsg, "bar", DataUtils.of(42), null);
    assertEquals(((FlexiBean) result).get("bar"), 42);
    result = SetObjectPropertyFunction.invoke(context, beanMsg, "bar", DataUtils.of(42), "float");
    assertEquals(((FlexiBean) result).get("bar"), (float) 42);
    result = SetObjectPropertyFunction.invoke(context, beanMsg, "cow", new Data(), null);
    assertEquals(((FlexiBean) result).get("cow"), null);
  }

  public void testImmutableBean() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final CurrencyAmount bean = CurrencyAmount.of(Currency.GBP, 42d);
    assertTrue(bean instanceof ImmutableBean);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "currency", DataUtils.of("USD"), null);
    assertEquals(((CurrencyAmount) result).getCurrency(), Currency.USD);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidBeanField() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final SimpleExchange bean = new SimpleExchange();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    SetObjectPropertyFunction.invoke(context, beanMsg, "nonExistentField", DataUtils.of("Test"), null);
  }

  public void testMutableBean() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final SimpleExchange bean = new SimpleExchange();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "name", DataUtils.of("Test"), null);
    assertEquals(((SimpleExchange) result).getName(), "Test");
  }

  public void testNonBean() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "x", DataUtils.of(42), null);
    assertEquals(((ObjectFunctionTest.NonJodaBean) result).getX(), 42);
    result = SetObjectPropertyFunction.invoke(context, beanMsg, "x", DataUtils.of(42d), "int");
    assertEquals(((ObjectFunctionTest.NonJodaBean) result).getX(), 42);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidNonBeanField1() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    SetObjectPropertyFunction.invoke(context, beanMsg, "y", DataUtils.of(42), null);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidNonBeanField2() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    SetObjectPropertyFunction.invoke(context, beanMsg, "class", DataUtils.of(SimpleExchange.class.getName()), null);
  }

  public void testNonObject_mutableMessage() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    bean.setX(42);
    final FudgeMsg beanMsg = (new FudgeSerializer(FudgeTypeConverter.getFudgeContext(context.getGlobalContext()))).objectToFudgeMsg(bean);
    // Check we've got no type information or hints
    assertEquals(beanMsg.toString(), "FudgeMsg[x => 42]");
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "y", DataUtils.of("ABC"), null);
    assertEquals(((FudgeMsg) result).getInt("x"), (Integer) 42);
    assertEquals(((FudgeMsg) result).getString("y"), "ABC");
  }

  public void testNonObject_immutableMessage() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    bean.setX(42);
    final FudgeContext fudgeContext = FudgeTypeConverter.getFudgeContext(context.getGlobalContext());
    final FudgeMsg beanMsg = new UnmodifiableFudgeMsg(fudgeContext, (new FudgeSerializer(fudgeContext)).objectToFudgeMsg(bean));
    // Check we've got no type information or hints
    assertEquals(beanMsg.toString(), "FudgeMsg[x => 42]");
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "y", DataUtils.of("ABC"), null);
    assertEquals(((FudgeMsg) result).getInt("x"), (Integer) 42);
    assertEquals(((FudgeMsg) result).getString("y"), "ABC");
  }

  public void testList() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final CurveConstructionConfiguration bean = new CurveConstructionConfiguration("Test", ImmutableList.<CurveGroupConfiguration>of(), ImmutableList.of("Foo", "Bar"));
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    // Standard list
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "exogenousConfigurations", DataUtils.of(new Value[] {ValueUtils.of("A"), ValueUtils.of("B"), ValueUtils.of("C") }),
        null);
    assertEquals(((CurveConstructionConfiguration) result).getExogenousConfigurations(), ImmutableList.of("A", "B", "C"));
    // Singleton list (inferred from a range)
    result = SetObjectPropertyFunction.invoke(context, beanMsg, "exogenousConfigurations", DataUtils.of(ValueUtils.of("X")), null);
    assertEquals(((CurveConstructionConfiguration) result).getExogenousConfigurations(), ImmutableList.of("X"));
    // Empty list (inferred from null) - which doesn't change the original object
    result = SetObjectPropertyFunction.invoke(context, beanMsg, "exogenousConfigurations", new Data(), null);
    assertEquals(((CurveConstructionConfiguration) result).getExogenousConfigurations(), ImmutableList.of("Foo", "Bar"));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public void testIssuerCurveTypeConfiguration() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    // Bean has filters set, so keys get correctly typed
    IssuerCurveTypeConfiguration bean = new IssuerCurveTypeConfiguration(ImmutableSet.<Object>of(),
        ImmutableSet.<LegalEntityFilter<LegalEntity>>of((LegalEntityFilter) new LegalEntityRegion(false, false, Collections.<Country>emptySet(), true, Collections.singleton(Currency.GBP))));
    FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    IssuerCurveTypeConfiguration result = (IssuerCurveTypeConfiguration) SetObjectPropertyFunction.invoke(context, beanMsg, "keys", DataUtils.of("GBP"), null);
    assertEquals(result.getKeys(), ImmutableSet.of(Currency.GBP));
    // Bean has insufficient filters, so key type can't be inferred
    bean = new IssuerCurveTypeConfiguration(ImmutableSet.<Object>of(), ImmutableSet.<LegalEntityFilter<LegalEntity>>of((LegalEntityFilter) new LegalEntitySector(false, false, ImmutableSet
        .of("Foo"))));
    beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    result = (IssuerCurveTypeConfiguration) SetObjectPropertyFunction.invoke(context, beanMsg, "keys", DataUtils.of("GBP"), null);
    assertEquals(result.getKeys(), ImmutableSet.of(ValueUtils.of("GBP")));
    // Key type can be explicitly set
    result = (IssuerCurveTypeConfiguration) SetObjectPropertyFunction.invoke(context, beanMsg, "keys", DataUtils.of("GBP"), "java.util.Set<com.opengamma.util.money.Currency>");
    assertEquals(result.getKeys(), ImmutableSet.of(Currency.GBP));
  }

}