package com.fileupload.web.app.model;

public class ListClass {
  private EntryElement[] entries;
  private Pagination pagination;
 
  public EntryElement[] getEntries() { return entries; }
  public void setEntries(EntryElement[] value) { this.entries = value; }
 
  public Pagination getPagination() { return pagination; }
  public void setPagination(Pagination value) { this.pagination = value; }
}
