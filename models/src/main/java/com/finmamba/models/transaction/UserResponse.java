package com.finmamba.models.transaction;

import lombok.Data;

@Data
public class UserResponse {

  private Integer id;
  private String username;
  private String email;
  private long appUserRoles;
}
