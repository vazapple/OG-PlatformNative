/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.List;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions.Builder;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Object describing all of the parameters required to construct a view client - i.e. the view and execution options.
 */
public final class ViewClientDescriptor {

  /**
   * Default value of samplePeriod.
   */
  public static final int DEFAULT_SAMPLE_PERIOD = 86400;

  public static JavaTypeInfo<ViewClientDescriptor> TYPE = JavaTypeInfo.builder(ViewClientDescriptor.class).get();

  private final UniqueId _viewId;
  private final ViewExecutionOptions _executionOptions;

  public ViewClientDescriptor(final UniqueId viewId, final ViewExecutionOptions executionOptions) {
    _viewId = viewId;
    _executionOptions = executionOptions;
  }

  public UniqueId getViewId() {
    return _viewId;
  }

  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  @Override
  public String toString() {
    return "ViewClientDescriptor[" + _viewId + "]";
  }

  /**
   * Returns a descriptor for ticking live market data. The view will recalculate when market data changes and obey time thresholds on the view definition.
   * 
   * @param viewId unique identifier of the view, not null
   * @param dataSource the data source, null for the default
   * @return the descriptor
   * @deprecated use version the takes valuation time and portfolio VC and pass nulls
   */
  @Deprecated
  public static ViewClientDescriptor tickingMarketData(final UniqueId viewId, final String dataSource) {
    return tickingMarketData(viewId, dataSource, null, null);
  }

  /**
   * Returns a descriptor for ticking live market data. The view will recalculate when market data changes and obey time thresholds on the view definition.
   * 
   * @param viewId unique identifier of the view, not null
   * @param dataSource the data source, null for the default
   * @return the descriptor
   */
  public static ViewClientDescriptor tickingMarketData(final UniqueId viewId, final String dataSource, Instant valuationTime, VersionCorrection portfolioVersionCorrection) {
    final MarketDataSpecification marketDataSpec;
    if (dataSource == null) {
      marketDataSpec = MarketData.live();
    } else {
      marketDataSpec = MarketData.live(dataSource);
    }
    Builder cycleOptions = ViewCycleExecutionOptions.builder();
    if (valuationTime != null) {
      cycleOptions.setValuationTime(valuationTime);
    }
    if (portfolioVersionCorrection != null) {
      cycleOptions.setResolverVersionCorrection(portfolioVersionCorrection);
    }
    cycleOptions.setMarketDataSpecification(marketDataSpec);
    return new ViewClientDescriptor(viewId, ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(), cycleOptions.create(), ExecutionFlags.triggersEnabled().get()));
  }

  /**
   * Returns a descriptor for ticking live market data. The view will recalculate when market data changes and obey time thresholds on the view definition.
   * 
   * @param viewId unique identifier of the view, not null
   * @param marketDataSpecifications a list of the market data specifications, not null
   * @param valuationTime TODO
   * @return the descriptor
   */
  public static ViewClientDescriptor tickingMarketData(final UniqueId viewId, final List<MarketDataSpecification> marketDataSpecifications, Instant valuationTime,
      VersionCorrection portfolioVersionCorrection) {
    Builder executionOptions = ViewCycleExecutionOptions.builder().setMarketDataSpecifications(marketDataSpecifications);
    if (valuationTime != null) {
      executionOptions = executionOptions.setValuationTime(valuationTime);
    }
    if (portfolioVersionCorrection != null) {
      executionOptions = executionOptions.setResolverVersionCorrection(portfolioVersionCorrection);
    }
    return new ViewClientDescriptor(viewId, ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(), executionOptions.create(), ExecutionFlags.triggersEnabled().get()));
  }

  /**
   * Returns a descriptor for a static live market data view. The view will recalculate when manually triggered and will not start until the first trigger is received (allowing injected overrides to
   * be set up before the first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param dataSource the data source, null for the default
   * @return the descriptor
   */
  public static ViewClientDescriptor staticMarketData(final UniqueId viewId, final String dataSource) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final ViewCycleExecutionOptions.Builder cycleOptions = ViewCycleExecutionOptions.builder();
    cycleOptions.setMarketDataSpecification((dataSource == null) ? MarketData.live() : MarketData.live(dataSource));
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions.create(), ExecutionFlags.none().waitForInitialTrigger().awaitMarketData().get());
    return new ViewClientDescriptor(viewId, options);
  }

  /**
   * Returns a descriptor for a static live market data view. The view will recalculate when manually triggered and will not start until the first trigger is received (allowing injected overrides to
   * be set up before the first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param marketDataSpecifications a list of the market data specifications, not null
   * @return the descriptor
   */
  public static ViewClientDescriptor staticMarketData(final UniqueId viewId, final List<MarketDataSpecification> marketDataSpecifications) {
    return staticMarketData(viewId, marketDataSpecifications, null, null);
  }

  /**
   * Returns a descriptor for a static live market data view. The view will recalculate when manually triggered and will not start until the first trigger is received (allowing injected overrides to
   * be set up before the first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param marketDataSpecifications a list of the market data specifications, not null
   * @return the descriptor
   */
  public static ViewClientDescriptor staticMarketData(final UniqueId viewId, final List<MarketDataSpecification> marketDataSpecifications, Instant valuationTime,
      VersionCorrection portfolioVersionCorrection) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final ViewCycleExecutionOptions.Builder cycleOptions = ViewCycleExecutionOptions.builder();
    cycleOptions.setMarketDataSpecifications(marketDataSpecifications);
    if (valuationTime != null) {
      cycleOptions.setValuationTime(valuationTime);
    }
    if (portfolioVersionCorrection != null) {
      cycleOptions.setResolverVersionCorrection(portfolioVersionCorrection);
    }
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions.create(), ExecutionFlags.none().waitForInitialTrigger().awaitMarketData().get());
    return new ViewClientDescriptor(viewId, options);
  }

  /**
   * Returns a descriptor for a view that can be manually iterated over a historical data sample. Cycles will only trigger manually and will not start until the first trigger is received (allowing
   * injected market data to be set up before the first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param firstValuationTime first valuation time of the sample, not null
   * @param lastValuationTime last valuation time in the sample, not null
   * @param samplePeriod period between each samples in seconds, e.g. 86400 for a day
   * @param timeSeriesResolverKey resolution key for the time series provider, use null for a default
   * @param timeSeriesFieldResolverKey resolution key for the time series provider, use null for a default
   * @return the descriptor
   */
  public static ViewClientDescriptor historicalMarketData(final UniqueId viewId, final Instant firstValuationTime, final Instant lastValuationTime, final int samplePeriod,
      final String timeSeriesResolverKey, final String timeSeriesFieldResolverKey) {
    ViewCycleExecutionSequence executionSequence = HistoricalExecutionSequenceFunction.generate(firstValuationTime, lastValuationTime, samplePeriod, timeSeriesResolverKey,
        timeSeriesFieldResolverKey);
    final ViewExecutionOptions options = ExecutionOptions.of(executionSequence, null, ExecutionFlags.none().waitForInitialTrigger().get());
    return new ViewClientDescriptor(viewId, options);
  }

  /**
   * Returns a descriptor for a view that can be manually iterated over a historical data sample. Cycles will only trigger manually and will not start until the first trigger is received (allowing
   * injected market data to be set up before the first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param firstValuationTime first valuation time of the sample, not null
   * @param lastValuationTime last valuation time in the sample, not null
   * @param samplePeriod period between each samples in seconds, e.g. 86400 for a day
   * @return the descriptor
   */
  public static ViewClientDescriptor historicalMarketData(final UniqueId viewId, final Instant firstValuationTime, final Instant lastValuationTime, final int samplePeriod) {
    return historicalMarketData(viewId, firstValuationTime, lastValuationTime, samplePeriod, null, null);
  }

  /**
   * Returns a descriptor for a view that can be manually iterated over a historical data sample. Cycles will only trigger manually and will not start until the first trigger is received (allowing
   * injected market data to be set up before the first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param firstValuationTime first valuation time of the sample, not null
   * @param lastValuationTime last valuation time in the sample, not null
   * @param timeSeriesResolverKey resolution key for the time series provider, use null for a default
   * @param timeSeriesFieldResolverKey resolution key for the time series provider, use null for a default
   * @return the descriptor
   */
  public static ViewClientDescriptor historicalMarketData(final UniqueId viewId, final Instant firstValuationTime, final Instant lastValuationTime, final String timeSeriesResolverKey,
      final String timeSeriesFieldResolverKey) {
    return historicalMarketData(viewId, firstValuationTime, lastValuationTime, DEFAULT_SAMPLE_PERIOD, timeSeriesResolverKey, timeSeriesFieldResolverKey);
  }

  /**
   * Returns a descriptor for a view that can be manually iterated over a historical data sample. Cycles will only trigger manually and will not start until the first trigger is received (allowing
   * injected market data to be set up before the first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param firstValuationTime first valuation time of the sample, not null
   * @param lastValuationTime last valuation time in the sample, not null
   * @return the descriptor
   */
  public static ViewClientDescriptor historicalMarketData(final UniqueId viewId, final Instant firstValuationTime, final Instant lastValuationTime) {
    return historicalMarketData(viewId, firstValuationTime, lastValuationTime, DEFAULT_SAMPLE_PERIOD, null, null);
  }

  /**
   * Returns a descriptor for a view that will use market data from a snapshot. Cycles will be triggered when the snapshot changes (if an unversioned identifier is supplied) or manually.
   * 
   * @param viewId unique identifier of the view, not null
   * @param snapshotId unique identifier of the market data snapshot, not null
   * @param valuationTime valuation time to use, null for current time
   * @return the descriptor
   * @deprecated use call with portfolio version correction parameter and pass null
   */
  @Deprecated
  public static ViewClientDescriptor tickingSnapshot(final UniqueId viewId, final UniqueId snapshotId, final Instant valuationTime) {
    return tickingSnapshot(viewId, snapshotId, valuationTime, null);
  }

  /**
   * Returns a descriptor for a view that will use market data from a snapshot. Cycles will be triggered when the snapshot changes (if an unversioned identifier is supplied) or manually.
   * 
   * @param viewId unique identifier of the view, not null
   * @param snapshotId unique identifier of the market data snapshot, not null
   * @param valuationTime valuation time to use, null for current time
   * @param portfolioVersionCorrection the version correction to use when resolving the portfolio, null for latest portfolio
   * @return the descriptor
   */
  public static ViewClientDescriptor tickingSnapshot(final UniqueId viewId, final UniqueId snapshotId, final Instant valuationTime, final VersionCorrection portfolioVersionCorrection) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final Builder cycleOptions = ViewCycleExecutionOptions.builder().setMarketDataSpecification(MarketData.user(snapshotId));
    if (valuationTime != null) {
      cycleOptions.setValuationTime(valuationTime);
    }
    if (portfolioVersionCorrection != null) {
      cycleOptions.setResolverVersionCorrection(portfolioVersionCorrection);
    }
    final ExecutionFlags flags = ExecutionFlags.none().triggerOnMarketData();
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions.create(), flags.get());
    return new ViewClientDescriptor(viewId, options);
  }

  /**
   * Returns a descriptor for a view that will use market data from a snapshot. The view will recalculate when triggered manually and will not start until the first trigger is received (allowing
   * injected overrides to be set up before the first). E.g. if created with an unversioned snapshot identifier, changes to the snapshot will not trigger a cycle but will be observed (the latest
   * version will be taken) when a cycle is manually triggered.
   * 
   * @param viewId unique identifier of the view, not null
   * @param snapshotId unique identifier of the market data snapshot, not null
   * @param valuationTime valuation time to use, null for current time
   * @return the descriptor
   * @deprecated use version that takes portfolioVersionCorrection and pass null
   */
  @Deprecated
  public static ViewClientDescriptor staticSnapshot(final UniqueId viewId, final UniqueId snapshotId, final Instant valuationTime) {
    return staticSnapshot(viewId, snapshotId, valuationTime, null);
  }

  /**
   * Returns a descriptor for a view that will use market data from a snapshot. The view will recalculate when triggered manually and will not start until the first trigger is received (allowing
   * injected overrides to be set up before the first). E.g. if created with an unversioned snapshot identifier, changes to the snapshot will not trigger a cycle but will be observed (the latest
   * version will be taken) when a cycle is manually triggered.
   * 
   * @param viewId unique identifier of the view, not null
   * @param snapshotId unique identifier of the market data snapshot, not null
   * @param valuationTime valuation time to use, null for current time
   * @param portfolioVersionCorrection the version correction to use when resolving the portfolio, or null to use latest portfolio
   * @return the descriptor
   */
  public static ViewClientDescriptor staticSnapshot(final UniqueId viewId, final UniqueId snapshotId, final Instant valuationTime, final VersionCorrection portfolioVersionCorrection) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final Builder cycleOptions = ViewCycleExecutionOptions.builder().setMarketDataSpecification(MarketData.user(snapshotId));
    if (valuationTime != null) {
      cycleOptions.setValuationTime(valuationTime);
    }
    if (portfolioVersionCorrection != null) {
      cycleOptions.setResolverVersionCorrection(portfolioVersionCorrection);
    }
    final ExecutionFlags flags = ExecutionFlags.none().waitForInitialTrigger();
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions.create(), flags.get());
    return new ViewClientDescriptor(viewId, options);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ViewClientDescriptor)) {
      return false;
    }
    final ViewClientDescriptor other = (ViewClientDescriptor) o;
    return getViewId().equals(other.getViewId()) && getExecutionOptions().equals(other.getExecutionOptions());
  }

  @Override
  public int hashCode() {
    return getViewId().hashCode() * 17 + getExecutionOptions().hashCode();
  }

  private static final String VIEW_ID_FIELD = "viewId";
  private static final String EXECUTION_OPTIONS_FIELD = "executionOptions";

  /**
   * Produces the Fudge encoding of the View Client Descriptor:
   * 
   * <pre>
   * message ViewClientDescriptor {
   *   required string viewId;
   *   required ViewExecutionOptions executionOptions;
   * }
   * </pre>
   * 
   * @param fudgeSerializer the Fudge serializer service
   * @return the Fudge encoding
   */
  public FudgeMsg toFudgeMsg(final FudgeSerializer fudgeSerializer) {
    final MutableFudgeMsg msg = fudgeSerializer.newMessage();
    msg.add(VIEW_ID_FIELD, getViewId().toString());
    fudgeSerializer.addToMessage(msg, EXECUTION_OPTIONS_FIELD, null, getExecutionOptions());
    return msg;
  }

  public static ViewClientDescriptor fromFudgeMsg(final FudgeDeserializer fudgeDeserializer, final FudgeMsg msg) {
    final UniqueId viewId = UniqueId.parse(msg.getString(VIEW_ID_FIELD));
    final ViewExecutionOptions executionOptions = fudgeDeserializer.fieldValueToObject(ExecutionOptions.class, msg.getByName(EXECUTION_OPTIONS_FIELD));
    return new ViewClientDescriptor(viewId, executionOptions);
  }

}
