package com.example.rollback.entity;

import lombok.Data;

@Data
public class Order {

  private long id;
  private String uuid;
  private long stockId;
  private long status;

}
