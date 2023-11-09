package com.trans.opengles.ui.bean;

public class SampleItem {

  private String name;
  private String description;
  private Class<?> clazz;

  public SampleItem(String name, String description, Class<?> clazz) {
    this.name = name;
    this.description = description;
    this.clazz = clazz;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Class getClazz() {
    return clazz;
  }

}
