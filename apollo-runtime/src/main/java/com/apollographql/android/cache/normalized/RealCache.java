package com.apollographql.android.cache.normalized;


import com.apollographql.android.impl.util.Utils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import static com.apollographql.android.impl.util.Utils.checkNotNull;

public final class RealCache implements Cache {
  private final CacheStore cacheStore;
  private final CacheKeyResolver cacheKeyResolver;
  private Map<RecordChangeSubscriber, Set<String>> subscribers;

  public RealCache(@Nonnull CacheStore cacheStore, @Nonnull CacheKeyResolver cacheKeyResolver) {
    this.cacheStore = cacheStore;
    this.cacheKeyResolver = cacheKeyResolver;
    this.subscribers = new LinkedHashMap<>();
  }

  @Override public void subscribe(RecordChangeSubscriber subscriber, Set<String> dependentKeys) {
    subscribers.put(subscriber, dependentKeys);
  }

  @Override public void unsubscribe(RecordChangeSubscriber subscriber) {
    subscribers.remove(subscriber);
  }

  @Override @Nonnull public ResponseNormalizer responseNormalizer() {
    return new ResponseNormalizer(cacheKeyResolver);
  }

  //Todo: Add proper threading model to RealCache (this should be done on different thread)
  @Override public void write(@Nonnull final Collection<Record> recordSet) {
    final Set<String> changedKeys = cacheStore.merge(checkNotNull(recordSet, "recordSet == null"));
    Map<RecordChangeSubscriber, Set<String>> iterableSubscribers = new LinkedHashMap<>(subscribers);
    for (Map.Entry<RecordChangeSubscriber, Set<String>> subscriberEntry : iterableSubscribers.entrySet()) {
      if (!Utils.areDisjoint(subscriberEntry.getValue(), changedKeys)) {
        subscriberEntry.getKey().onDependentKeysChanged();
      }
    }
  }

  @Override public Record read(@Nonnull String key) {
    return cacheStore.loadRecord(checkNotNull(key, "key == null"));
  }

  @Override public Collection<Record> read(@Nonnull Collection<String> keys) {
    return cacheStore.loadRecords(checkNotNull(keys, "keys == null"));
  }
}
