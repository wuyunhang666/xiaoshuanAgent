package com.demo.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
@TableId(type = IdType.AUTO)
private Long id;
private String username;
private String idCard;
private String department;
private String date;
private String time;
private String doctorName;
}