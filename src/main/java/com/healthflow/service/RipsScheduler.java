package com.healthflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RipsScheduler {

    private static final Logger log = LoggerFactory.getLogger(RipsScheduler.class);

    @Autowired
    private RipsReminderService reminderService;

    // Ejecutar todos los días a las 8:00 AM (hora Colombia)
    @Scheduled(cron = "0 0 8 * * *", zone = "America/Bogota")
    public void verificarYEnviarRecordatorios() {
        log.info("Ejecutando tarea programada: verificar RIPS pendientes");
        reminderService.enviarRecordatoriosRipsPendientes();
    }
}