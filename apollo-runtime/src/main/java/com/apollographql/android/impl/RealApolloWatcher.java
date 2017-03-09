package com.apollographql.android.impl;

import com.apollographql.android.ApolloCall;
import com.apollographql.android.api.graphql.Operation;
import com.apollographql.android.api.graphql.Response;
import com.apollographql.android.cache.normalized.Cache;
import com.apollographql.android.cache.normalized.CacheControl;
import com.apollographql.android.impl.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RealApolloWatcher<T extends Operation.Data> {

  private RealApolloCall<T> activeCall;
  @Nullable private ApolloCall.Callback<T> callback = null;
  private final Cache cache;
  private CacheControl refetchCacheControl = CacheControl.CACHE_FIRST;

  public RealApolloWatcher(RealApolloCall<T> originalCall, Cache cache) {
    activeCall = originalCall;
    this.cache = cache;
  }

  private Cache.RecordChangeSubscriber recordChangeSubscriber = new Cache.RecordChangeSubscriber() {
    @Override public void onDependentKeyChanged() {
      refetch();
      cache.unsubscribe(this);
    }
  };

  private ApolloCall.Callback<T> callbackProxy(final ApolloCall.Callback<T> sourceCallback,
      final RealApolloCall<T> call) {
    return new ApolloCall.Callback<T>() {
      @Override public void onResponse(@Nonnull Response<T> response) {
        sourceCallback.onResponse(response);
        cache.subscribe(recordChangeSubscriber, call.dependentKeys());
      }

      @Override public void onFailure(@Nonnull Exception e) {
        sourceCallback.onFailure(e);
      }
    };
  }

  public void stopWatching() {
    cache.unsubscribe(recordChangeSubscriber);
  }

  public void enqueueAndWatch(@Nullable final ApolloCall.Callback<T> callback) {
    this.callback = callback;
    fetch();
  }

  @Nonnull public RealApolloWatcher<T> refetchCacheControl(@Nonnull CacheControl cacheControl) {
    Utils.checkNotNull(cacheControl, "cacheControl == null");
    this.refetchCacheControl = cacheControl;
    return this;
  }

  @Nonnull public RealApolloWatcher<T> refetch() {
    activeCall.cancel(); //Todo: is this necessary / good?
    activeCall = activeCall.clone().cacheControl(refetchCacheControl);
    fetch();
    return this;
  }

  private void fetch() {
    activeCall.enqueue(callbackProxy(callback, activeCall));
  }

}