package com.apollographql.android;

import com.apollographql.android.api.graphql.Operation;
import com.apollographql.android.cache.normalized.CacheControl;
import com.apollographql.android.impl.RealApolloWatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ApolloWatcher<T extends Operation.Data> {

  void enqueueAndWatch(@Nullable final ApolloCall.Callback<T> callback);

  void stopWatching();

  @Nonnull RealApolloWatcher<T> refetchCacheControl(@Nonnull CacheControl cacheControl);

  @Nonnull RealApolloWatcher<T> refetch();
}
