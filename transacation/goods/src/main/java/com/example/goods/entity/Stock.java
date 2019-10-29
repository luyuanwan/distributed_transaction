package com.example.goods.entity;

import lombok.Data;

@Data
public class Stock {

  private long id;
  private long stock;
  private String name;
  private long status;
  private String uuid;

}
