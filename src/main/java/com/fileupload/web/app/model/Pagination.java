package com.fileupload.web.app.model;

public class Pagination {
  private long count;
  private boolean hasMoreItems;
  private long maxItems;
  private long skipCount;
  private long totalItems;
 
  public long getCount() { return count; }
  public void setCount(long value) { this.count = value; }
 
  public boolean getHasMoreItems() { return hasMoreItems; }
  public void setHasMoreItems(boolean value) { this.hasMoreItems = value; }
 
  public long getMaxItems() { return maxItems; }
  public void setMaxItems(long value) { this.maxItems = value; }
 
  public long getSkipCount() { return skipCount; }
  public void setSkipCount(long value) { this.skipCount = value; }
 
  public long getTotalItems() { return totalItems; }
  public void setTotalItems(long value) { this.totalItems = value; }
}
