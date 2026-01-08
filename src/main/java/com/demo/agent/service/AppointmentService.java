package com.demo.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.agent.entity.Appointment;

public interface AppointmentService extends IService<Appointment> {
    Appointment getOne(Appointment appointment);
}