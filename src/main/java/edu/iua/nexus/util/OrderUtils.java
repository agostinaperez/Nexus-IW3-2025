package edu.iua.nexus.util;

import edu.iua.nexus.model.Alarm;
import edu.iua.nexus.model.Order;

public class OrderUtils {
// Metodo utilitario para serializador
    public static String getAlarmStatus(Order order) {
        if (order.getAlarms() == null || order.getAlarms().isEmpty()) {
            return "Sin alarmas";
        }

        boolean hasPendingReview = false;
        boolean hasConfirmedIssue = false;

        for (Alarm alarm : order.getAlarms()) {
            switch (alarm.getStatus()) {
                case PENDING:
                    hasPendingReview = true;
                    break;
                case CONFIRMED_ISSUE:
                    hasConfirmedIssue = true;
                    break;
                default:
                    break;
            }
        }

        if (hasConfirmedIssue) {
            return "Problema";
        }
        if (hasPendingReview) {
            return "Pendiente";
        }
        return "Sin alarmas";
    }
}
