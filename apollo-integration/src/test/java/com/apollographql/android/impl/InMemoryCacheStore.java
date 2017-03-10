package com.apollographql.android.impl;

import com.apollographql.android.cache.normalized.CacheStore;
import com.apollographql.android.cache.normalized.Record;
import com.apollographql.android.cache.normalized.RecordSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class InMemoryCacheStore extends CacheStore {

  private final RecordSet recordSet;

  public InMemoryCacheStore() {
    this.recordSet = new RecordSet();
  }

  public Record loadRecord(String key) {
    return recordSet.get(key);
  }

  public synchronized Set<String> merge(Record apolloRecord) {
    return recordSet.merge(apolloRecord);
  }

  @Override public Collection<Record> loadRecords(Collection<String> keys) {
    List<Record> records = new ArrayList<>(keys.size());
    for (String key : keys) {
      final Record record = loadRecord(key);
      if (record != null) {
        records.add(record);
      }
    }
    return records;
  }

  @Override public Set<String> merge(Collection<Record> recordSet) {
    Set<String> changedKeys = new LinkedHashSet<>();
    for (Record record: recordSet) {
      changedKeys.addAll(merge(record));
    }
    return changedKeys;
  }

  public Collection<Record> allRecords() {
    return recordSet.allRecords();
  }
}
