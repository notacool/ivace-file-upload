package com.fileupload.web.app.model;

import java.util.UUID;

public class EntryEntry {
  private UUID id;
  private String tag;
 
  public UUID getID() { return id; }
  public void setID(UUID value) { this.id = value; }
 
  public String getTag() { return tag; }
  public void setTag(String value) { this.tag = value; }
}
