package model;

public enum Filter {
    // Estados básicos según el diagrama
    INICIAL,
    LISTO,
    DESPACHADO,
    EN_EJECUCION,
    TIEMPO_EXPIRADO,
    BLOQUEAR,
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
    
    // Nueva transición especial: Suspendido Bloqueado → Suspendido Listo
    TRANSICION_BLOQUEADO_A_LISTO,

    DE_LISTO_A_SUSPENDIDO,
    // Filtro especial
    TODO
}