package com.robin.metrics.registry;

import com.robin.metrics.calculate.*;
import com.robin.metrics.calculate.Timer;
import com.robin.metrics.utils.ThreadLocalStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Robin.li
 * @Date: 2018/7/26
 **/

public class MetricsRegistry implements Iterable<Calculator> {

    private static final Logger logger = LoggerFactory.getLogger(MetricsRegistry.class);
    private final ConcurrentMap<String, Calculator> metrics = new ConcurrentHashMap<String, Calculator>();
    private final List<MetricsDiscovery> discoveries = new LinkedList<MetricsDiscovery>();
    private static final ThreadLocalStringBuilder localCreateKeyBuffer = new ThreadLocalStringBuilder();
    private MetricsConfig config;


    protected MetricsRegistry(MetricsConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config should not be empty");
        }
        this.config = config;
    }

    public Calculator register(String namespace, String metricName, SortedMap<String, String> tags, Calculator metric) {
        if (metric == null) {
            throw new IllegalArgumentException("metric should not be empty");
        }
        String key = createKey(namespace, metricName, tags);
        Calculator old = (Calculator) this.metrics.get(key);
        if (old != null) {
            return old;
        }
        metric.setKey(key);
        old = (Calculator) this.metrics.putIfAbsent(key, metric);
        return old != null ? old : metric;
    }

    public Calculator remove(String namespace, String metricName, SortedMap<String, String> tags) {
        String key = createKey(namespace, metricName, tags);
        return (Calculator) this.metrics.remove(key);
    }

    public <T extends Calculator> T get(String namespace, String metricName, SortedMap<String, String> tags, Class<T> metricClass) {
        if (metricClass == null) {
            throw new IllegalArgumentException("metricClass should not be empty");
        }
        String key = createKey(namespace, metricName, tags);
        Calculator old = (Calculator) this.metrics.get(key);
        return old != null ? (T) metricClass.cast(old) : null;
    }

    public Timer timer(String namespace, String metricName, SortedMap<String, String> tags) {
        String key = createKey(namespace, metricName, tags);
        return (Timer) getOrAdd(key, this.timerBuilder);
    }

    public Counter counter(String namespace, String metricName, SortedMap<String, String> tags) {
        String key = createKey(namespace, metricName, tags);
        return (Counter) getOrAdd(key, this.counterBuilder);
    }

    public Counter counter(String namespace, String metricName, SortedMap<String, String> tags, boolean recordZero) {
        String key = createKey(namespace, metricName, tags);
        return (Counter) getOrAdd(key, recordZero ? this.recodeZeroDataCounterBuilder : this.counterBuilder);
    }

    public SlidingWindowCounter slidingWindowCounter(String namespace, String metricName, SortedMap<String, String> tags) {
        String key = createKey(namespace, metricName, tags);
        return (SlidingWindowCounter) getOrAdd(key, this.slidingWindowCounterBuilder);
    }

    public Gauge gauge(String namespace, String metricName, SortedMap<String, String> tags) {
        String key = createKey(namespace, metricName, tags);
        return (Gauge) getOrAdd(key, this.gaugeBuilder);
    }

    public Gauge gauge(String namespace, String metricName, SortedMap<String, String> tags, boolean isDouble) {

        String key = createKey(namespace, metricName, tags);
        return (Gauge) getOrAdd(key, isDouble ? this.gaugeForDoubleBuilder : this.gaugeBuilder);
    }

    public MetricsConfig getConfig() {
        return this.config;
    }

    public Iterator<Calculator> iterator() {
        return this.metrics.values().iterator();
    }

    public void discoverMetrics() {
        List<MetricsDiscovery> ones = null;
        synchronized (this.discoveries) {
            if (!this.discoveries.isEmpty()) {
                ones = new ArrayList<MetricsDiscovery>(this.discoveries);
            }
        }
        if ((ones != null) && (!ones.isEmpty())) {
            for (MetricsDiscovery it : ones) {
                try {
                    it.discover(this);
                } catch (Throwable t) {
                    logger.error("Fail to discover metrics.", t);
                }
            }
        }
    }

    public void registerDiscovery(MetricsDiscovery discovery) {
        if (discovery != null) {
            synchronized (this.discoveries) {
                if (!this.discoveries.contains(discovery)) {
                    this.discoveries.add(discovery);
                }
            }
        }
    }

    public void removeDiscovery(MetricsDiscovery discovery) {
        if (discovery != null) {
            synchronized (this.discoveries) {
                this.discoveries.remove(discovery);
            }
        }
    }

    @Deprecated
    public Calculator register(String namespace, String metricName, TreeMap<String, String> tags, Calculator metric) {
        return register(namespace, metricName, (SortedMap)tags, metric);
    }

    @Deprecated
    public Calculator remove(String namespace, String metricName, TreeMap<String, String> tags) {
        return this.remove(namespace, metricName, (SortedMap)tags);
    }

    @Deprecated
    public <T extends Calculator> T get(String namespace, String metricName, TreeMap<String, String> tags, Class<T> metricClass) {
        return get(namespace, metricName, tags, metricClass);
    }

    @Deprecated
    public Timer timer(String namespace, String metricName, TreeMap<String, String> tags) {
        return timer(namespace, metricName, (SortedMap<String, String>) tags);
    }

    @Deprecated
    public Counter counter(String namespace, String metricName, TreeMap<String, String> tags) {
        return counter(namespace, metricName, (SortedMap<String, String>) tags);
    }

    @Deprecated
    public Counter counter(String namespace, String metricName, TreeMap<String, String> tags, boolean recordZero) {
        return counter(namespace, metricName, (SortedMap)tags, recordZero);
    }

    @Deprecated
    public SlidingWindowCounter slidingWindowCounter(String namespace, String metricName, TreeMap<String, String> tags) {
        return slidingWindowCounter(namespace, metricName, (SortedMap)tags);
    }

    @Deprecated
    public Gauge gauge(String namespace, String metricName, TreeMap<String, String> tags) {
        return gauge(namespace, metricName, (SortedMap)tags);
    }

    @Deprecated
    public Gauge gauge(String namespace, String metricName, TreeMap<String, String> tags, boolean isDouble) {
        return gauge(namespace, metricName, (SortedMap)tags, isDouble);
    }

    private String createKey(String namespace, String metricName, SortedMap<String, String> tags) {
        if ((metricName == null) || (metricName.isEmpty())) {
            throw new IllegalArgumentException("metricName should not be empty");
        }
        if (namespace == null) {
            namespace = "";
        }
        StringBuilder sb =  (StringBuilder) localCreateKeyBuffer.get();

        sb.setLength(0);
        sb.append(namespace).append('\t').append(metricName);
        if ((tags != null) && (tags.size() > 0)) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                sb.append('\t').append((String) entry.getKey()).append(':').append((String) entry.getValue());
            }
        }
        String str = sb.toString();
        sb.setLength(0);
        return str;
    }

    @SuppressWarnings("unchecked")
    private <T extends Calculator> T getOrAdd(String key, MetricBuilder<T> builder) {
        Calculator metric = (Calculator) this.metrics.get(key);
        if (builder.isInstance(metric)) {
            return (T) metric;
        }
        if (metric == null) {
            T added = builder.newMetric(key);
            Calculator old = (Calculator) this.metrics.putIfAbsent(key, added);
            if ((old != null) && (builder.isInstance(added))) {
                return (T) old;
            }
            if (old == null) {
                return added;
            }
        }
        throw new IllegalArgumentException(key + " is already used for a different type of metric");
    }

    @SuppressWarnings("unchecked")
    private MetricBuilder<Counter> counterBuilder = new MetricBuilder() {
        public Counter newMetric(String key) {
            return new CounterImplLongAdder(key);
        }

        public boolean isInstance(Calculator calculator) {
            return Counter.class.isInstance(calculator);
        }
    };

    @SuppressWarnings("unchecked")
    private MetricBuilder<Counter> recodeZeroDataCounterBuilder = new MetricBuilder() {
        public Counter newMetric(String key) {
            Counter c = new CounterImplLongAdder(key);
            c.setRecordZeroData(true);
            return c;
        }

        public boolean isInstance(Calculator calculator) {
            return Counter.class.isInstance(calculator);
        }
    };

    @SuppressWarnings("unchecked")
    private MetricBuilder<SlidingWindowCounter> slidingWindowCounterBuilder = new MetricBuilder() {
        public SlidingWindowCounter newMetric(String key) {
            return new SlidingWindowCounter(key, MetricsRegistry.this.config.getSlidingWindowCounterSize(), MetricsRegistry.this.config.getSlidingWindowCounterPrecision(), TimeUnit.MILLISECONDS);
        }

        public boolean isInstance(Calculator calculator) {
            return SlidingWindowCounter.class.isInstance(calculator);
        }
    };


    @SuppressWarnings("unchecked")
    private MetricBuilder<Timer> timerBuilder = new MetricBuilder() {
        public Timer newMetric(String key) {
            return new TimerImplLongAdder(key);
        }

        public boolean isInstance(Calculator calculator) {
            return Timer.class.isInstance(calculator);
        }
    };

    @SuppressWarnings("unchecked")
    private MetricBuilder<Gauge> gaugeBuilder = new MetricBuilder() {
        public Gauge newMetric(String key) {
            return new Gauge(key);
        }

        public boolean isInstance(Calculator calculator) {
            return Gauge.class.isInstance(calculator);
        }
    };


    @SuppressWarnings("unchecked")
    private MetricBuilder<GaugeForDouble> gaugeForDoubleBuilder = new MetricBuilder() {
        public GaugeForDouble newMetric(String key) {
            return new GaugeForDouble(key);
        }

        public boolean isInstance(Calculator calculator) {
            return GaugeForDouble.class.isInstance(calculator);
        }
    };


    private static abstract interface MetricBuilder<T extends Calculator> {
        public abstract T newMetric(String paramString);

        public abstract boolean isInstance(Calculator paramCalculator);
    }

}
