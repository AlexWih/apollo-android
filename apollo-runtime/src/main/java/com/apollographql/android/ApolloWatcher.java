package com.apollographql.android;

import com.apollographql.android.api.graphql.Operation;
import com.apollographql.android.cache.normalized.CacheControl;
import com.apollographql.android.impl.RealApolloWatcher;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ApolloWatcher<T extends Operation.Data> {

  interface WatcherSubscription {
    void unsubscribe();
  }

  @Nonnull @CheckReturnValue WatcherSubscription enqueueAndWatch(@Nullable final ApolloCall.Callback<T> callback);

  @Nonnull RealApolloWatcher<T> refetchCacheControl(@Nonnull CacheControl cacheControl);

  @Nonnull RealApolloWatcher<T> refetch();
}
