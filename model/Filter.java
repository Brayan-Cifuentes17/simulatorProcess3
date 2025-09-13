package model;

public enum Filter {
    // Estados básicos según el diagrama
    INICIAL,
    LISTO,
    DESPACHADO,
    EN_EJECUCION,
    TIEMPO_EXPIRADO,
    BLOQUEADO,
    DESPERTAR,
    FINALIZADO,
    
    // Estados de suspensión según el diagrama
    SUSPENDER_LISTOS,
    SUSPENDIDO_LISTO,
    REANUDAR_LISTOS,
    SUSPENDER_BLOQUEADOS,
    SUSPENDIDO_BLOQUEADO,
    REANUDAR_BLOQUEADOS,
    
    // Filtro especial
    TODO
}